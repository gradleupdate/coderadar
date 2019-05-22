package io.reflectoring.coderadar.core.projectadministration.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.reflectoring.coderadar.core.projectadministration.CoderadarConfigurationProperties;
import io.reflectoring.coderadar.core.projectadministration.domain.Project;
import io.reflectoring.coderadar.core.projectadministration.port.driven.project.CreateProjectPort;
import io.reflectoring.coderadar.core.projectadministration.port.driver.project.create.CreateProjectCommand;
import io.reflectoring.coderadar.core.projectadministration.service.project.CreateProjectService;

import java.io.File;
import java.util.Date;

import io.reflectoring.coderadar.core.vcs.port.driver.CloneRepositoryUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class CreateProjectServiceTest {
  @Mock private CreateProjectPort createProjectPort;
  @Mock private CoderadarConfigurationProperties coderadarConfigurationProperties;
  @InjectMocks private CreateProjectService testSubject;

  @Test
  void returnsNewProjectId() {
    when(coderadarConfigurationProperties.getWorkdir()).thenReturn(new File("coderadar-workdir").toPath());
    CreateProjectCommand command =
        new CreateProjectCommand(
            "project", "username", "password", "http://valid.url", true, new Date(), new Date());

    Project project = new Project();
    project.setName("project");
    project.setVcsUrl("http://valid.url");
    project.setVcsUsername("username");
    project.setVcsPassword("password");
    project.setVcsOnline(true);
    project.setVcsStart(new Date());
    project.setVcsEnd(new Date());

    when(createProjectPort.createProject(any())).thenReturn(1L);

    Long projectId = testSubject.createProject(command);

    Assertions.assertEquals(1L, projectId.longValue());
  }
}
