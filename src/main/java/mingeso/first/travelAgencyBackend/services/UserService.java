package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.AccountStatus;
import mingeso.first.travelAgencyBackend.enums.Role;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;

import static mingeso.first.travelAgencyBackend.utils.ValidationUtils.isValidEmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public List<UserEntity> getUsers(){
        return userRepository.findAll();
    }

    public UserEntity getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BadRequestException("User not found with Keycloak ID: " + keycloakId));
    }

    public UserEntity syncUserFromKeycloak(String keycloakId, String email, String name, String lastName) {
        Optional<UserEntity> existingUser = userRepository.findByKeycloakId(keycloakId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        try {
            UserEntity newUser = new UserEntity();
            newUser.setKeycloakId(keycloakId);
            newUser.setEmail(email);
            newUser.setName(name != null ? name : "Sin nombre");
            newUser.setLastName(lastName != null ? lastName : "Sin apellido");
            newUser.setStateAccount(AccountStatus.ACTIVE);
            newUser.setRole(Role.CLIENT);
            newUser.setRut("PENDING_" + keycloakId.substring(0, 5));

            return userRepository.saveAndFlush(newUser);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return userRepository.findByKeycloakId(keycloakId)
                    .orElseGet(() -> userRepository.findByEmail(email)
                            .orElseThrow(() -> new BadRequestException("Error de consistencia en sincronización concurrente")));
        }
    }

    public UserEntity updateUserDetails(Long id, UserEntity updatedData) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        existingUser.setName(updatedData.getName());
        existingUser.setLastName(updatedData.getLastName());
        existingUser.setPhone(updatedData.getPhone());
        existingUser.setNationality(updatedData.getNationality());
        existingUser.setRut(updatedData.getRut());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setStateAccount(AccountStatus.INACTIVE);
        userRepository.save(user);
    }
}
