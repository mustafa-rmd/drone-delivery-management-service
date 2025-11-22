package com.acme.services.dronedelivery.service;

import com.acme.services.dronedelivery.model.dto.AuthRequest;
import com.acme.services.dronedelivery.model.dto.AuthResponse;

public interface AuthService {

  AuthResponse authenticate(AuthRequest request);
}
