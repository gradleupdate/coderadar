package org.wickedsource.coderadar.job.analyze;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.gitective.core.BlobUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wickedsource.coderadar.analyzer.api.FileMetrics;
import org.wickedsource.coderadar.analyzer.api.Finding;
import org.wickedsource.coderadar.analyzer.api.Metric;
import org.wickedsource.coderadar.analyzer.api.SourceCodeFileAnalyzerPlugin;
import org.wickedsource.coderadar.analyzer.domain.*;
import org.wickedsource.coderadar.analyzer.match.FileMatchingPattern;
import org.wickedsource.coderadar.analyzingjob.domain.AnalyzingJob;
import org.wickedsource.coderadar.analyzingjob.domain.AnalyzingJobRepository;
import org.wickedsource.coderadar.commit.domain.Commit;
import org.wickedsource.coderadar.commit.domain.CommitRepository;
import org.wickedsource.coderadar.core.WorkdirManager;
import org.wickedsource.coderadar.file.domain.File;
import org.wickedsource.coderadar.file.domain.FileRepository;
import org.wickedsource.coderadar.filepattern.domain.FilePattern;
import org.wickedsource.coderadar.filepattern.domain.FilePatternRepository;
import org.wickedsource.coderadar.filepattern.domain.FileSetType;
import org.wickedsource.coderadar.metric.domain.finding.FindingRepository;
import org.wickedsource.coderadar.metric.domain.metricvalue.MetricValue;
import org.wickedsource.coderadar.metric.domain.metricvalue.MetricValueId;
import org.wickedsource.coderadar.metric.domain.metricvalue.MetricValueRepository;
import org.wickedsource.coderadar.project.domain.Project;
import org.wickedsource.coderadar.vcs.git.GitCommitFinder;

@Service
public class CommitAnalyzer {

  private Logger logger = LoggerFactory.getLogger(CommitAnalyzer.class);

  private CommitRepository commitRepository;

  private WorkdirManager workdirManager;

  private FilePatternRepository filePatternRepository;

  private GitCommitFinder commitFinder;

  private AnalyzerPluginRegistry analyzerRegistry;

  private AnalyzerConfigurationRepository analyzerConfigurationRepository;

  private AnalyzerConfigurationFileRepository analyzerConfigurationFileRepository;

  private FileAnalyzer fileAnalyzer;

  private FileRepository fileRepository;

  private MetricValueRepository metricValueRepository;

  private FindingRepository findingRepository;

  private AnalyzingJobRepository analyzingJobRepository;

  private static final Set<DiffEntry.ChangeType> CHANGES_TO_ANALYZE =
      EnumSet.of(
          DiffEntry.ChangeType.ADD,
          DiffEntry.ChangeType.COPY,
          DiffEntry.ChangeType.MODIFY,
          DiffEntry.ChangeType.RENAME);

  @Autowired
  public CommitAnalyzer(
      CommitRepository commitRepository,
      WorkdirManager workdirManager,
      FilePatternRepository filePatternRepository,
      GitCommitFinder commitFinder,
      AnalyzerPluginRegistry analyzerRegistry,
      AnalyzerConfigurationRepository analyzerConfigurationRepository,
      AnalyzerConfigurationFileRepository analyzerConfigurationFileRepository,
      FileAnalyzer fileAnalyzer,
      FileRepository fileRepository,
      MetricValueRepository metricValueRepository,
      FindingRepository findingRepository,
      AnalyzingJobRepository analyzingJobRepository) {
    this.commitRepository = commitRepository;
    this.workdirManager = workdirManager;
    this.filePatternRepository = filePatternRepository;
    this.commitFinder = commitFinder;
    this.analyzerRegistry = analyzerRegistry;
    this.analyzerConfigurationRepository = analyzerConfigurationRepository;
    this.analyzerConfigurationFileRepository = analyzerConfigurationFileRepository;
    this.fileAnalyzer = fileAnalyzer;
    this.fileRepository = fileRepository;
    this.metricValueRepository = metricValueRepository;
    this.findingRepository = findingRepository;
    this.analyzingJobRepository = analyzingJobRepository;
  }

