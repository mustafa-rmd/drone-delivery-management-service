package com.acme.services.dronedelivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/** Integration tests for order management endpoints. */
@DisplayName("Order Management Integration Tests")
class OrderIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should create order as end user")
  void shouldCreateOrderAsEndUser() throws Exception {
    // Given
    String token = authenticateAsEndUser("john_doe");
    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    // When & Then
    MvcResult result =
        mockMvc
            .perform(
                post("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.enduserName").value("john_doe"))
            .andExpect(jsonPath("$.status").value("pending"))
            .andExpect(jsonPath("$.originLatitude").value(40.7128))
            .andExpect(jsonPath("$.originLongitude").value(-74.0060))
            .andExpect(jsonPath("$.destinationLatitude").value(40.7589))
            .andExpect(jsonPath("$.destinationLongitude").value(-73.9851))
            .andReturn();

    OrderDto order =
        objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
    assertThat(order.getId()).isNotNull();
    assertThat(order.getEnduserName()).isEqualTo("john_doe");
  }

  @Test
  @DisplayName("Should reject order creation without authentication")
  void shouldRejectOrderCreationWithoutAuth() throws Exception {
    // Given
    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should reject order creation as drone")
  void shouldRejectOrderCreationAsDrone() throws Exception {
    // Given
    String token = authenticateAsDrone("Drone-Alpha-1");
    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should get my orders as end user")
  void shouldGetMyOrdersAsEndUser() throws Exception {
    // Given
    String token = authenticateAsEndUser("jane_doe");

    // Create an order first
    CreateOrderRequest createRequest =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    mockMvc.perform(
        post("/api/orders")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)));

    // When & Then
    mockMvc
        .perform(
            get("/api/orders/my-orders")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].enduserName").value("jane_doe"));
  }

  @Test
  @DisplayName("Should get all orders as admin")
  void shouldGetAllOrdersAsAdmin() throws Exception {
    // Given
    String adminToken = authenticateAsAdmin();

    // When & Then
    mockMvc
        .perform(
            get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(adminToken))
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("Should reject invalid coordinates - latitude out of range")
  void shouldRejectInvalidLatitude() throws Exception {
    // Given
    String token = authenticateAsEndUser("test_user");
    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("91.0")) // Invalid: > 90
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.originLatitude").exists());
  }

  @Test
  @DisplayName("Should reject invalid coordinates - longitude out of range")
  void shouldRejectInvalidLongitude() throws Exception {
    // Given
    String token = authenticateAsEndUser("test_user");
    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-181.0")) // Invalid: < -180
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.originLongitude").exists());
  }

  @Test
  @DisplayName("Should reject order with same origin and destination")
  void shouldRejectSameOriginAndDestination() throws Exception {
    // Given
    String token = authenticateAsEndUser("test_user");
    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7128"))
            .destinationLongitude(new BigDecimal("-74.0060"))
            .build();

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.errors.message")
                .value(
                    "Origin and destination coordinates must be different (minimum distance: ~1 meter)"));
  }

  @Test
  @DisplayName("Should withdraw order as end user")
  void shouldWithdrawOrderAsEndUser() throws Exception {
    // Given
    String token = authenticateAsEndUser("withdraw_user");

    // Create an order first
    CreateOrderRequest createRequest =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andReturn();

    OrderDto createdOrder =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderDto.class);

    // When & Then - Withdraw the order
    mockMvc
        .perform(
            post("/api/orders/" + createdOrder.getId() + "/withdraw")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("withdrawn"));
  }

  @Test
  @DisplayName("Should not allow withdrawing an order that has already been picked up")
  void shouldNotAllowWithdrawingPickedUpOrder() throws Exception {
    // Given
    String userToken = authenticateAsEndUser("pickup_withdraw_user");
    String droneToken = authenticateAsDrone("Drone-Withdraw-1");

    // Enduser creates an order
    CreateOrderRequest createRequest =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(userToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andReturn();

    OrderDto createdOrder =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderDto.class);

    // Drone reserves and picks up the order
    mockMvc.perform(
        post("/api/drones/jobs/" + createdOrder.getId() + "/reserve")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)));
    mockMvc.perform(
        post("/api/drones/jobs/" + createdOrder.getId() + "/pickup")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)));

    // When & Then - Enduser can no longer withdraw it
    mockMvc
        .perform(
            post("/api/orders/" + createdOrder.getId() + "/withdraw")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(userToken)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "Order cannot be withdrawn once it has been picked up or reached a terminal state"));
  }

  @Test
  @DisplayName("Should not allow user to withdraw another user's order")
  void shouldNotAllowWithdrawingOtherUsersOrder() throws Exception {
    // Given
    String user1Token = authenticateAsEndUser("user1");
    String user2Token = authenticateAsEndUser("user2");

    // User1 creates an order
    CreateOrderRequest createRequest =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(user1Token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andReturn();

    OrderDto createdOrder =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderDto.class);

    // When & Then - User2 tries to withdraw user1's order
    mockMvc
        .perform(
            post("/api/orders/" + createdOrder.getId() + "/withdraw")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(user2Token)))
        .andExpect(status().isForbidden());
  }
}
