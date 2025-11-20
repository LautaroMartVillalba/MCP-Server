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
 * Abstract base class representing an Address entity within the application.
 * <p>
 * This class uses single table inheritance strategy with a discriminator column
 * to distinguish between HotelAddress and PersonAddress types.
 * </p>
 * <p>
 * An address stores location details including street, number, floor, and apartment number.
 * Addresses are referenced by either Hotel or Person entities, but never both simultaneously.
 * </p>
 */
@Entity
@Table(name = "entity_address")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "address_type", discriminatorType = DiscriminatorType.STRING)
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

}
