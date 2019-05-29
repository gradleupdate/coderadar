package io.reflectoring.coderadar.graph.projectadministration.module;

import io.reflectoring.coderadar.core.projectadministration.domain.Module;
import io.reflectoring.coderadar.graph.projectadministration.module.repository.DeleteModuleRepository;
import io.reflectoring.coderadar.graph.projectadministration.module.service.DeleteModuleService;
import io.reflectoring.coderadar.graph.projectadministration.project.repository.DeleteProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@DisplayName("Delete module")
class DeleteModuleServiceTest {
  private DeleteModuleRepository deleteModuleRepository = mock(DeleteModuleRepository.class);
  private DeleteProjectRepository deleteProjectRepository = mock(DeleteProjectRepository.class);

  private DeleteModuleService deleteModuleService;

  @BeforeEach
  void setUp() {
    deleteModuleService = new DeleteModuleService(deleteModuleRepository, deleteProjectRepository);
  }

  @Test
  @DisplayName("Should delete module when passing a valid module entity")
  void shouldDleteModuleWhenPassingAValidModuleEntity() {
    doNothing().when(deleteModuleRepository).delete(isA(Module.class));
    deleteModuleService.delete(new Module());
    verify(deleteModuleRepository, times(1)).delete(any(Module.class));
  }

  @Test
  @DisplayName("Should delete module when passing a valid module id")
  void shouldDeleteModuleWhenPassingAValidModuleId() {
    doNothing().when(deleteModuleRepository).deleteById(isA(Long.class));
    deleteModuleService.delete(1L);
    verify(deleteModuleRepository, times(1)).deleteById(any(Long.class));
  }
}