  public void analyzeCommit(Commit commit) {
    if (commit == null) {
      throw new IllegalArgumentException("argument commit must not be null!");
    }

    Path gitRoot = workdirManager.getLocalGitRoot(commit.getProject().getName());
    try {
      Git gitClient = Git.open(gitRoot.toFile());
      boolean isFirstCommit = isFirstCommitInAnalyzingJob(commit);

      List<SourceCodeFileAnalyzerPlugin> analyzers = getAnalyzersForProject(commit.getProject());

      if (analyzers.isEmpty()) {
        logger.warn(
            "skipping analysis of commit {} since there are no analyzers configured for project {}!",
            commit.getName(),
            commit.getProject().getName());
        return;
      }

      logger.info("starting analysis of commit {}", commit.getName());
      if (isFirstCommit) {
        // If it is the first commit to be analyzed we want to analyze ALL files that are in the repo
        // at the time of the commit so that we have all the files' metrics ...
        walkAllFilesInCommit(gitClient, commit, analyzers);
      } else {
        // ... otherwise we only want to analyze the files that were changed with this commit
        walkTouchedFilesInCommit(gitClient, commit, analyzers);
      }
    } catch (IOException e) {
      throw new IllegalStateException(String.format("error opening git root %s", gitRoot));
    }
    commit.setAnalyzed(Boolean.TRUE);
    commitRepository.save(commit);
  }

  private boolean isFirstCommitInAnalyzingJob(Commit commit) {
    AnalyzingJob strategy =
        analyzingJobRepository.findByProjectId(commit.getProject().getId());

    if (strategy.getFromDate() == null) {
      return false;
    }

    Commit firstCommitInDateRange =
        commitRepository.findFirstCommitAfterDate(
            commit.getProject().getId(), strategy.getFromDate());
    return firstCommitInDateRange.getId() == commit.getId();
  }

  private List<SourceCodeFileAnalyzerPlugin> getAnalyzersForProject(Project project) {
    List<SourceCodeFileAnalyzerPlugin> analyzers = new ArrayList<>();
    List<AnalyzerConfiguration> configs =
        analyzerConfigurationRepository.findByProjectId(project.getId());
    for (AnalyzerConfiguration config : configs) {
      AnalyzerConfigurationFile configFile =
          analyzerConfigurationFileRepository
              .findByAnalyzerConfigurationProjectIdAndAnalyzerConfigurationId(
                  project.getId(), config.getId());
      if (configFile != null) {
        analyzers.add(
            analyzerRegistry.createAnalyzer(config.getAnalyzerName(), configFile.getFileData()));
      } else {
        analyzers.add(analyzerRegistry.createAnalyzer(config.getAnalyzerName()));
      }
    }
    return analyzers;
  }

  /**
   * Iterates over all files that were modified within a commit and runs the analyzers over them to
   * store metric values in the database.
   */
  private void walkTouchedFilesInCommit(
      Git gitClient, Commit commit, List<SourceCodeFileAnalyzerPlugin> analyzers)
      throws IOException {
    RevCommit gitCommit = commitFinder.findCommit(gitClient, commit.getName());
    if (gitCommit == null) {
      throw new IllegalArgumentException(
          String.format(
              "commit with name %s was not found in git root %s",
              commit.getName(), gitClient.getRepository().getDirectory()));
    }

    DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
    diffFormatter.setRepository(gitClient.getRepository());
    diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
    diffFormatter.setDetectRenames(true);

    ObjectId parentId = null;
    if (gitCommit.getParentCount() > 0) {
      // TODO: support multiple parents
      parentId = gitCommit.getParent(0).getId();
    }

    FileMatchingPattern pattern = getFileMatchingPattern(commit);

    List<DiffEntry> diffs = diffFormatter.scan(parentId, gitCommit);
    for (DiffEntry diff : diffs) {
      String filepath = diff.getPath(DiffEntry.Side.NEW);

      if (!shouldBeAnalyzed(diff.getChangeType())) {
        logger.debug(
            "skipping analysis of file {} because of changetype {}",
            filepath,
            diff.getChangeType());
        continue;
      }
      if (!pattern.matches(filepath)) {
        logger.debug(
            "skipping analysis of file {} because it does not match source file pattern of project {}",
            filepath,
            commit.getProject().getName());
        continue;
      }

      analyzeFile(gitClient, commit, analyzers, gitCommit, filepath);
    }
  }

