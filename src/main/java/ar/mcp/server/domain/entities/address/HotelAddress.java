package ar.mcp.server.domain.entities.address;

import ar.mcp.server.domain.entities.Hotel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Hotel-specific Address entity.
 * <p>
 * This class extends the abstract Address class and specializes it for Hotel entities.
 * Each hotel address is linked to exactly one Hotel.
 * </p>
 */
@Entity
@DiscriminatorValue("HOTEL")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "hotelAddressBuilder")
public class HotelAddress extends Address {

    @OneToOne
    @JoinColumn(name = "entity_hotel_id", referencedColumnName = "id")
    private Hotel hotel;

}
