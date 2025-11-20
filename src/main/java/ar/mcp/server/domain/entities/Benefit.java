package ar.mcp.server.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalTime;

/**
 * Represents a benefit (services included on a reservation) offered by a hotel.
 * <p>
 * This entity is mapped to the {@code entity_benefit} table in the database.
 * A benefit may include amenities such as spa, gym, room service, or other
 * hotel-provided facilities. Each benefit has a name, description, and operating hours,
 * and is associated with multiple {@link Hotel}s through a Many-to-Many relationship.
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Stores descriptive and functional information about a service/benefit.</li>
 *   <li>Defines the operating hours of the service.</li>
 *   <li>Can be associated with multiple hotels.</li>
 * </ul>
 *
 */
@Entity
@Table(name = "entity_benefit")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "hotels")
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
    @ManyToMany(mappedBy = "benefits")
    private List<Hotel> hotels = new ArrayList<>();

}
