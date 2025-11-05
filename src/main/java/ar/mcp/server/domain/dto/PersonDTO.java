package ar.mcp.server.domain.dto;

import ar.mcp.server.domain.entities.Person;
import lombok.*;

/**
 * Data Transfer Object for {@link Person}.
 * <p>
 * Used to transfer person (client) data between layers without exposing the entity directly.
 * Contains basic personal information, reservation reference, and address details.
 * </p>
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PersonDTO {

    private Long id;
    private String email;
    private String dni;
    private String name;
    private int age;
    private String cellPhone;
    private int numberOfReservations;
    private Long reservationId;
    private String ubication;
    private String address;

}
