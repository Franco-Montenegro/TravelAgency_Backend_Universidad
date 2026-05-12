package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.AccountStatus;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setName("Juan");
        user.setLastName("Perez");
        user.setEmail("juan.perez@mail.com");
        user.setPassword("password123");
    }

    @Test
    @DisplayName("Debe registrar un usuario exitosamente con estado ACTIVO")
    void registerUserSuccess() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        // Act
        UserEntity savedUser = userService.registerUser(user);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getStateAccount()).isEqualTo(AccountStatus.ACTIVE);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Debe lanzar excepción si faltan campos obligatorios")
    void registerUserMissingFields() {
        user.setName(null);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing required fields");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el formato del email es inválido")
    void registerUserInvalidEmail() {
        user.setEmail("correo-invalido");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el email ya existe (Unicidad)")
    void registerUserDuplicateEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already in use");
    }

    @Test
    @DisplayName("Debe realizar un borrado lógico (cambiar a INACTIVE)")
    void deleteUserLogical() {
        // Arrange
        Long userId = 1L;
        user.setStateAccount(AccountStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(userId);

        // Assert
        assertThat(user.getStateAccount()).isEqualTo(AccountStatus.INACTIVE);
        verify(userRepository, times(1)).save(user);
    }
}
