package io.reflectoring.coderadar.projectadministration.service.user.load;

import io.reflectoring.coderadar.projectadministration.UserNotFoundException;
import io.reflectoring.coderadar.projectadministration.domain.User;
import io.reflectoring.coderadar.projectadministration.port.driven.user.LoadUserPort;
import io.reflectoring.coderadar.projectadministration.port.driver.user.load.LoadUserResponse;
import io.reflectoring.coderadar.projectadministration.port.driver.user.load.LoadUserUseCase;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoadUserService implements LoadUserUseCase {

  private final LoadUserPort port;

  @Autowired
  public LoadUserService(LoadUserPort port) {
    this.port = port;
  }

  @Override
  public LoadUserResponse loadUser(Long id) {
    Optional<User> user = port.loadUser(id);
    if (user.isPresent()) {
      return new LoadUserResponse(user.get().getId(), user.get().getUsername());
    } else {
      throw new UserNotFoundException(id);
    }
  }
}
