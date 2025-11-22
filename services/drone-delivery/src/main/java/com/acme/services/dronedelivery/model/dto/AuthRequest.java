package com.acme.services.dronedelivery.model.dto;

import com.acme.services.dronedelivery.model.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication request")
public class AuthRequest {

  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
  @Pattern(
      regexp = "^[a-zA-Z0-9_-]+$",
      message = "Name must contain only alphanumeric characters, hyphens, and underscores")
  @Schema(
      description = "User name (alphanumeric, hyphens, underscores only)",
      example = "john_doe",
      minLength = 3,
      maxLength = 255)
  private String name;

  @NotNull(message = "User type is required")
  @Schema(
      description = "Type of user (admin, enduser, or drone)",
      example = "admin",
      allowableValues = {"admin", "enduser", "drone"})
  private UserType type;
}
