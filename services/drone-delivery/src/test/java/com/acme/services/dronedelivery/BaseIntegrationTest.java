package com.acme.services.dronedelivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.services.dronedelivery.model.dto.AuthRequest;
import com.acme.services.dronedelivery.model.dto.AuthResponse;
import com.acme.services.dronedelivery.model.enums.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests using Testcontainers. Provides common setup for PostgreSQL
 * container and authentication utilities.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  static PostgreSQLContainer<?> postgres;

  static {
    postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("acme_test")
            .withUsername("postgres")
            .withPassword("postgres")
            .withReuse(true);
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.datasource.url", () -> postgres.getJdbcUrl() + "&currentSchema=drone_delivery");
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  /**
   * Authenticate a user and return the JWT token.
   *
   * @param name the user name
   * @param type the user type
   * @return JWT token
   */
  protected String authenticate(String name, UserType type) throws Exception {
    AuthRequest authRequest = AuthRequest.builder().name(name).type(type).build();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isOk())
            .andReturn();

    AuthResponse authResponse =
        objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
    return authResponse.getToken();
  }

  /**
   * Authenticate as an admin user.
   *
   * @return JWT token for admin
   */
  protected String authenticateAsAdmin() throws Exception {
    return authenticate("admin", UserType.ADMIN);
  }

  /**
   * Authenticate as an end user.
   *
   * @param username the username
   * @return JWT token for end user
   */
  protected String authenticateAsEndUser(String username) throws Exception {
    return authenticate(username, UserType.ENDUSER);
  }

  /**
   * Authenticate as a drone.
   *
   * @param droneName the drone name
   * @return JWT token for drone
   */
  protected String authenticateAsDrone(String droneName) throws Exception {
    return authenticate(droneName, UserType.DRONE);
  }

  /**
   * Get authorization header with Bearer token.
   *
   * @param token JWT token
   * @return Authorization header value
   */
  protected String bearerToken(String token) {
    return "Bearer " + token;
  }
}
