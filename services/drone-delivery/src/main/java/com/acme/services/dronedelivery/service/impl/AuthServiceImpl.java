package com.acme.services.dronedelivery.service.impl;

import com.acme.services.dronedelivery.model.dto.AuthRequest;
import com.acme.services.dronedelivery.model.dto.AuthResponse;
import com.acme.services.dronedelivery.model.entity.Drone;
import com.acme.services.dronedelivery.model.entity.User;
import com.acme.services.dronedelivery.model.enums.DroneStatus;
import com.acme.services.dronedelivery.model.enums.UserType;
import com.acme.services.dronedelivery.repository.DroneRepository;
import com.acme.services.dronedelivery.repository.UserRepository;
import com.acme.services.dronedelivery.security.JwtUtil;
import com.acme.services.dronedelivery.service.AuthService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final DroneRepository droneRepository;
  private final JwtUtil jwtUtil;

  @Override
  @Transactional
  public AuthResponse authenticate(AuthRequest request) {
    log.info("Authenticating user: {} with type: {}", request.getName(), request.getType());

    // Find or create user
    User user =
        userRepository
            .findByNameAndUserType(request.getName(), request.getType())
            .orElseGet(
                () -> {
                  User newUser =
                      User.builder().name(request.getName()).userType(request.getType()).build();
                  return userRepository.save(newUser);
                });

    // If user is a drone, create or update drone entity
    if (user.getUserType() == UserType.DRONE) {
      droneRepository
          .findByName(user.getName())
          .orElseGet(
              () -> {
                log.info("Creating drone entity for user: {}", user.getName());
                Drone newDrone =
                    Drone.builder()
                        .name(user.getName())
                        .userType(user.getUserType())
                        .status(DroneStatus.AVAILABLE)
                        .isBroken(false)
                        .latitude(BigDecimal.ZERO)
                        .longitude(BigDecimal.ZERO)
                        .build();
                return droneRepository.save(newDrone);
              });
    }

    // Generate JWT token
    String token = jwtUtil.generateToken(user.getName(), user.getUserType().name());

    log.info("User authenticated successfully: {}", user.getName());

    return AuthResponse.builder()
        .token(token)
        .name(user.getName())
        .type(user.getUserType())
        .build();
  }
}
