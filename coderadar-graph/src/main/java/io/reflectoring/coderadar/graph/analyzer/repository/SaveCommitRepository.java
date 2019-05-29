package io.reflectoring.coderadar.graph.analyzer.repository;

import io.reflectoring.coderadar.core.projectadministration.domain.Commit;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaveCommitRepository extends Neo4jRepository<Commit, Long> {}
