package com.acme.services.dronedelivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import com.acme.services.dronedelivery.model.enums.OrderStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/** Integration tests for drone operations endpoints. */
@DisplayName("Drone Operations Integration Tests")
class DroneIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should get available jobs as drone")
  void shouldGetAvailableJobsAsDrone() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Beta-1");
    String userToken = authenticateAsEndUser("order_user");

    // Create a pending order
    CreateOrderRequest createRequest =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    mockMvc.perform(
        post("/api/orders")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(userToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)));

    // When & Then
    mockMvc
        .perform(
            get("/api/drones/jobs")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken))
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].status").value("pending"));
  }

  @Test
  @DisplayName("Should reserve job as drone")
  void shouldReserveJobAsDrone() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Gamma-1");
    String userToken = authenticateAsEndUser("reserve_user");

    // Create a pending order
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

    // When & Then - Reserve the order
    mockMvc
        .perform(
            post("/api/drones/jobs/" + createdOrder.getId() + "/reserve")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("reserved"))
        .andExpect(jsonPath("$.droneName").value("Drone-Gamma-1"));
  }

  @Test
  @DisplayName("Should complete full delivery workflow")
  void shouldCompleteFullDeliveryWorkflow() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Delta-1");
    String userToken = authenticateAsEndUser("workflow_user");

    // Step 1: Create order
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

    OrderDto order =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderDto.class);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

    // Step 2: Reserve order
    MvcResult reserveResult =
        mockMvc
            .perform(
                post("/api/drones/jobs/" + order.getId() + "/reserve")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
            .andExpect(status().isOk())
            .andReturn();

    order =
        objectMapper.readValue(reserveResult.getResponse().getContentAsString(), OrderDto.class);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.RESERVED);

    // Step 3: Pickup order
    MvcResult pickupResult =
        mockMvc
            .perform(
                post("/api/drones/jobs/" + order.getId() + "/pickup")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
            .andExpect(status().isOk())
            .andReturn();

    order = objectMapper.readValue(pickupResult.getResponse().getContentAsString(), OrderDto.class);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PICKED_UP);
    assertThat(order.getPickedUpAt()).isNotNull();

    // Step 4: Deliver order
    MvcResult deliverResult =
        mockMvc
            .perform(
                post("/api/drones/jobs/" + order.getId() + "/deliver")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
            .andExpect(status().isOk())
            .andReturn();

    order =
        objectMapper.readValue(deliverResult.getResponse().getContentAsString(), OrderDto.class);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    assertThat(order.getDeliveredAt()).isNotNull();
  }

  @Test
  @DisplayName("Should update drone location")
  void shouldUpdateDroneLocation() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Epsilon-1");
    UpdateLocationRequest locationRequest =
        UpdateLocationRequest.builder()
            .latitude(new BigDecimal("40.7589"))
            .longitude(new BigDecimal("-73.9851"))
            .build();

    // When & Then
    mockMvc
        .perform(
            put("/api/drones/location")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latitude").value(40.7589))
        .andExpect(jsonPath("$.longitude").value(-73.9851));
  }

  @Test
  @DisplayName("Should mark drone as broken")
  void shouldMarkDroneAsBroken() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Zeta-1");

    // When & Then
    mockMvc
        .perform(
            post("/api/drones/broken").header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isBroken").value(true))
        .andExpect(jsonPath("$.status").value("broken"));
  }

  @Test
  @DisplayName("Should prevent drone from reserving multiple jobs")
  void shouldPreventMultipleJobReservations() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Eta-1");
    String userToken = authenticateAsEndUser("multi_order_user");

    // Create two orders
    CreateOrderRequest createRequest1 =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7128"))
            .originLongitude(new BigDecimal("-74.0060"))
            .destinationLatitude(new BigDecimal("40.7589"))
            .destinationLongitude(new BigDecimal("-73.9851"))
            .build();

    CreateOrderRequest createRequest2 =
        CreateOrderRequest.builder()
            .originLatitude(new BigDecimal("40.7589"))
            .originLongitude(new BigDecimal("-73.9851"))
            .destinationLatitude(new BigDecimal("40.7128"))
            .destinationLongitude(new BigDecimal("-74.0060"))
            .build();

    MvcResult result1 =
        mockMvc
            .perform(
                post("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(userToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest1)))
            .andReturn();

    MvcResult result2 =
        mockMvc
            .perform(
                post("/api/orders")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken(userToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest2)))
            .andReturn();

    OrderDto order1 =
        objectMapper.readValue(result1.getResponse().getContentAsString(), OrderDto.class);
    OrderDto order2 =
        objectMapper.readValue(result2.getResponse().getContentAsString(), OrderDto.class);

    // Reserve first order
    mockMvc
        .perform(
            post("/api/drones/jobs/" + order1.getId() + "/reserve")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
        .andExpect(status().isOk());

    // Try to reserve second order - should fail
    mockMvc
        .perform(
            post("/api/drones/jobs/" + order2.getId() + "/reserve")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Drone already has an active order"));
  }

  @Test
  @DisplayName("Should fail order as drone")
  void shouldFailOrderAsDrone() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Theta-1");
    String userToken = authenticateAsEndUser("fail_order_user");

    // Create and reserve an order
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

    OrderDto order =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), OrderDto.class);

    // Reserve the order
    mockMvc.perform(
        post("/api/drones/jobs/" + order.getId() + "/reserve")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)));

    // Pickup the order
    mockMvc.perform(
        post("/api/drones/jobs/" + order.getId() + "/pickup")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)));

    // When & Then - Fail the order
    mockMvc
        .perform(
            post("/api/drones/jobs/" + order.getId() + "/fail")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("failed"));
  }

  @Test
  @DisplayName("Should reject non-drone user from accessing drone endpoints")
  void shouldRejectNonDroneUserFromDroneEndpoints() throws Exception {
    // Given
    String userToken = authenticateAsEndUser("regular_user");

    // When & Then
    mockMvc
        .perform(get("/api/drones/jobs").header(HttpHeaders.AUTHORIZATION, bearerToken(userToken)))
        .andExpect(status().isForbidden());
  }
}
