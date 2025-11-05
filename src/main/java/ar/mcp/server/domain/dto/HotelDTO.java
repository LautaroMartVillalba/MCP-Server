package ar.mcp.server.domain.dto;

import ar.mcp.server.domain.entities.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object for {@link Hotel}.
 * <p>
 * Used to transfer hotel data between layers without exposing the entity directly.
 * Contains basic hotel fields, lists of related entity IDs, and address information.
 * </p>
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HotelDTO {
    private Long id;
    private String name;
    private double stars;
    private int totalRooms;
    private int freeRooms;
    private int reservedRooms;
    private String contactPhone;
    private List<Long> roomsId;
    private List<Long> benefitsId;
    private List<Long> attractionsId;
    private String ubication;
    private String address;
}