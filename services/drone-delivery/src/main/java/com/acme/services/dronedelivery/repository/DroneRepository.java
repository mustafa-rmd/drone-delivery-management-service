package com.acme.services.dronedelivery.repository;

import com.acme.services.dronedelivery.model.entity.Drone;
import com.acme.services.dronedelivery.model.enums.DroneStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DroneRepository
    extends JpaRepository<Drone, Long>, JpaSpecificationExecutor<Drone> {

  Optional<Drone> findByName(String name);

  List<Drone> findByStatus(DroneStatus status);

  List<Drone> findByStatusAndIsBroken(DroneStatus status, Boolean isBroken);
}
