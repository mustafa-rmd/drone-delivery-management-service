package com.acme.services.dronedelivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.services.dronedelivery.model.dto.AuthRequest;
import com.acme.services.dronedelivery.model.dto.AuthResponse;
import com.acme.services.dronedelivery.model.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/** Integration tests for authentication endpoints. */
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should authenticate admin user successfully")
  void shouldAuthenticateAdminUser() throws Exception {
    // Given
    AuthRequest request = AuthRequest.builder().name("admin").type(UserType.ADMIN).build();

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.name").value("admin"))
            .andExpect(jsonPath("$.type").value("admin"))
            .andReturn();

    AuthResponse response =
        objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
    assertThat(response.getToken()).isNotEmpty();
    assertThat(response.getName()).isEqualTo("admin");
    assertThat(response.getType()).isEqualTo(UserType.ADMIN);
  }

  @Test
  @DisplayName("Should authenticate end user successfully")
  void shouldAuthenticateEndUser() throws Exception {
    // Given
    AuthRequest request = AuthRequest.builder().name("john_doe").type(UserType.ENDUSER).build();

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.name").value("john_doe"))
        .andExpect(jsonPath("$.type").value("enduser"));
  }

  @Test
  @DisplayName("Should authenticate drone and create drone entity")
  void shouldAuthenticateDroneAndCreateEntity() throws Exception {
    // Given
    AuthRequest request = AuthRequest.builder().name("Drone-Alpha-1").type(UserType.DRONE).build();

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.name").value("Drone-Alpha-1"))
        .andExpect(jsonPath("$.type").value("drone"));
  }

  @Test
  @DisplayName("Should reject authentication with invalid name - too short")
  void shouldRejectAuthenticationWithShortName() throws Exception {
    // Given
    AuthRequest request = AuthRequest.builder().name("ab").type(UserType.ADMIN).build();

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").exists());
  }

  @Test
  @DisplayName("Should reject authentication with invalid name - special characters")
  void shouldRejectAuthenticationWithSpecialCharacters() throws Exception {
    // Given
    AuthRequest request = AuthRequest.builder().name("user@name").type(UserType.ADMIN).build();

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").exists());
  }

  @Test
  @DisplayName("Should reject authentication with missing name")
  void shouldRejectAuthenticationWithMissingName() throws Exception {
    // Given
    AuthRequest request = AuthRequest.builder().type(UserType.ADMIN).build();

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").exists());
  }

  @Test
  @DisplayName("Should reject authentication with missing user type")
  void shouldRejectAuthenticationWithMissingUserType() throws Exception {
    // Given
    AuthRequest request = AuthRequest.builder().name("admin").build();

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.type").exists());
  }

}
