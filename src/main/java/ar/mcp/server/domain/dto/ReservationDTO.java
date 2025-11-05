package ar.mcp.server.domain.dto;

import ar.mcp.server.domain.entities.Reservation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for {@link Reservation}.
 * <p>
 * Used to transfer reservation data between layers without exposing the entity directly.
 * Contains basic reservation details and references to the associated person and room.
 * </p>
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReservationDTO {
    private Long id;
    private Long numberOfPeople;
    private Long numberOfNights;
    private LocalDate startAt;
    private LocalDate endAt;
    private BigDecimal totalPrice;
    private Long personId;
    private Long roomBookedId;
}
