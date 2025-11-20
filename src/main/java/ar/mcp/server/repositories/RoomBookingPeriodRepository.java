package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.RoomBookingPeriod;
import ar.mcp.server.domain.enums.RoomBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for accessing and managing {@link RoomBookingPeriod} entities.
 *
 * <p>Extends {@link org.springframework.data.jpa.repository.JpaRepository} for standard
 * CRUD operations and {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor}
 * to support dynamic queries using Specifications.</p>
 */
@Repository
public interface RoomBookingPeriodRepository extends JpaRepository<RoomBookingPeriod, Long>, JpaSpecificationExecutor<RoomBookingPeriod> {

}
