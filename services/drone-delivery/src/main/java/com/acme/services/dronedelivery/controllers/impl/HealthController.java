package com.acme.services.dronedelivery.controllers.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

  @GetMapping
  @Operation(summary = "Root endpoint", description = "Returns basic service information")
  public ResponseEntity<Map<String, String>> root() {
    Map<String, String> response = new HashMap<>();
    response.put("service", "Drone Delivery Management Service");
    response.put("status", "running");
    response.put("version", "0.0.1");
    return ResponseEntity.ok(response);
  }
}
