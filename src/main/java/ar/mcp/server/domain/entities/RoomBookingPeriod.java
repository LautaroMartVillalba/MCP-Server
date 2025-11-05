package ar.mcp.server.domain.entities;

import ar.mcp.server.domain.enums.RoomBookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a RoomBookingPeriod entity within the application.
 * <p>
 * This class maps to the "entity_room_booking_period" table in the database.
 * It stores information about the specific booking periods of rooms, including
 * start and end dates, booking status, and associated room and reservation.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link Room}: Many-to-One relationship. Each booking period belongs to a specific room.</li>
 *     <li>{@link Reservation}: One-to-One relationship. Each booking period is associated with a single reservation.</li>
 * </ul>
 * </p>
 * <p>
 * The status field uses an enumerated type {@link RoomBookingStatus} to define the current booking state.
 * This entity allows tracking room availability over time and linking it to reservations.
 * </p>
 */
@Entity
@Table(name = "entity_room_booking_period")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomBookingPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private LocalDate startAt;
    @NotNull
    private LocalDate endAt;
    @NotNull
    @Enumerated(EnumType.STRING)
    private RoomBookingStatus status;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private Room room;
    @NotNull
    @OneToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "id")
    private Reservation reservation;
}
