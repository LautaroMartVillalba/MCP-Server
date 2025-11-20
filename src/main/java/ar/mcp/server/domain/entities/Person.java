package ar.mcp.server.domain.entities;

import ar.mcp.server.domain.entities.address.PersonAddress;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Person entity within the application.
 * <p>
 * This class maps to the "entity_person" table in the database.
 * It contains personal information of a client, such as email,
 * identification number (DNI), name, age, contact number, and reservation history.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link Reservation}: One-to-Many relationship. Each person can have multiple reservations.</li>
 *     <li>{@link PersonAddress}: One-to-One relationship. Each person can have a unique address.</li>
 * </ul>
 * </p>
 * <p>
 * The email, dni, and cellPhone fields are unique to ensure proper identification and contact.
 * The numberOfReservations field tracks the total reservations made by the person.
 * </p>
 */
@Entity
@Table(name = "entity_person")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @NotBlank
    @Email
    @Size(min = 10)
    @Column(unique = true)
    private String email;
    @NotNull
    @NotBlank
    @Size(min = 10)
    @Column(unique = true)
    private String dni;
    @NotNull
    @NotBlank
    @Size(min = 16, max = 52)
    private String name;
    @NotNull
    @Min(18)
    private int age;
    @Column(unique = true, name = "cell_phone_number")
    @NotNull
    @Size(min = 7)
    private String cellPhone;
    @NotNull
    private int numberOfReservations;
    @OneToMany(mappedBy = "person")
    private List<Reservation> reservations = new ArrayList<>();
    @OneToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private PersonAddress address;

}
