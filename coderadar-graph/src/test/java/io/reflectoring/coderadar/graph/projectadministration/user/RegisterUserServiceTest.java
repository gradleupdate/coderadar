package io.reflectoring.coderadar.graph.projectadministration.user;

import io.reflectoring.coderadar.core.projectadministration.domain.User;
import io.reflectoring.coderadar.graph.projectadministration.user.repository.RegisterUserRepository;
import io.reflectoring.coderadar.graph.projectadministration.user.service.RegisterUserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class RegisterUserServiceTest {
  private RegisterUserRepository registerUserRepository = mock(RegisterUserRepository.class);

  @Test
  @DisplayName("Should return long when passing valid argument")
  void shouldReturnLongWhenPassingValidArgument() {
    RegisterUserService registerUserService = new RegisterUserService(registerUserRepository);

    User testUser = new User();
    testUser.setId(20L);
    Mockito.when(registerUserRepository.save(any())).thenReturn(testUser);

    Long returned = registerUserService.register(new User());
    Assertions.assertThat(returned).isNotNull();
  }
}
