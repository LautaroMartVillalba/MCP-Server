package ar.mcp.server.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

/**
 * Represents an Attraction entity within the application.
 * <p>
 * This class maps to the "entity_attraction" table in the database.
 * It stores information about attractions associated with a hotel, including
 * name, description, visitor capacity, and operating hours.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link Hotel}: Many-to-One relationship. Each attraction is linked to a single hotel.</li>
 * </ul>
 * </p>
 * <p>
 * Fields such as openAt and closeAt define the daily operating hours,
 * and peopleCapacity indicates the maximum number of visitors allowed.
 * The description field provides detailed information about the attraction.
 * </p>
 */
@Entity
@Table(name = "entity_attraction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "hotel")
public class Attraction {

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
    private int peopleCapacity;
    @NotNull
    private LocalTime openAt;
    @NotNull
    private LocalTime closeAt;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id", referencedColumnName = "id")
    private Hotel hotel;


}
