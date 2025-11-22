package com.acme.services.dronedelivery.mapper;

import com.acme.services.dronedelivery.model.dto.DroneDto;
import com.acme.services.dronedelivery.model.entity.Drone;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DroneMapper {

  DroneDto toDto(Drone drone);
}
