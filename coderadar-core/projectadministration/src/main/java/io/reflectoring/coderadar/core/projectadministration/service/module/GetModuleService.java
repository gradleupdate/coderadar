package io.reflectoring.coderadar.core.projectadministration.service.module;

import io.reflectoring.coderadar.core.projectadministration.domain.Module;
import io.reflectoring.coderadar.core.projectadministration.port.driven.module.GetModulePort;
import io.reflectoring.coderadar.core.projectadministration.port.driver.module.get.GetModuleResponse;
import io.reflectoring.coderadar.core.projectadministration.port.driver.module.get.GetModuleUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetModuleService implements GetModuleUseCase {
  private final GetModulePort getModulePort;

  @Autowired
  public GetModuleService(GetModulePort getModulePort) {
    this.getModulePort = getModulePort;
  }

  @Override
  public GetModuleResponse get(Long id) {
    Module module = getModulePort.get(id);
    return new GetModuleResponse(id, module.getPath());
  }
}
