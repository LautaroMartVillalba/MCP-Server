package ar.mcp.server.domain.dto;


import ar.mcp.server.domain.entities.address.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for {@link Address}.
 * <p>
 * Used to transfer address data between layers without exposing the entity directly.
 * Contains basic address fields and references to associated hotel, person, and state.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddressDTO {

    private Long id;
    private String street;
    private String number;
    private String floor;
    private String departmentNumber;
    private String stateId;
    private String subdivisionName;
    private Long hotelId;
    private Long personId;

}
