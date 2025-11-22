package com.acme.services.dronedelivery.service;

import com.acme.services.dronedelivery.model.dto.DroneDto;
import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.dto.UpdateLocationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DroneService {

  Page<OrderDto> getAvailableJobs(Pageable pageable);

  OrderDto reserveJob(String droneName, Long orderId);

  OrderDto pickupOrder(String droneName, Long orderId);

  OrderDto deliverOrder(String droneName, Long orderId);

  OrderDto failOrder(String droneName, Long orderId);

  DroneDto updateLocation(String droneName, UpdateLocationRequest request);

  OrderDto getCurrentOrder(String droneName);

  DroneDto markAsBroken(String droneName);

  DroneDto markAsFixed(String droneName);

  Page<DroneDto> getAllDrones(Pageable pageable);

  DroneDto getDroneByName(String name);
}
