package io.reflectoring.coderadar.graph.projectadministration.analyzerconfig;

import io.reflectoring.coderadar.core.projectadministration.domain.AnalyzerConfiguration;
import io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.repository.GetAnalyzerConfigurationRepository;
import io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.service.GetAnalyzerConfigurationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Get analyzer configuration")
class GetAnalyzerConfigurationServiceTest {
  private GetAnalyzerConfigurationRepository getAnalyzerConfigurationRepository =
      mock(GetAnalyzerConfigurationRepository.class);

  private GetAnalyzerConfigurationService getAnalyzerConfigurationService;

  @BeforeEach
  void setUp() {
    getAnalyzerConfigurationService =
        new GetAnalyzerConfigurationService(getAnalyzerConfigurationRepository);
  }

  @Test
  @DisplayName(
      "Should return analyzer configuration as optional when a analyzer configuration with the passing ID exists")
  void shouldReturnAnalzerConfigurationAsOptionalWhenAAnalzerConfigurationWithThePassingIdExists() {
    AnalyzerConfiguration mockedItem = new AnalyzerConfiguration();
    mockedItem.setId(1L);
    when(getAnalyzerConfigurationRepository.findById(any(Long.class)))
        .thenReturn(Optional.of(mockedItem));

    Optional<AnalyzerConfiguration> returned =
        getAnalyzerConfigurationService.getAnalyzerConfiguration(1L);

    verify(getAnalyzerConfigurationRepository, times(1)).findById(1L);
    verifyNoMoreInteractions(getAnalyzerConfigurationRepository);
    Assertions.assertTrue(returned.isPresent());
    Assertions.assertEquals(new Long(1L), returned.get().getId());
  }

  @Test
  @DisplayName(
      "Should return analyzer configuration as empty optional when a analyzer configuration with the passing ID doesn't exists")
  void
      shouldReturnAnalyzerConfigurationAsEmptyOptionalWhenAAnalzerConfigurationWithThePassingIdDoesntExists() {
    Optional<AnalyzerConfiguration> mockedItem = Optional.empty();
    when(getAnalyzerConfigurationRepository.findById(any(Long.class))).thenReturn(mockedItem);

    Optional<AnalyzerConfiguration> returned =
        getAnalyzerConfigurationService.getAnalyzerConfiguration(1L);

    verify(getAnalyzerConfigurationRepository, times(1)).findById(1L);
    verifyNoMoreInteractions(getAnalyzerConfigurationRepository);
    Assertions.assertFalse(returned.isPresent());
  }
}
