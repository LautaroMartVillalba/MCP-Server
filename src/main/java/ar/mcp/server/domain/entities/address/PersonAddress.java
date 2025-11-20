package ar.mcp.server.domain.entities.address;

import ar.mcp.server.domain.entities.Person;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Person-specific Address entity.
 * <p>
 * This class extends the abstract Address class and specializes it for Person entities.
 * Each person address is linked to exactly one Person.
 * </p>
 */
@Entity
@DiscriminatorValue("PERSON")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "personAddressBuilder")
public class PersonAddress extends Address {

    @OneToOne(mappedBy = "address")
    private Person person;

}
