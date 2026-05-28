package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.AccountStatus;
import mingeso.first.travelAgencyBackend.enums.Role;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
        user.setId(1L);
        user.setKeycloakId("uuid-12345");
        user.setName("Juan");
        user.setLastName("Perez");
        user.setEmail("juan.perez@mail.com");
        user.setStateAccount(AccountStatus.ACTIVE);
        user.setRole(Role.CLIENT);
    }

    // ==========================================
    // TESTS: getUsers
    // ==========================================

    @Test
    @DisplayName("Debe retornar una lista de usuarios cuando existen registros")
    void getUsers_ShouldReturnList() {
        UserEntity user2 = new UserEntity();
        user2.setName("Maria");
        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserEntity> result = userService.getUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserEntity::getName).containsExactly("Juan", "Maria");
        verify(userRepository, times(1)).findAll();
    }

    // ==========================================
    // TESTS: getUserByKeycloakId
    // ==========================================

    @Test
    @DisplayName("Debe retornar el usuario buscado por Keycloak ID")
    void getUserByKeycloakId_Success() {
        when(userRepository.findByKeycloakId("uuid-12345")).thenReturn(Optional.of(user));

        UserEntity result = userService.getUserByKeycloakId("uuid-12345");

        assertThat(result).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("uuid-12345");
        verify(userRepository).findByKeycloakId("uuid-12345");
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si el Keycloak ID no existe")
    void getUserByKeycloakId_NotFound() {
        when(userRepository.findByKeycloakId("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByKeycloakId("non-existent"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found with Keycloak ID: non-existent");
    }

    // ==========================================
    // TESTS: syncUserFromKeycloak
    // ==========================================

    @Test
    @DisplayName("syncUserFromKeycloak: Debe retornar el usuario si ya existe previamente")
    void syncUserFromKeycloak_UserAlreadyExists() {
        when(userRepository.findByKeycloakId("uuid-12345")).thenReturn(Optional.of(user));

        UserEntity result = userService.syncUserFromKeycloak("uuid-12345", "juan.perez@mail.com", "Juan", "Perez");

        assertThat(result).isEqualTo(user);
        verify(userRepository, never()).saveAndFlush(any(UserEntity.class));
    }

    @Test
    @DisplayName("syncUserFromKeycloak: Debe crear y registrar un nuevo usuario con datos completos si no existe")
    void syncUserFromKeycloak_CreateNewUser_FullData() {
        when(userRepository.findByKeycloakId("uuid-12345")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.syncUserFromKeycloak("uuid-12345", "juan.perez@mail.com", "Juan", "Perez");

        assertThat(result).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("uuid-12345");
        assertThat(result.getName()).isEqualTo("Juan");
        assertThat(result.getLastName()).isEqualTo("Perez");
        assertThat(result.getStateAccount()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(result.getRole()).isEqualTo(Role.CLIENT);
        assertThat(result.getRut()).isEqualTo("PENDING_uuid-");
        verify(userRepository).saveAndFlush(any(UserEntity.class));
    }

    @Test
    @DisplayName("syncUserFromKeycloak: Debe usar nombres por defecto si los datos de Keycloak vienen nulos")
    void syncUserFromKeycloak_CreateNewUser_NullData() {
        when(userRepository.findByKeycloakId("uuid-12345")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.syncUserFromKeycloak("uuid-12345", "juan.perez@mail.com", null, null);

        assertThat(result.getName()).isEqualTo("Sin nombre");
        assertThat(result.getLastName()).isEqualTo("Sin apellido");
    }

    // ==========================================
    // TESTS CORREGIDOS: syncUserFromKeycloak (Concurrencia)
    // ==========================================

    @Test
    @DisplayName("syncUserFromKeycloak: Manejo de concurrencia: si falla por duplicado, recupera por KeycloakID")
    void syncUserFromKeycloak_Concurrency_FindByInverseKeycloakId() {
        // Primera consulta antes del try: no lo encuentra
        // Segunda consulta dentro del catch: sí lo encuentra
        when(userRepository.findByKeycloakId("uuid-12345"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(user));

        when(userRepository.saveAndFlush(any(UserEntity.class))).thenThrow(DataIntegrityViolationException.class);

        UserEntity result = userService.syncUserFromKeycloak("uuid-12345", "juan.perez@mail.com", "Juan", "Perez");

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("syncUserFromKeycloak: Manejo de concurrencia: si falla por duplicado de email, recupera por Email")
    void syncUserFromKeycloak_Concurrency_FindByInverseEmail() {
        // Primera y segunda consulta por KeycloakId devuelven vacío
        when(userRepository.findByKeycloakId("uuid-12345")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenThrow(DataIntegrityViolationException.class);

        // Usamos lenient() porque la ejecución depende del flujo exacto del orElseGet
        lenient().when(userRepository.findByEmail("juan.perez@mail.com")).thenReturn(Optional.of(user));

        UserEntity result = userService.syncUserFromKeycloak("uuid-12345", "juan.perez@mail.com", "Juan", "Perez");

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("syncUserFromKeycloak: Manejo de concurrencia: lanza excepción si no se recupera por ninguna vía")
    void syncUserFromKeycloak_Concurrency_ThrowsException() {
        when(userRepository.findByKeycloakId("uuid-12345")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenThrow(DataIntegrityViolationException.class);

        // Configuramos ambas búsquedas de rescate para que devuelvan vacío
        lenient().when(userRepository.findByEmail("juan.perez@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.syncUserFromKeycloak("uuid-12345", "juan.perez@mail.com", "Juan", "Perez"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Error de consistencia en sincronización concurrente");
    }

    // ==========================================
    // TESTS: updateUserDetails
    // ==========================================

    @Test
    @DisplayName("Debe actualizar correctamente los datos de un usuario existente")
    void updateUserDetails_Success() {
        Long userId = 1L;
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setName("Nombre Antiguo");

        UserEntity newData = new UserEntity();
        newData.setName("Nombre Nuevo");
        newData.setLastName("Apellido Nuevo");
        newData.setPhone("+56912345678");
        newData.setNationality("Chilena");
        newData.setRut("12.345.678-9");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.updateUserDetails(userId, newData);

        assertThat(result.getName()).isEqualTo("Nombre Nuevo");
        assertThat(result.getLastName()).isEqualTo("Apellido Nuevo");
        assertThat(result.getPhone()).isEqualTo("+56912345678");
        assertThat(result.getNationality()).isEqualTo("Chilena");
        assertThat(result.getRut()).isEqualTo("12.345.678-9");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException al intentar actualizar un usuario que no existe")
    void updateUserDetails_UserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserDetails(userId, new UserEntity()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found");
    }

    // ==========================================
    // TESTS: deleteUser
    // ==========================================

    @Test
    @DisplayName("Debe realizar un borrado lógico (cambiar a INACTIVE)")
    void deleteUser_Success() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        assertThat(user.getStateAccount()).isEqualTo(AccountStatus.INACTIVE);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException al intentar borrar un usuario inexistente")
    void deleteUser_NotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found");
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}