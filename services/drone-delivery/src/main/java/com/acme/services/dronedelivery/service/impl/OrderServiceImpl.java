package com.acme.services.dronedelivery.service.impl;

import com.acme.services.dronedelivery.exception.InvalidOrderStateException;
import com.acme.services.dronedelivery.exception.OrderNotFoundException;
import com.acme.services.dronedelivery.exception.UnauthorizedAccessException;
import com.acme.services.dronedelivery.mapper.OrderMapper;
import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import com.acme.services.dronedelivery.model.entity.Order;
import com.acme.services.dronedelivery.model.enums.OrderStatus;
import com.acme.services.dronedelivery.repository.OrderRepository;
import com.acme.services.dronedelivery.service.OrderService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  @Override
  @Transactional
  public OrderDto createOrder(String enduserName, CreateOrderRequest request) {
    log.info("Creating order for user: {}", enduserName);

    Order order =
        Order.builder()
            .enduserName(enduserName)
            .originLatitude(request.getOriginLatitude())
            .originLongitude(request.getOriginLongitude())
            .destinationLatitude(request.getDestinationLatitude())
            .destinationLongitude(request.getDestinationLongitude())
            .status(OrderStatus.PENDING)
            .build();

    order = orderRepository.save(order);
    log.info("Order created with ID: {}", order.getId());

    return orderMapper.toDto(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderDto> getMyOrders(String enduserName, Pageable pageable) {
    log.info(
        "Fetching orders for user: {} with pagination: page={}, size={}",
        enduserName,
        pageable.getPageNumber(),
        pageable.getPageSize());
    Page<Order> orders = orderRepository.findByEnduserName(enduserName, pageable);
    return orders.map(orderMapper::toDto);
  }

  @Override
  @Transactional
  public OrderDto withdrawOrder(String enduserName, Long orderId) {
    log.info("User {} attempting to withdraw order {}", enduserName, orderId);

    Order order = findOrderByIdOrThrow(orderId);
    validateOrderOwnership(order, enduserName);
    // An order may only be withdrawn before a drone has picked it up.
    validateOrderStatus(
        order,
        new OrderStatus[] {OrderStatus.PENDING, OrderStatus.RESERVED},
        "Order cannot be withdrawn once it has been picked up or reached a terminal state");

    order.setStatus(OrderStatus.WITHDRAWN);
    orderRepository.save(order);

    log.info("Order {} withdrawn by user {}", orderId, enduserName);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderDto> getAllOrders(Pageable pageable) {
    log.info(
        "Fetching all orders with pagination: page={}, size={}",
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<Order> orders = orderRepository.findAllWithDrone(pageable);
    return orders.map(orderMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderDto getOrderById(Long orderId) {
    log.info("Fetching order by ID: {}", orderId);
    Order order = findOrderByIdOrThrow(orderId);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderDto getOrderById(String username, Long orderId) {
    log.info("User {} attempting to fetch order {}", username, orderId);
    Order order = findOrderByIdOrThrow(orderId);
    validateOrderOwnership(order, username);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public OrderDto updateOrigin(Long orderId, UpdateLocationRequest request) {
    log.info("Admin updating origin for order {}", orderId);

    Order order = findOrderByIdOrThrow(orderId);
    validateOrderStatus(
        order,
        new OrderStatus[] {OrderStatus.PENDING, OrderStatus.RESERVED},
        "Cannot update origin - order already picked up or in terminal state");

    order.setOriginLatitude(request.getLatitude());
    order.setOriginLongitude(request.getLongitude());
    orderRepository.save(order);

    log.info("Origin updated for order {}", orderId);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public OrderDto updateDestination(Long orderId, UpdateLocationRequest request) {
    log.info("Admin updating destination for order {}", orderId);

    Order order = findOrderByIdOrThrow(orderId);
    validateOrderStatusNot(
        order,
        new OrderStatus[] {OrderStatus.DELIVERED, OrderStatus.FAILED, OrderStatus.WITHDRAWN},
        "Cannot update destination - order in terminal state");

    order.setDestinationLatitude(request.getLatitude());
    order.setDestinationLongitude(request.getLongitude());
    orderRepository.save(order);

    log.info("Destination updated for order {}", orderId);
    return orderMapper.toDto(order);
  }

  // ==================== Helper Methods ====================

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
   * Validates that the order belongs to the specified user.
   *
   * @param order the order to validate
   * @param enduserName the expected owner
   * @throws UnauthorizedAccessException if order doesn't belong to user
   */
  private void validateOrderOwnership(Order order, String enduserName) {
    if (!order.getEnduserName().equals(enduserName)) {
      throw new UnauthorizedAccessException("Order does not belong to this user");
    }
  }

  /**
   * Validates that the order status is one of the allowed statuses.
   *
   * @param order the order to validate
   * @param allowedStatuses the allowed statuses
   * @param errorMessage the error message if validation fails
   * @throws InvalidOrderStateException if order status is not allowed
   */
  private void validateOrderStatus(
      Order order, OrderStatus[] allowedStatuses, String errorMessage) {
    boolean isAllowed =
        Arrays.stream(allowedStatuses).anyMatch(status -> status == order.getStatus());
    if (!isAllowed) {
      throw new InvalidOrderStateException(errorMessage);
    }
  }

  /**
   * Validates that the order status is NOT one of the forbidden statuses.
   *
   * @param order the order to validate
   * @param forbiddenStatuses the forbidden statuses
   * @param errorMessage the error message if validation fails
   * @throws InvalidOrderStateException if order status is forbidden
   */
  private void validateOrderStatusNot(
      Order order, OrderStatus[] forbiddenStatuses, String errorMessage) {
    boolean isForbidden =
        Arrays.stream(forbiddenStatuses).anyMatch(status -> status == order.getStatus());
    if (isForbidden) {
      throw new InvalidOrderStateException(errorMessage);
    }
  }
}
