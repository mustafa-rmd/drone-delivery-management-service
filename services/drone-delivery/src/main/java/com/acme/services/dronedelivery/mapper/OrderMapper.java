package com.acme.services.dronedelivery.mapper;

import com.acme.services.dronedelivery.model.dto.OrderDto;
import com.acme.services.dronedelivery.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

  @Mapping(source = "drone.id", target = "droneId")
  @Mapping(source = "drone.name", target = "droneName")
  OrderDto toDto(Order order);
}
