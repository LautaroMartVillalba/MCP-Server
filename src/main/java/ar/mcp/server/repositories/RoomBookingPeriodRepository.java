package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.RoomBookingPeriod;
import ar.mcp.server.domain.enums.RoomBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomBookingPeriodRepository extends JpaRepository<RoomBookingPeriod, Long> {
    List<RoomBookingPeriod> findByStartAtGreaterThan(LocalDate startAt);
    List<RoomBookingPeriod> findByEndAtLessThan(LocalDate endAt);
    List<RoomBookingPeriod> findByStartAtGreaterThanAndEndAtLessThan(LocalDate startAt, LocalDate endAt);
    List<RoomBookingPeriod> findByStatus(RoomBookingStatus status);
    @Query("SELECT a FROM RoomBookingPeriod a JOIN a.room b WHERE b.id = :roomId")
    List<RoomBookingPeriod> findByRoom(Long roomId);
}
