package ar.mcp.server.domain.dto;

import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.enums.BedsType;
import ar.mcp.server.domain.enums.RoomState;
import ar.mcp.server.domain.enums.RoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for {@link Room}.
 * <p>
 * Used to transfer room data between layers without exposing the entity directly.
 * Contains basic room information, enumerated types for bed, room type, and state,
 * and references to associated hotel, reservations, and booking periods.
 * </p>
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoomDTO{

    private Long id;
    private int floor;
    private int numberOfBeds;
    @Enumerated(EnumType.STRING)
    private BedsType bedType;
    private int peopleCapacity;
    private Long timesBooked;
    private BigDecimal pricePerNight;
    @Enumerated(EnumType.STRING)
    private RoomType roomType;
    @Enumerated(EnumType.STRING)
    private RoomState state;
    private Long hotelId;
    private List<Long> reservationId;
    private List<Long> roomBookingPeriodId;

}
