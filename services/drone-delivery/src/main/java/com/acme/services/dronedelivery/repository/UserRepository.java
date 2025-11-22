package com.acme.services.dronedelivery.repository;

import com.acme.services.dronedelivery.model.entity.User;
import com.acme.services.dronedelivery.model.enums.UserType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByNameAndUserType(String name, UserType userType);
}
