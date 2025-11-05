package ar.mcp.server.domain.entities;

import ar.mcp.server.domain.enums.BedsType;
import ar.mcp.server.domain.enums.RoomState;
import ar.mcp.server.domain.enums.RoomType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a Room entity within the application.
 * <p>
 * This class maps to the "entity_room" table in the database.
 * It contains detailed information about a hotel room, including its floor, bed configuration,
 * capacity, type, state, booking count, and pricing.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link Hotel}: Many-to-One relationship. Each room belongs to a single hotel.</li>
 *     <li>{@link Reservation}: One-to-Many relationship. A room can have multiple reservations.</li>
 *     <li>{@link RoomBookingPeriod}: One-to-Many relationship. A room can have multiple booking periods.</li>
 * </ul>
 * </p>
 * <p>
 * The roomType, bedType, and state fields use enumerated types to define predefined categories.
 * The timesBooked field tracks the number of times the room has been booked, and
 * pricePerNight indicates the cost per night for the room.
 * </p>
 */
@Entity
@Table(name = "entity_room")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "hotel")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Min(1)
    private int floor;
    @NotNull
    @Min(1)
    @Max(4)
    private int numberOfBeds;
    @NotNull
    @Enumerated(EnumType.STRING)
    private BedsType bedType;
    @NotNull
    @Min(1)
    @Max(4)
    private int peopleCapacity;
    @Enumerated(EnumType.STRING)
    @NotNull
    private RoomType roomType;
    @Enumerated(EnumType.STRING)
    @NotNull
    private RoomState state;
    @NotNull
    private Long timesBooked;
    @NotNull
    private BigDecimal pricePerNight;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id", referencedColumnName = "id")
    private Hotel hotel;
    @NotNull
    @OneToMany(mappedBy = "roomBooked")
    private List<Reservation> reservation;
    @NotNull
    @OneToMany(mappedBy = "room")
    private List<RoomBookingPeriod> roomBookingPeriod;

}
