package com.acme.services.dronedelivery.controllers;

import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
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

@Tag(name = "Order Management", description = "Endpoints for managing delivery orders")
@SecurityRequirement(name = "Bearer Authentication")
public interface OrderControllerV1 {

  @Operation(summary = "Create order", description = "Create a new delivery order")
  ResponseEntity<OrderDto> createOrder(
      @Valid @RequestBody CreateOrderRequest request, Principal principal);

  @Operation(
      summary = "Get my orders",
      description = "Get paginated list of orders for the current user")
  ResponseEntity<PagedModel<OrderDto>> getMyOrders(
      Principal principal,
      @Parameter(description = "Pagination parameters (page, size, sort)")
          @PageableDefault(size = 20, sort = "createdAt")
          Pageable pageable);

  @Operation(summary = "Withdraw order", description = "Withdraw a pending order")
  ResponseEntity<OrderDto> withdrawOrder(@PathVariable Long orderId, Principal principal);

  @Operation(
      summary = "Get all orders",
      description = "Get paginated list of all orders (Admin only)")
  ResponseEntity<PagedModel<OrderDto>> getAllOrders(
      @Parameter(description = "Pagination parameters (page, size, sort)")
          @PageableDefault(size = 20, sort = "createdAt")
          Pageable pageable);

  @Operation(
      summary = "Get order by ID",
      description =
          "Get order details by ID. Admin can view any order, enduser can only view their own orders")
  ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId, Principal principal);

  @Operation(
      summary = "Update order origin",
      description = "Admin updates order origin coordinates")
  ResponseEntity<OrderDto> updateOrigin(
      @PathVariable Long orderId, @Valid @RequestBody UpdateLocationRequest request);

  @Operation(
      summary = "Update order destination",
      description = "Admin updates order destination coordinates")
  ResponseEntity<OrderDto> updateDestination(
      @PathVariable Long orderId, @Valid @RequestBody UpdateLocationRequest request);
}
