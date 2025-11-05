package ar.mcp.server.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a Reservation entity within the application.
 * <p>
 * This class maps to the "entity_reservation" table in the database.
 * It stores information about hotel reservations, including the number of people,
 * number of nights, booking dates, total price, and associated client and room.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link Person}: One-to-One relationship. Each reservation is linked to a single client.</li>
 *     <li>{@link Room}: Many-to-One relationship. Each reservation is associated with a specific room.</li>
 * </ul>
 * </p>
 * <p>
 * The startAt and endAt fields use date validation to ensure proper booking periods.
 * The totalPrice field indicates the cost for the reservation, calculated based on the room and duration.
 * </p>
 */
@Entity
@Table(name = "entity_reservation")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "client")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Min(1)
    @Max(value = 4, message = "")
    @Column(name = "number_of_people")
    private Long numberOfPeople;
    @NotNull
    @Min(1)
    @Column(name = "number_of_nights")
    private Long numberOfNights;
    @NotNull
    @FutureOrPresent
    @Column(name = "start_at")
    private LocalDate startAt;
    @NotNull
    @Future
    @Column(name = "end_at")
    private LocalDate endAt;
    @NotNull
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @NotNull
    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person client;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private Room roomBooked;

}
