package ar.mcp.server.domain.entities.address;

import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.Person;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an Address entity within the application.
 * <p>
 * This class maps to the "entity_address" table in the database.
 * It stores the address details of a person or a hotel, including street, number, floor, and department number.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link States}: Many-to-One relationship. Each address is associated with a state or province.</li>
 *     <li>{@link Hotel}: One-to-One relationship. An address can be linked to a single hotel.</li>
 *     <li>{@link Person}: One-to-One relationship. An address can be linked to a single person.</li>
 * </ul>
 * </p>
 * <p>
 * Nullable fields like floor and departmentNumber allow flexibility for different types of addresses.
 * This entity centralizes location information used by both hotels and clients in the system.
 * </p>
 */
@Entity
@Table(name = "entity_address")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Nonnull
    private String street;
    @Nonnull
    private String number;
    @Nullable
    private String floor;
    @Nullable
    @Column(name = "door_number")
    private String departmentNumber;
    @ManyToOne
    private States state;
    @OneToOne(mappedBy = "address")
    private Hotel hotel;
    @OneToOne(mappedBy = "address")
    private Person person;

}
