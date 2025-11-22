package com.acme.services.dronedelivery.service.impl;

import com.acme.services.dronedelivery.exception.DroneNotFoundException;
import com.acme.services.dronedelivery.exception.InvalidDroneStateException;
import com.acme.services.dronedelivery.exception.InvalidOrderStateException;
import com.acme.services.dronedelivery.exception.OrderAssignmentException;
import com.acme.services.dronedelivery.exception.OrderNotFoundException;
import com.acme.services.dronedelivery.mapper.DroneMapper;
import com.acme.services.dronedelivery.mapper.OrderMapper;
import com.acme.services.dronedelivery.model.dto.DroneDto;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import com.acme.services.dronedelivery.model.entity.Drone;
import com.acme.services.dronedelivery.model.entity.Order;
import com.acme.services.dronedelivery.model.enums.DroneStatus;
import com.acme.services.dronedelivery.model.enums.OrderStatus;
import com.acme.services.dronedelivery.repository.DroneRepository;
import com.acme.services.dronedelivery.repository.OrderRepository;
import com.acme.services.dronedelivery.service.DroneService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DroneServiceImpl implements DroneService {

  private final DroneRepository droneRepository;
  private final OrderRepository orderRepository;
  private final DroneMapper droneMapper;
  private final OrderMapper orderMapper;

  @Override
  @Transactional(readOnly = true)
  public Page<OrderDto> getAvailableJobs(Pageable pageable) {
    log.info(
        "Fetching available jobs with pagination: page={}, size={}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING, pageable);
    return pendingOrders.map(orderMapper::toDto);
  }

  @Override
  @Transactional
  public OrderDto reserveJob(String droneName, Long orderId) {
    log.info("Drone {} attempting to reserve order {}", droneName, orderId);

    Drone drone = findDroneByNameOrThrow(droneName);
    validateDroneAvailableForReservation(drone);

    // Use pessimistic locking to prevent race conditions when multiple drones
    // attempt to reserve the same order simultaneously
    Order order = findOrderByIdForUpdateOrThrow(orderId);
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new InvalidOrderStateException("Order is not available for reservation");
    }

    // Reserve the order
    order.setStatus(OrderStatus.RESERVED);
    order.setDrone(drone);
    orderRepository.save(order);

    // Update drone status
    drone.setStatus(DroneStatus.BUSY);
    droneRepository.save(drone);

    log.info("Order {} reserved by drone {}", orderId, droneName);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public OrderDto pickupOrder(String droneName, Long orderId) {
    log.info("Drone {} attempting to pickup order {}", droneName, orderId);

    Drone drone = findDroneByNameOrThrow(droneName);
    Order order = findOrderByIdOrThrow(orderId);

    validateOrderAssignedToDrone(order, drone);

    if (order.getStatus() != OrderStatus.RESERVED) {
      throw new InvalidOrderStateException("Order must be in RESERVED status to pickup");
    }

    // Mark as picked up
    order.setStatus(OrderStatus.PICKED_UP);
    order.setPickedUpAt(LocalDateTime.now());
    orderRepository.save(order);

    log.info("Order {} picked up by drone {}", orderId, droneName);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public OrderDto deliverOrder(String droneName, Long orderId) {
    log.info("Drone {} attempting to deliver order {}", droneName, orderId);

    Drone drone = findDroneByNameOrThrow(droneName);
    Order order = findOrderByIdOrThrow(orderId);

    validateOrderAssignedToDrone(order, drone);

    if (order.getStatus() != OrderStatus.PICKED_UP) {
      throw new InvalidOrderStateException("Order must be in PICKED_UP status to deliver");
    }

    // Mark as delivered
    order.setStatus(OrderStatus.DELIVERED);
    order.setDeliveredAt(LocalDateTime.now());
    orderRepository.save(order);

    // Free up the drone
    drone.setStatus(DroneStatus.AVAILABLE);
    droneRepository.save(drone);

    log.info("Order {} delivered by drone {}", orderId, droneName);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public OrderDto failOrder(String droneName, Long orderId) {
    log.info("Drone {} attempting to mark order {} as failed", droneName, orderId);

    Drone drone = findDroneByNameOrThrow(droneName);
    Order order = findOrderByIdOrThrow(orderId);

    validateOrderAssignedToDrone(order, drone);

    if (order.getStatus() != OrderStatus.PICKED_UP) {
      throw new InvalidOrderStateException("Order must be in PICKED_UP status to mark as failed");
    }

    // Mark as failed
    order.setStatus(OrderStatus.FAILED);
    orderRepository.save(order);

    // Free up the drone
    drone.setStatus(DroneStatus.AVAILABLE);
    droneRepository.save(drone);

    log.info("Order {} marked as failed by drone {}", orderId, droneName);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public DroneDto updateLocation(String droneName, UpdateLocationRequest request) {
    log.info("Updating location for drone {}", droneName);

    Drone drone = findDroneByNameOrThrow(droneName);

    drone.setLatitude(request.getLatitude());
    drone.setLongitude(request.getLongitude());
    droneRepository.save(drone);

    log.info("Location updated for drone {}", droneName);
    return droneMapper.toDto(drone);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderDto getCurrentOrder(String droneName) {
    log.info("Fetching current order for drone {}", droneName);

    Drone drone = findDroneByNameOrThrow(droneName);

    Order order =
        orderRepository
            .findCurrentOrderByDroneId(drone.getId())
            .orElseThrow(
                () -> new OrderNotFoundException("No active order found for drone: " + droneName));

    log.info("Found current order {} for drone {}", order.getId(), droneName);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public DroneDto markAsBroken(String droneName) {
    log.info("Marking drone {} as broken", droneName);

    Drone drone = findDroneByNameOrThrow(droneName);

    drone.setIsBroken(true);
    drone.setStatus(DroneStatus.BROKEN);

    int affectedOrders =
        orderRepository.resetOrdersForBrokenDrone(
            drone.getId(), drone.getLatitude(), drone.getLongitude());

    if (affectedOrders > 0) {
      log.info(
          "{} order(s) reset to PENDING for broken drone {} (picked-up orders relocated to drone's last position)",
          affectedOrders,
          droneName);
    }

    droneRepository.save(drone);
    log.info("Drone {} marked as broken", droneName);
    return droneMapper.toDto(drone);
  }

  @Override
  @Transactional
  public DroneDto markAsFixed(String droneName) {
    log.info("Marking drone {} as fixed", droneName);

    Drone drone = findDroneByNameOrThrow(droneName);

    drone.setIsBroken(false);
    drone.setStatus(DroneStatus.AVAILABLE);
    droneRepository.save(drone);

    log.info("Drone {} marked as fixed", droneName);
    return droneMapper.toDto(drone);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<DroneDto> getAllDrones(Pageable pageable) {
    log.info(
        "Fetching all drones with pagination: page={}, size={}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<Drone> drones = droneRepository.findAll(pageable);
    return drones.map(droneMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public DroneDto getDroneByName(String name) {
    log.info("Fetching drone by name: {}", name);
    Drone drone = findDroneByNameOrThrow(name);
    return droneMapper.toDto(drone);
  }

  // ==================== Helper Methods ====================

  /**
   * Finds a drone by name or throws DroneNotFoundException.
   *
   * @param droneName the name of the drone
   * @return the found drone
   * @throws DroneNotFoundException if drone not found
   */
  private Drone findDroneByNameOrThrow(String droneName) {
    return droneRepository
        .findByName(droneName)
        .orElseThrow(() -> new DroneNotFoundException(droneName));
  }

  /**
   * Finds an order by ID or throws OrderNotFoundException.
   *
   * @param orderId the order ID
   * @return the found order
   * @throws OrderNotFoundException if order not found
   */
  private Order findOrderByIdOrThrow(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  /**
   * Finds an order by ID with pessimistic write lock or throws OrderNotFoundException. This method
   * acquires a database-level lock on the order row, preventing concurrent modifications by other
   * transactions. The lock is held until the transaction commits or rolls back.
   *
   * @param orderId the order ID
   * @return the found order with pessimistic lock
   * @throws OrderNotFoundException if order not found
   */
  private Order findOrderByIdForUpdateOrThrow(Long orderId) {
    return orderRepository
        .findByIdForUpdate(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  /**
   * Validates that the drone is available for job reservation.
   *
   * @param drone the drone to validate
   * @throws InvalidDroneStateException if drone is broken or not available
   */
  private void validateDroneAvailableForReservation(Drone drone) {
    if (Boolean.TRUE.equals(drone.getIsBroken())) {
      throw new InvalidDroneStateException("Drone is broken and cannot reserve jobs");
    }
    if (drone.getStatus() == DroneStatus.BUSY) {
      throw new InvalidDroneStateException("Drone already has an active order");
    }
    if (drone.getStatus() != DroneStatus.AVAILABLE) {
      throw new InvalidDroneStateException("Drone is not available");
    }
  }

  /**
   * Validates that the order is assigned to the specified drone.
   *
   * @param order the order to validate
   * @param drone the drone that should be assigned
   * @throws OrderAssignmentException if order is not assigned to the drone
   */
  private void validateOrderAssignedToDrone(Order order, Drone drone) {
    if (order.getDrone() == null || !order.getDrone().getId().equals(drone.getId())) {
      throw new OrderAssignmentException("Order is not assigned to this drone");
    }
  }
}
