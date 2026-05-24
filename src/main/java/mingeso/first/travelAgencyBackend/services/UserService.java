package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.AccountStatus;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;

import static mingeso.first.travelAgencyBackend.utils.ValidationUtils.isValidEmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public List<UserEntity> getUsers(){
        return userRepository.findAll();
    }

    public UserEntity registerUser(UserEntity user) {
        //
        if (user.getName() == null || user.getLastName() == null || user.getEmail() == null || user.getPassword() == null) {
            throw new BadRequestException("Missing required fields");
        }
        if (!isValidEmail(user.getEmail())) {
            throw new BadRequestException("Invalid email format");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BadRequestException("Email already in use");
        }
        user.setStateAccount(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    public UserEntity updateUserDetails(Long id, UserEntity updatedData) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        existingUser.setName(updatedData.getName());
        existingUser.setPhone(updatedData.getPhone());
        existingUser.setNationality(updatedData.getNationality());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setStateAccount(AccountStatus.INACTIVE);
        userRepository.save(user);
    }
}
