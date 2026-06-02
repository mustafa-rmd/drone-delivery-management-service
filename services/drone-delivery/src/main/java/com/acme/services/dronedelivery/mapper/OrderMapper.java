package com.acme.services.dronedelivery.mapper;

import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.entity.Drone;
import com.acme.services.dronedelivery.model.entity.Order;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

  /** Mean Earth radius in metres, used for the great-circle (Haversine) distance. */
  double EARTH_RADIUS_METERS = 6_371_000d;

  /**
   * Assumed drone cruise speed in metres per second (~36 km/h). Used to turn the remaining
   * great-circle distance into a rough delivery ETA.
   */
  double ASSUMED_CRUISE_SPEED_MPS = 10d;

  @Mapping(source = "drone.id", target = "droneId")
  @Mapping(source = "drone.name", target = "droneName")
  @Mapping(source = "drone.latitude", target = "droneLatitude")
  @Mapping(source = "drone.longitude", target = "droneLongitude")
  @Mapping(target = "etaSeconds", expression = "java(estimateEtaSeconds(order))")
  OrderDto toDto(Order order);

  /**
   * Estimates the remaining time to delivery in seconds from the assigned drone's current position.
   *
   * <p>Before pickup the drone must still travel to the origin and then on to the destination;
   * after pickup only the leg to the destination remains. Returns {@code null} when there is no
   * assigned drone with a known location or the order is in a terminal state.
   *
   * @param order the order being mapped
   * @return estimated seconds until delivery, or {@code null} if it cannot be determined
   */
  default Long estimateEtaSeconds(Order order) {
    Drone drone = order.getDrone();
    if (drone == null || drone.getLatitude() == null || drone.getLongitude() == null) {
      return null;
    }

    double meters;
    switch (order.getStatus()) {
      case PICKED_UP, IN_TRANSIT ->
          meters =
              haversineMeters(
                  drone.getLatitude(),
                  drone.getLongitude(),
                  order.getDestinationLatitude(),
                  order.getDestinationLongitude());
      case PENDING, RESERVED ->
          meters =
              haversineMeters(
                      drone.getLatitude(),
                      drone.getLongitude(),
                      order.getOriginLatitude(),
                      order.getOriginLongitude())
                  + haversineMeters(
                      order.getOriginLatitude(),
                      order.getOriginLongitude(),
                      order.getDestinationLatitude(),
                      order.getDestinationLongitude());
      default -> {
        // DELIVERED, FAILED, WITHDRAWN — nothing left to estimate.
        return null;
      }
    }

    return Math.round(meters / ASSUMED_CRUISE_SPEED_MPS);
  }

  /**
   * Great-circle distance between two coordinates in metres using the Haversine formula.
   *
   * @return the distance in metres, or {@code 0} if any coordinate is null
   */
  default double haversineMeters(
      BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
    if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
      return 0d;
    }

    double lat1Rad = Math.toRadians(lat1.doubleValue());
    double lat2Rad = Math.toRadians(lat2.doubleValue());
    double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
    double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

    double a =
        Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_METERS * c;
  }
}
