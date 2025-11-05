package ar.mcp.server.domain.dto;

import ar.mcp.server.domain.entities.Benefit;
import lombok.*;

import java.time.LocalTime;

/**
 * Data Transfer Object for {@link Benefit}.
 * <p>
 * Used to transfer benefit (service) data between layers without exposing the entity directly.
 * Contains basic benefit fields and reference to the associated hotel.
 * </p>
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BenefitDTO {

    private Long id;
    private String name;
    private String description;
    private LocalTime openAt;
    private LocalTime closeAt;
    private Long hotelId;
}
