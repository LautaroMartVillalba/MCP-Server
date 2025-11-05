package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.enums.BedsType;
import ar.mcp.server.domain.enums.RoomState;
import ar.mcp.server.domain.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
/**
 * Repository interface for accessing and managing {@link Room} entities.
 *
 * <p>Custom query methods are automatically implemented by Spring Data JPA
 * based on method names and parameter types.</p>
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByNumberOfBeds(int beds);
    List<Room> findByBedType(BedsType bedType);
    List<Room> findByPeopleCapacity(int numberOfPeople);
    List<Room> findByRoomType(RoomType roomType);
    List<Room> findByState(RoomState state);
    @Query("SELECT r FROM Room r JOIN r.hotel h WHERE h.id = :id")
    List<Room> findByHotel(Long id);
    @Query("SELECT r FROM Room r JOIN r.reservation res WHERE res.id = :id")
    List<Room> findByReservation(Long id);
    @Query("""
            SELECT r FROM Room r
            WHERE r.id NOT IN (SELECT rbp.room.id FROM RoomBookingPeriod rbp
                WHERE (rbp.status = 'RESERVED' OR rbp.status = 'BLOCKED')
                    AND rbp.startAt < :startAt
                    AND rbp.endAt > :endAt)
            """)
    List<Room> findByAvailableRoom(LocalDate startAt, LocalDate endAt);

}
