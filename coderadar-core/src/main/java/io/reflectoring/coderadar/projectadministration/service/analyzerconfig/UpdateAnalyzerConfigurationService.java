package io.reflectoring.coderadar.projectadministration.service.analyzerconfig;

import io.reflectoring.coderadar.projectadministration.AnalyzerConfigurationNotFoundException;
import io.reflectoring.coderadar.projectadministration.domain.AnalyzerConfiguration;
import io.reflectoring.coderadar.projectadministration.port.driven.analyzerconfig.GetAnalyzerConfigurationPort;
import io.reflectoring.coderadar.projectadministration.port.driven.analyzerconfig.UpdateAnalyzerConfigurationPort;
import io.reflectoring.coderadar.projectadministration.port.driver.analyzerconfig.update.UpdateAnalyzerConfigurationCommand;
import io.reflectoring.coderadar.projectadministration.port.driver.analyzerconfig.update.UpdateAnalyzerConfigurationUseCase;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateAnalyzerConfigurationService implements UpdateAnalyzerConfigurationUseCase {

  private final GetAnalyzerConfigurationPort getAnalyzerConfigurationPort;
  private final UpdateAnalyzerConfigurationPort updateAnalyzerConfigurationPort;

  @Autowired
  public UpdateAnalyzerConfigurationService(
      UpdateAnalyzerConfigurationPort updateAnalyzerConfigurationPort,
      GetAnalyzerConfigurationPort getAnalyzerConfigurationPort) {
    this.updateAnalyzerConfigurationPort = updateAnalyzerConfigurationPort;
    this.getAnalyzerConfigurationPort = getAnalyzerConfigurationPort;
  }

  @Override
  public void update(UpdateAnalyzerConfigurationCommand command, Long analyzerId)
      throws AnalyzerConfigurationNotFoundException {
    Optional<AnalyzerConfiguration> analyzerConfiguration =
        getAnalyzerConfigurationPort.getAnalyzerConfiguration(analyzerId);
    if (analyzerConfiguration.isPresent()) {
      AnalyzerConfiguration newAnalyzerConfiguration = analyzerConfiguration.get();
      newAnalyzerConfiguration.setAnalyzerName(command.getAnalyzerName());
      newAnalyzerConfiguration.setEnabled(command.getEnabled());
      updateAnalyzerConfigurationPort.update(newAnalyzerConfiguration);
    } else {
      throw new AnalyzerConfigurationNotFoundException(analyzerId);
    }
  }
}
