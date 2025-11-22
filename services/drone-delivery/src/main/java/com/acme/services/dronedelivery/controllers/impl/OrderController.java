package com.acme.services.dronedelivery.controllers.impl;

import com.acme.services.dronedelivery.controllers.OrderControllerV1;
import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import com.acme.services.dronedelivery.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerV1 {

  private final OrderService orderService;

  @Override
  @PostMapping
  @PreAuthorize("hasRole('ENDUSER')")
  public ResponseEntity<OrderDto> createOrder(
      @Valid @RequestBody CreateOrderRequest request, Principal principal) {
    return ok(orderService.createOrder(getEnduserName(principal), request));
  }

  @Override
  @GetMapping("/my-orders")
  @PreAuthorize("hasRole('ENDUSER')")
  public ResponseEntity<Page<OrderDto>> getMyOrders(Principal principal, Pageable pageable) {
    return ok(orderService.getMyOrders(getEnduserName(principal), pageable));
  }

  @Override
  @PostMapping("/{orderId}/withdraw")
  @PreAuthorize("hasRole('ENDUSER')")
  public ResponseEntity<OrderDto> withdrawOrder(@PathVariable Long orderId, Principal principal) {
    return ok(orderService.withdrawOrder(getEnduserName(principal), orderId));
  }

  @Override
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<OrderDto>> getAllOrders(Pageable pageable) {
    return ok(orderService.getAllOrders(pageable));
  }

  @Override
  @GetMapping("/{orderId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'ENDUSER')")
  public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId, Principal principal) {
    // Check if user has ADMIN role
    if (hasRole("ADMIN")) {
      // Admin can view any order
      return ok(orderService.getOrderById(orderId));
    } else {
      // Enduser can only view their own orders
      return ok(orderService.getOrderById(getEnduserName(principal), orderId));
    }
  }

  @Override
  @PutMapping("/{orderId}/origin")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<OrderDto> updateOrigin(
      @PathVariable Long orderId, @Valid @RequestBody UpdateLocationRequest request) {
    return ok(orderService.updateOrigin(orderId, request));
  }

  @Override
  @PutMapping("/{orderId}/destination")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<OrderDto> updateDestination(
      @PathVariable Long orderId, @Valid @RequestBody UpdateLocationRequest request) {
    return ok(orderService.updateDestination(orderId, request));
  }

  /**
   * Helper method to extract enduser name from authenticated principal.
   *
   * @param principal the authenticated principal
   * @return the enduser name
   */
  private String getEnduserName(Principal principal) {
    return principal.getName();
  }

  /**
   * Helper method to check if current user has a specific role.
   *
   * @param role the role to check (without ROLE_ prefix)
   * @return true if user has the role, false otherwise
   */
  private boolean hasRole(String role) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
  }

  /**
   * Helper method to wrap response in ResponseEntity.ok().
   *
   * @param body the response body
   * @param <T> the type of response body
   * @return ResponseEntity with 200 OK status
   */
  private <T> ResponseEntity<T> ok(T body) {
    return ResponseEntity.ok(body);
  }
}
