package com.acme.services.dronedelivery.controllers;

import com.acme.services.dronedelivery.model.dto.AuthRequest;
import com.acme.services.dronedelivery.model.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "JWT authentication endpoints")
public interface AuthControllerV1 {

  @Operation(
      summary = "Authenticate user",
      description = "Authenticate user and receive JWT token. Creates user if not exists.")
  ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request);
}
