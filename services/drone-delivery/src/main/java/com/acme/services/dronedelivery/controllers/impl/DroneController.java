package com.acme.services.dronedelivery.controllers.impl;

import com.acme.services.dronedelivery.controllers.DroneControllerV1;
import com.acme.services.dronedelivery.model.dto.DroneDto;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import com.acme.services.dronedelivery.service.DroneService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
public class DroneController implements DroneControllerV1 {

  private final DroneService droneService;

  @Override
  @GetMapping("/jobs")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<PagedModel<OrderDto>> getAvailableJobs(Pageable pageable) {
    return ok(new PagedModel<>(droneService.getAvailableJobs(pageable)));
  }

  @Override
  @PostMapping("/jobs/{orderId}/reserve")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<OrderDto> reserveJob(@PathVariable Long orderId, Principal principal) {
    return ok(droneService.reserveJob(getDroneName(principal), orderId));
  }

  @Override
  @PostMapping("/jobs/{orderId}/pickup")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<OrderDto> pickupOrder(@PathVariable Long orderId, Principal principal) {
    return ok(droneService.pickupOrder(getDroneName(principal), orderId));
  }

  @Override
  @PostMapping("/jobs/{orderId}/deliver")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<OrderDto> deliverOrder(@PathVariable Long orderId, Principal principal) {
    return ok(droneService.deliverOrder(getDroneName(principal), orderId));
  }

  @Override
  @PostMapping("/jobs/{orderId}/fail")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<OrderDto> failOrder(@PathVariable Long orderId, Principal principal) {
    return ok(droneService.failOrder(getDroneName(principal), orderId));
  }

  @Override
  @PutMapping("/location")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<DroneDto> updateLocation(
      @Valid @RequestBody UpdateLocationRequest request, Principal principal) {
    return ok(droneService.updateLocation(getDroneName(principal), request));
  }

  @Override
  @GetMapping("/current-order")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<OrderDto> getCurrentOrder(Principal principal) {
    return ok(droneService.getCurrentOrder(getDroneName(principal)));
  }

  @Override
  @PostMapping("/broken")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<DroneDto> markAsBroken(Principal principal) {
    return ok(droneService.markAsBroken(getDroneName(principal)));
  }

  @Override
  @PostMapping("/fixed")
  @PreAuthorize("hasRole('DRONE')")
  public ResponseEntity<DroneDto> markAsFixed(Principal principal) {
    return ok(droneService.markAsFixed(getDroneName(principal)));
  }

  @Override
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedModel<DroneDto>> getAllDrones(Pageable pageable) {
    return ok(new PagedModel<>(droneService.getAllDrones(pageable)));
  }

  @Override
  @GetMapping("/{name}")
  @PreAuthorize("hasAnyRole('ADMIN', 'DRONE')")
  public ResponseEntity<DroneDto> getDroneByName(@PathVariable String name) {
    return ok(droneService.getDroneByName(name));
  }

  @Override
  @PostMapping("/{name}/broken")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<DroneDto> markDroneAsBroken(@PathVariable String name) {
    return ok(droneService.markAsBroken(name));
  }

  @Override
  @PostMapping("/{name}/fixed")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<DroneDto> markDroneAsFixed(@PathVariable String name) {
    return ok(droneService.markAsFixed(name));
  }

  /**
   * Helper method to extract drone name from authenticated principal.
   *
   * @param principal the authenticated principal
   * @return the drone name
   */
  private String getDroneName(Principal principal) {
    return principal.getName();
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
