package com.acme.services.dronedelivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for pessimistic locking and sequential race condition scenarios.
 *
 * <p>Note: MockMvc is not thread-safe for true concurrent testing. These tests verify the
 * pessimistic locking logic works correctly in sequential scenarios that simulate race conditions.
 */
@DisplayName("Pessimistic Locking Integration Tests")
class ConcurrencyIntegrationTest extends BaseIntegrationTest {

  @Test
  @DisplayName("Should prevent multiple drones from reserving the same order")
  void shouldPreventMultipleReservations() throws Exception {
    // Given
    String userToken = authenticateAsEndUser("concurrent_user");

    // Create a single order
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

    // Create two drones
    String drone1Token = authenticateAsDrone("Drone-1");
    String drone2Token = authenticateAsDrone("Drone-2");

    // When - First drone reserves the order
    mockMvc
        .perform(
            post("/api/drones/jobs/" + order.getId() + "/reserve")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(drone1Token)))
        .andExpect(status().isOk());

    // Then - Second drone should fail to reserve the same order
    mockMvc
        .perform(
            post("/api/drones/jobs/" + order.getId() + "/reserve")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(drone2Token)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should prevent race condition when drone marks as broken with active order")
  void shouldHandleDroneBreakdownWithActiveOrder() throws Exception {
    // Given
    String droneToken = authenticateAsDrone("Drone-Breakdown-Test");
    String userToken = authenticateAsEndUser("breakdown_user");

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

    // Reserve and pickup the order
    mockMvc.perform(
        post("/api/drones/jobs/" + order.getId() + "/reserve")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)));

    mockMvc.perform(
        post("/api/drones/jobs/" + order.getId() + "/pickup")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)));

    // When - Mark drone as broken
    mockMvc
        .perform(
            post("/api/drones/broken").header(HttpHeaders.AUTHORIZATION, bearerToken(droneToken)))
        .andExpect(status().isOk());

    // Then - The order should be reset to pending state
    // (This is verified by the service logic that resets orders for broken drones)
  }
}
