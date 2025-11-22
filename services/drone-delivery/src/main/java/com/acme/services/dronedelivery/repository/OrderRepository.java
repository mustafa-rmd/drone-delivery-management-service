package com.acme.services.dronedelivery.repository;

import com.acme.services.dronedelivery.model.entity.Order;
import com.acme.services.dronedelivery.model.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

  /**
   * Find an order by ID with pessimistic write lock. This prevents race conditions when multiple
   * drones attempt to reserve the same order simultaneously. The lock is held until the transaction
   * commits.
   *
   * @param id the order ID
   * @return the order wrapped in Optional, or empty if not found
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.id = :id")
  Optional<Order> findByIdForUpdate(@Param("id") Long id);

  /**
   * Find orders by enduser name with eager loading of drone relationship to prevent N+1 queries.
   *
   * @param enduserName the enduser name
   * @param pageable pagination information
   * @return page of orders with drone information
   */
  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.drone WHERE o.enduserName = :enduserName")
  Page<Order> findByEnduserName(@Param("enduserName") String enduserName, Pageable pageable);

  /**
   * Find orders by status with eager loading of drone relationship to prevent N+1 queries.
   *
   * @param status the order status
   * @param pageable pagination information
   * @return page of orders with drone information
   */
  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.drone WHERE o.status = :status")
  Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

  /**
   * Find all orders with eager loading of drone relationship to prevent N+1 queries.
   *
   * @param pageable pagination information
   * @return page of orders with drone information
   */
  @Query("SELECT o FROM Order o LEFT JOIN FETCH o.drone")
  Page<Order> findAllWithDrone(Pageable pageable);

  /**
   * Find the current order assigned to a drone (RESERVED or PICKED_UP status).
   *
   * @param droneId the drone ID
   * @return the current order wrapped in Optional, or empty if no active order
   */
  @Query(
      "SELECT o FROM Order o WHERE o.drone.id = :droneId AND o.status IN ('RESERVED', 'PICKED_UP')")
  Optional<Order> findCurrentOrderByDroneId(@Param("droneId") Long droneId);

  /**
   * Bulk update: reset all PICKED_UP and RESERVED orders for a broken drone in a single query. Uses
   * CASE statement to conditionally update origin coordinates only for PICKED_UP orders.
   *
   * @param droneId the broken drone ID
   * @param latitude the broken drone's latitude (for PICKED_UP orders)
   * @param longitude the broken drone's longitude (for PICKED_UP orders)
   * @return number of orders updated
   */
  @Modifying
  @Query(
      "UPDATE Order o SET "
          + "o.status = 'PENDING', "
          + "o.originLatitude = CASE WHEN o.status = 'PICKED_UP' THEN :latitude ELSE o.originLatitude END, "
          + "o.originLongitude = CASE WHEN o.status = 'PICKED_UP' THEN :longitude ELSE o.originLongitude END, "
          + "o.pickedUpAt = CASE WHEN o.status = 'PICKED_UP' THEN NULL ELSE o.pickedUpAt END, "
          + "o.drone = NULL "
          + "WHERE o.drone.id = :droneId AND o.status IN ('PICKED_UP', 'RESERVED')")
  int resetOrdersForBrokenDrone(
      @Param("droneId") Long droneId,
      @Param("latitude") BigDecimal latitude,
      @Param("longitude") BigDecimal longitude);
}
