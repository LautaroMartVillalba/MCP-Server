package ar.mcp.server.domain.entities;

import ar.mcp.server.domain.entities.address.Address;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Hotel entity within the application.
 * <p>
 * This class maps to the "entity_hotel" table in the database.
 * It contains fundamental information about a hotel, including its name,
 * star rating, room capacity, contact information, and associations with other entities.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link Room}: One-to-Many relationship. A hotel can have multiple rooms.</li>
 *     <li>{@link Benefit}: One-to-Many relationship. A hotel can provide multiple benefits.</li>
 *     <li>{@link Attraction}: One-to-Many relationship. A hotel can have multiple nearby attractions.</li>
 *     <li>{@link Address}: One-to-One relationship. Each hotel has a unique address.</li>
 * </ul>
 * </p>
 * <p>
 * Fields like totalRooms, freeRooms, and reservedRooms allow tracking room availability.
 * The contactPhone field ensures a unique contact number for the hotel.
 * </p>
 */
@Entity
@Table(name = "entity_hotel")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @NotBlank
    @Size(min = 2, max = 40)
    private String name;
    @NotNull
    private double stars;
    @NotNull
    @Min(2)
    @Column(name = "total_rooms")
    private int totalRooms;
    @NotNull
    @Column(name = "free_rooms")
    private int freeRooms;
    @NotNull
    @Column(name = "reserved_rooms")
    private int reservedRooms;
    @NotNull
    @Size(min = 9, max = 12)
    @Column(unique = true, name = "contact_phone")
    private String contactPhone;
    @NotNull
    @OneToMany(mappedBy = "hotel")
    private List<Room> rooms = new ArrayList<>();
    @NotNull
    @OneToMany(mappedBy = "hotel")
    private List<Benefit> benefits = new ArrayList<>();
    @NotNull
    @OneToMany(mappedBy = "hotel")
    private List <Attraction> attractions = new ArrayList<>();
    @OneToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

}
