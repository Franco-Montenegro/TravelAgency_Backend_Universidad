package mingeso.first.travelAgencyBackend.repositories;

import mingeso.first.travelAgencyBackend.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    public UserEntity findByRut(String rut);
    List<UserEntity> findByEmail(String email);

    //ward de codigo - borrar luego
    @Query(value = "SELECT * FROM users WHERE users.rut = :rut", nativeQuery = true)
    UserEntity findByRutNativeQuery(@Param("rut") String rut);
}
