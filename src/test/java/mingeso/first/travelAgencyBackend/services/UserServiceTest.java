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

import java.util.ArrayList;
import java.util.List;
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

    @Test
    @DisplayName("Debe retornar una lista de usuarios cuando existen registros")
    void getUsers_ShouldReturnList() {
        // GIVEN
        UserEntity user1 = new UserEntity();
        user1.setName("Juan");
        UserEntity user2 = new UserEntity();
        user2.setName("Maria");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // WHEN
        List<UserEntity> result = userService.getUsers();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserEntity::getName).containsExactly("Juan", "Maria");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe actualizar correctamente los datos de un usuario existente")
    void updateUserDetails_Success() {
        // GIVEN
        Long userId = 1L;
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setName("Nombre Antiguo");

        UserEntity newData = new UserEntity();
        newData.setName("Nombre Nuevo");
        newData.setPhone("+56912345678");
        newData.setNationality("Chilena");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        UserEntity result = userService.updateUserDetails(userId, newData);

        // THEN
        assertThat(result.getName()).isEqualTo("Nombre Nuevo");
        assertThat(result.getPhone()).isEqualTo("+56912345678");
        assertThat(result.getNationality()).isEqualTo("Chilena");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException al intentar actualizar un usuario que no existe")
    void updateUserDetails_UserNotFound() {
        // GIVEN
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> userService.updateUserDetails(userId, new UserEntity()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found");
    }
}
