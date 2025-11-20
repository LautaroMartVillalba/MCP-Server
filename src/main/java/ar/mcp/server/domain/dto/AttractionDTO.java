package ar.mcp.server.domain.dto;

import ar.mcp.server.domain.entities.Attraction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

import java.util.List;

/**
 * Data Transfer Object for {@link Attraction}.
 * <p>
 * Used to transfer attraction data between layers without exposing the entity directly.
 * Contains basic attraction fields and references to associated hotels.
 * </p>
 */
@AllArgsConstructor
@Builder
@Data
public class AttractionDTO {

    private Long id;
    private String name;
    private String description;
    private int peopleCapacity;
    private LocalTime openAt;
    private LocalTime closeAt;
    private List<Long> hotelIds;

}
