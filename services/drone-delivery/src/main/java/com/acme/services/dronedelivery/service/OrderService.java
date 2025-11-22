package com.acme.services.dronedelivery.service;

import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  OrderDto createOrder(String enduserName, CreateOrderRequest request);

  Page<OrderDto> getMyOrders(String enduserName, Pageable pageable);

  OrderDto withdrawOrder(String enduserName, Long orderId);

  Page<OrderDto> getAllOrders(Pageable pageable);

  OrderDto getOrderById(Long orderId);

  OrderDto getOrderById(String username, Long orderId);

  OrderDto updateOrigin(Long orderId, UpdateLocationRequest request);

  OrderDto updateDestination(Long orderId, UpdateLocationRequest request);
}
