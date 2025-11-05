package ar.mcp.server.domain.entities.address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * Represents a State or Province entity within the application.
 * <p>
 * This class maps to the "entity_states" table in the database.
 * It stores information about a state's code, associated country, and subdivision name.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>{@link Address}: One-to-Many relationship. A state can have multiple addresses linked to it.</li>
 * </ul>
 * </p>
 * <p>
 * The code field uniquely identifies the state, while countryCode links it to a specific country.
 * The subdivision field provides the name of the state or province.
 * This entity centralizes regional information used by addresses of hotels and persons.
 * </p>
 */
@Entity
@Table(name = "entity_states")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class States {

    @Id
    private String code;
    @Column(name = "country_code")
    private String countryCode;
    @Column(name = "subdivision_name")
    private String subdivision;
    @OneToMany(mappedBy = "state")
    private List<Address> addresses;

}
