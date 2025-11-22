package com.acme.services.dronedelivery.controllers.impl;

import com.acme.services.dronedelivery.controllers.AuthControllerV1;
import com.acme.services.dronedelivery.model.dto.AuthRequest;
import com.acme.services.dronedelivery.model.dto.AuthResponse;
import com.acme.services.dronedelivery.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerV1 {

  private final AuthService authService;

  @Override
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest request) {
    return ok(authService.authenticate(request));
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
