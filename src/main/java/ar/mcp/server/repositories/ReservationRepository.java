package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for accessing and managing Reservation entities.
 * <p>
 * Extends JpaRepository to provide basic CRUD operations and query derivation capabilities.
 * Custom queries are defined to support more specific filtering based on reservation attributes.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

}
