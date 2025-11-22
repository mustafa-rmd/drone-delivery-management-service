package com.acme.services.dronedelivery.model.dto;

import com.acme.services.dronedelivery.model.enums.OrderStatus;
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
@Schema(description = "Delivery order information")
public class OrderDto {

  @Schema(description = "Order ID", example = "1")
  private Long id;

  @Schema(description = "End user name", example = "john_doe")
  private String enduserName;

  @Schema(description = "Pickup location latitude", example = "40.7128")
  private BigDecimal originLatitude;

  @Schema(description = "Pickup location longitude", example = "-74.0060")
  private BigDecimal originLongitude;

  @Schema(description = "Delivery location latitude", example = "40.7589")
  private BigDecimal destinationLatitude;

  @Schema(description = "Delivery location longitude", example = "-73.9851")
  private BigDecimal destinationLongitude;

  @Schema(description = "Order status", example = "pending")
  private OrderStatus status;

  @Schema(description = "Assigned drone ID", example = "1")
  private Long droneId;

  @Schema(description = "Assigned drone name", example = "Drone-Alpha-1")
  private String droneName;

  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp")
  private LocalDateTime updatedAt;

  @Schema(description = "Pickup timestamp")
  private LocalDateTime pickedUpAt;

  @Schema(description = "Delivery timestamp")
  private LocalDateTime deliveredAt;
}