  private FileMatchingPattern getFileMatchingPattern(Commit commit) {
    List<FilePattern> sourceFilePatterns =
        filePatternRepository.findByProjectIdAndFileSetType(
            commit.getProject().getId(), FileSetType.SOURCE);
    return toFileMatchingPattern(sourceFilePatterns);
  }

  /**
   * Iterates over all files that exist at the time of the given commit and runs the analyzers over
   * them to store metric values in the database.
   */
  private void walkAllFilesInCommit(
      Git gitClient, Commit commit, List<SourceCodeFileAnalyzerPlugin> analyzers) {
    RevCommit gitCommit = commitFinder.findCommit(gitClient, commit.getName());
    if (gitCommit == null) {
      throw new IllegalArgumentException(
          String.format(
              "commit with name %s was not found in git root %s",
              commit.getName(), gitClient.getRepository().getDirectory()));
    }

    FileMatchingPattern pattern = getFileMatchingPattern(commit);

    try (TreeWalk treeWalk = new TreeWalk(gitClient.getRepository())) {
      treeWalk.addTree(gitCommit.getTree());
      treeWalk.setRecursive(true);
      while (treeWalk.next()) {
        String filepath = treeWalk.getPathString();

        if (!pattern.matches(filepath)) {
          logger.debug(
              "skipping analysis of file {} because it does not match source file pattern of project {}",
              filepath,
              commit.getProject().getName());
          continue;
        }

        analyzeFile(gitClient, commit, analyzers, gitCommit, filepath);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void analyzeFile(
      Git gitClient,
      Commit commit,
      List<SourceCodeFileAnalyzerPlugin> analyzers,
      RevCommit gitCommit,
      String filepath) {
    byte[] fileContent =
        BlobUtils.getRawContent(gitClient.getRepository(), gitCommit.getId(), filepath);
    FileMetrics metrics = fileAnalyzer.analyzeFile(analyzers, filepath, fileContent);
    storeMetrics(commit, filepath, metrics);
  }

  private boolean shouldBeAnalyzed(DiffEntry.ChangeType changeType) {
    return CHANGES_TO_ANALYZE.contains(changeType);
  }

  private void storeMetrics(Commit commit, String filePath, FileMetrics metrics) {
    File file = findInCommit(filePath, commit);
    if (file == null) {
      throw new IllegalStateException(
          String.format(
              "file %s not found for commit %s in database!", filePath, commit.getName()));
    }

    for (Metric metric : metrics.getMetrics()) {
      MetricValueId id = new MetricValueId(commit, file, metric.getId());
      MetricValue metricValue = new MetricValue(id, metrics.getMetricCount(metric));
      metricValueRepository.save(metricValue);

      for (Finding finding : metrics.getFindings(metric)) {
        org.wickedsource.coderadar.metric.domain.finding.Finding entity =
            new org.wickedsource.coderadar.metric.domain.finding.Finding();
        entity.setId(id);
        entity.setLineStart(finding.getLineStart());
        entity.setLineEnd(finding.getLineEnd());
        entity.setCharStart(finding.getCharStart());
        entity.setLineEnd(finding.getLineEnd());
        findingRepository.save(entity);
      }
    }
  }

  private File findInCommit(String filePath, Commit commit) {
    List<File> files =
        fileRepository.findInCommit(filePath, commit.getName(), commit.getProject().getId());
    if (files.size() == 1) {
      return files.get(0);
    } else if (files.size() > 1) {
      // Usually, we only get one file as result. However in the exotic case that MySQL is used (whose queries
      // are case insensitive by default) and the same file exists in the database more than once with different
      // upper or lower case characters, we can have more than one result. In this case, we select the correct
      // file by hand.
      for (File file : files) {
        if (file.getFilepath().equals(filePath)) {
          return file;
        }
      }
    }
    throw new IllegalStateException(
        String.format("could not find file '%s' in commit %s", filePath, commit.getName()));
  }

  private FileMatchingPattern toFileMatchingPattern(List<FilePattern> filePatternList) {
    FileMatchingPattern pattern = new FileMatchingPattern();
    for (FilePattern files : filePatternList) {
      switch (files.getInclusionType()) {
        case INCLUDE:
          pattern.addIncludePattern(files.getPattern());
          break;
        case EXCLUDE:
          pattern.addExcludePattern(files.getPattern());
          break;
        default:
          throw new IllegalStateException(
              String.format("invalid InclusionType %s", files.getInclusionType()));
      }
    }
    return pattern;
  }
}