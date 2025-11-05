package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByNumberOfPeople(int people);
    List<Reservation> findByNumberOfNights(int nights);
    List<Reservation> findByNumberOfPeopleAndNumberOfNights(int people, int nights);
    List<Reservation> findByStartAtGreaterThan(LocalDate start);
    List<Reservation> findByEndAtLessThan(LocalDate end);
    List<Reservation> findByStartAtGreaterThanAndEndAtLessThan(LocalDate start, LocalDate end);
    @Query("SELECT r FROM Reservation r WHERE r.roomBooked = :roomId")
    List<Reservation> findByRoom(Long roomId);
    @Query("SELECT r FROM Reservation r WHERE r.client = :personId")
    List<Reservation> findByPerson(Long personId);

}
