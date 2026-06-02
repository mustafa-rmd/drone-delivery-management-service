package com.acme.services.dronedelivery.controllers;

import com.acme.services.dronedelivery.model.dto.DroneDto;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Drone Operations", description = "Endpoints for drone operations")
@SecurityRequirement(name = "Bearer Authentication")
public interface DroneControllerV1 {

  @Operation(
      summary = "Get available jobs",
      description = "Get paginated list of pending delivery orders")
  ResponseEntity<PagedModel<OrderDto>> getAvailableJobs(
      @Parameter(description = "Pagination parameters (page, size, sort)")
          @PageableDefault(size = 20, sort = "createdAt")
          Pageable pageable);

  @Operation(summary = "Reserve a job", description = "Reserve a delivery order for this drone")
  ResponseEntity<OrderDto> reserveJob(@PathVariable Long orderId, Principal principal);

  @Operation(summary = "Pickup order", description = "Mark order as picked up")
  ResponseEntity<OrderDto> pickupOrder(@PathVariable Long orderId, Principal principal);

  @Operation(summary = "Deliver order", description = "Mark order as delivered")
  ResponseEntity<OrderDto> deliverOrder(@PathVariable Long orderId, Principal principal);

  @Operation(summary = "Fail order", description = "Mark order as failed")
  ResponseEntity<OrderDto> failOrder(@PathVariable Long orderId, Principal principal);

  @Operation(summary = "Update location", description = "Update drone's current location")
  ResponseEntity<DroneDto> updateLocation(
      @Valid @RequestBody UpdateLocationRequest request, Principal principal);

  @Operation(
      summary = "Get current order",
      description = "Get details of the order currently assigned to this drone")
  ResponseEntity<OrderDto> getCurrentOrder(Principal principal);

  @Operation(summary = "Mark as broken", description = "Mark this drone as broken (self-service)")
  ResponseEntity<DroneDto> markAsBroken(Principal principal);

  @Operation(summary = "Mark as fixed", description = "Mark this drone as fixed")
  ResponseEntity<DroneDto> markAsFixed(Principal principal);

  @Operation(
      summary = "Get all drones",
      description = "Get paginated list of all drones (Admin only)")
  ResponseEntity<PagedModel<DroneDto>> getAllDrones(
      @Parameter(description = "Pagination parameters (page, size, sort)")
          @PageableDefault(size = 20, sort = "name")
          Pageable pageable);

  @Operation(summary = "Get drone by name", description = "Get drone information by name")
  ResponseEntity<DroneDto> getDroneByName(@PathVariable String name);

  @Operation(
      summary = "Mark drone as broken (Admin)",
      description = "Admin marks any drone as broken by name")
  ResponseEntity<DroneDto> markDroneAsBroken(@PathVariable String name);

  @Operation(
      summary = "Mark drone as fixed (Admin)",
      description = "Admin marks any drone as fixed by name")
  ResponseEntity<DroneDto> markDroneAsFixed(@PathVariable String name);
}
