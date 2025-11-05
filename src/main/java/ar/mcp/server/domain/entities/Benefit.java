package ar.mcp.server.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

/**
 * Represents a benefit (services included on a reservation) offered by a hotel.
 * <p>
 * This entity is mapped to the {@code entity_service} table in the database.
 * A benefit may include amenities such as spa, gym, room service, or other
 * hotel-provided facilities. Each benefit has a name, description, and operating hours,
 * and is associated with a specific {@link Hotel}.
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Stores descriptive and functional information about a service/benefit.</li>
 *   <li>Defines the operating hours of the service.</li>
 *   <li>Associates the benefit with the hotel that offers it.</li>
 * </ul>
 *
 */
@Entity
@Table(name = "entity_service")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "hotel")
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Size(min = 4, max = 20)
    private String name;
    @NotNull
    @Size(min = 50, max = 500)
    private String description;
    @NotNull
    private LocalTime openAt;
    @NotNull
    private LocalTime closeAt;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id", referencedColumnName = "id")
    private Hotel hotel;

}
