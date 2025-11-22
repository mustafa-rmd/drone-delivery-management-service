package com.acme.services.dronedelivery.model.dto;

import com.acme.services.dronedelivery.model.enums.DroneStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Drone information")
public class DroneDto {

  @Schema(description = "Drone ID", example = "1")
  private Long id;

  @Schema(description = "Drone name", example = "Drone-Alpha-1")
  private String name;

  @Schema(description = "Drone status", example = "available")
  private DroneStatus status;

  @Schema(description = "Current latitude", example = "40.7128")
  private BigDecimal latitude;

  @Schema(description = "Current longitude", example = "-74.0060")
  private BigDecimal longitude;

  @Schema(description = "Is drone broken", example = "false")
  private Boolean isBroken;

  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp")
  private LocalDateTime updatedAt;
}
