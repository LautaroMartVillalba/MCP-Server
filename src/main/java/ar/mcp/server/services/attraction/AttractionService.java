package ar.mcp.server.services.attraction;

import ar.mcp.server.domain.dto.AttractionDTO;
import ar.mcp.server.domain.entities.Attraction;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.repositories.AttractionRepository;
import ar.mcp.server.repositories.HotelRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for managing {@link Attraction} entities.
 * Provides CRUD operations and search functionalities for attractions associated with {@link Hotel} entities.
 *
 * Relationships:
 * - {@link Attraction} is associated with a single {@link Hotel}.
 * - Interacts with {@link AttractionRepository} and {@link HotelRepository} to persist and retrieve entities.
 */



@Service
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final HotelRepository hotelRepository;


    public AttractionService(AttractionRepository attractionRepository, HotelRepository hotelRepository) {
        this.attractionRepository = attractionRepository;
        this.hotelRepository = hotelRepository;
    }

    /**
     * Validates the basic information of an attraction.
     *
     * @param name           Name of the attraction. Must not be null or blank.
     * @param description    Description of the attraction. Must not be null or blank.
     * @param peopleCapacity Maximum capacity of people for the attraction. Must be >= 1.
     * @param openAt         Opening time of the attraction. Must not be null.
     * @param closeAt        Closing time of the attraction. Must not be null.
     * @throws RuntimeException if any parameter is invalid.
     */
    private void validateInfo(String name, String description, int peopleCapacity, LocalTime openAt, LocalTime closeAt){
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Insert the attraction name.");
        }
        if (description == null || description.isBlank()) {
            throw new RuntimeException("Insert the description name.");
        }
        if (peopleCapacity < 1) {
            throw new RuntimeException("At least the attraction must be capable to be used by one person.");
        }
        if (openAt == null) {
            throw new RuntimeException("Attraction opening cannot be null.");
        }
        if (closeAt == null) {
            throw new RuntimeException("Attraction ending cannot be null.");
        }
    }

    /**
     * Converts a list of {@link Attraction} entities to {@link AttractionDTO}.
     *
     * @param list List of {@link Attraction} entities.
     * @return List of {@link AttractionDTO} objects.
     */
    public List<AttractionDTO> parseFromAttractionListToAttractionDTOList(List<Attraction> list){
        return list.stream().map(attraction -> {
            List<Long> hotelIds = attraction.getHotels().stream()
                    .map(Hotel::getId)
                    .toList();

            return AttractionDTO.builder()
                    .id(attraction.getId())
                    .name(attraction.getName())
                    .description(attraction.getDescription())
                    .peopleCapacity(attraction.getPeopleCapacity())
                    .openAt(attraction.getOpenAt())
                    .closeAt(attraction.getCloseAt())
                    .hotelIds(hotelIds).build();
        }).toList();
    }

    /**
     * Creates a new {@link Attraction} and associates it with multiple {@link Hotel}s.
     *
     * @param dto {@link AttractionDTO} containing attraction data.
     * @return The created {@link Attraction} entity.
     * @throws RuntimeException if required fields are missing.
     * @throws RuntimeException if any hotel does not exist.
     */
    @McpTool(
            name = "create_attraction",
            description = "Crea una nueva atracción y la asocia a uno o más hoteles existentes."
    )
    @Transactional
    public Attraction createAttraction(
            @ToolParam(required = true, description = """
            DTO con los datos de la atracción a crear. hotelIds puede estar vacío inicialmente.
            """) AttractionDTO dto) {
        validateInfo(dto.getName(), dto.getDescription(), dto.getPeopleCapacity(), dto.getOpenAt(), dto.getCloseAt());

        List<Hotel> hotels = new ArrayList<>();
        if (dto.getHotelIds() != null && !dto.getHotelIds().isEmpty()) {
            hotels = hotelRepository.findAllById(dto.getHotelIds());
            if (hotels.size() != dto.getHotelIds().size()) {
                throw new RuntimeException("One or more hotels not found in the Database.");
            }
        }

        Attraction attraction = Attraction.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .peopleCapacity(dto.getPeopleCapacity())
                .openAt(dto.getOpenAt())
                .closeAt(dto.getCloseAt())
                .hotels(hotels).build();

        attractionRepository.save(attraction);

        return attraction;
    }

    /**
     * Retrieves an {@link Attraction} entity by its ID.
     *
     * @param id ID of the attraction. Must be greater than 0.
     * @return {@link Attraction} entity.
     * @throws RuntimeException        if ID is invalid.
     * @throws RuntimeException if the attraction is not found.
     */
    @McpTool(
            name = "get_attraction_by_id_object",
            description = "Obtiene una entidad Attraction a partir de su ID."
    )
    public Attraction getAttractionByIdObject(
            @ToolParam(required = true, description = """
            ID de la atracción a obtener.
            """) Long id) {
        if (id <= 0) {
            throw new RuntimeException("Id cannot be null.");
        }

        return attractionRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    @McpTool(
            name = "find_attraction_by_spec",
            description = """
                Realiza una búsqueda dinámica de atracciones según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "id": null,
                  "name": null,
                  "description": "jacuzzi con hidromasaje",
                  "minPeopleCapacity": null,
                  "maxPeopleCapacity": null,
                  "openAt": null,
                  "closeAt": null
                }

                En este ejemplo, solo se filtrarán las atracciones cuya descripción tenga una coincidencia con "jacuzzi con hidromasaje" en su texto."""
    )
    public List<AttractionDTO> findAttractionBySpec(
            @ToolParam(required = false,
                    description = "Identificador único del registro. No obligatorio.") Long id,
            @ToolParam(required = false,
                    description = "Nombre de la atracción. No obligatorio.") String name,
            @ToolParam(required = false,
                    description = """
            Idea general en frases cortas o con una o
            dos palabras clave para evitar falsos negativos en coincidencias de
            descripciones. No obligatorio.""") String description,
            @ToolParam(required = false,
                    description = "Capacidad mínima de gente por la cual se quiere filtrar. No obligatorio.") Integer minPeopleCapacity,
            @ToolParam(required = false,
                    description = "Cantidad máxima de gente por la cual se quiere filtrar. No obligatorio.") Integer maxPeopleCapacity,
            @ToolParam(required = false,
                    description = "Horario de apertura. No obligatorio.") LocalTime openAt,
            @ToolParam(required = false,
                    description = "Horario de cierre. No obligatorio.") LocalTime closeAt
    ){
        Specification<Attraction> specification = Specification.unrestricted();

        specification = specification.and(AttractionSpecifications.hasId(id));
        specification = specification.and(AttractionSpecifications.hasName(name));
        specification = specification.and(AttractionSpecifications.hasDescription(description));
        specification = specification.and(AttractionSpecifications.hasCapacityGreaterThan(minPeopleCapacity));
        specification = specification.and(AttractionSpecifications.hasCapacityLessThan(maxPeopleCapacity));
        specification = specification.and(AttractionSpecifications.hasOpenAt(openAt));
        specification = specification.and(AttractionSpecifications.hasEndAt(closeAt));

        specification = specification.and(AttractionSpecifications.fetchEverythingForDTO());

        return this.parseFromAttractionListToAttractionDTOList(attractionRepository.findAll(specification));
    }

    /**
     * Updates an existing {@link Attraction} entity with the information provided in the DTO.
     *
     * @param id  ID of the attraction to update. Must exist in the database.
     * @param dto {@link AttractionDTO} containing the new values. Fields that are null or empty are ignored.
     * @return The updated {@link Attraction} entity.
     * @throws RuntimeException if no attraction with the given ID exists.
     * @throws RuntimeException        if any field in the updated entity is invalid according to {@link #validateInfo}.
     */
    @McpTool(
            name = "update_attraction",
            description = "Actualiza una atracción existente con los datos proporcionados."
    )
    @Transactional
    Attraction updateAttraction(
            @ToolParam(required = true, description = """
            ID de la atracción que se desea actualizar.
            """) Long id,
            @ToolParam(required = true, description = """
            DTO con los nuevos valores para la atracción.
            """) AttractionDTO dto) {
        Attraction attractionInDB = attractionRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the Database."));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            attractionInDB.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            attractionInDB.setDescription(dto.getDescription());
        }
        if (dto.getOpenAt() != null) {
            attractionInDB.setOpenAt(dto.getOpenAt());
        }
        if (dto.getCloseAt() != null) {
            attractionInDB.setCloseAt(dto.getCloseAt());
        }
        if (dto.getPeopleCapacity() != 0) {
            attractionInDB.setPeopleCapacity(dto.getPeopleCapacity());
        }

        validateInfo(attractionInDB.getName()
                , attractionInDB.getDescription()
                , attractionInDB.getPeopleCapacity()
                , attractionInDB.getOpenAt()
                , attractionInDB.getCloseAt());
        attractionRepository.save(attractionInDB);

        return attractionInDB;
    }

    /**
     * Deletes an {@link Attraction} entity from the database.
     * Deletion is not allowed if the attraction is currently operating (i.e., current time is between openAt and closeAt).
     *
     * @param id ID of the attraction to delete. Must exist in the database.
     * @throws RuntimeException if no attraction with the given ID exists.
     * @throws RuntimeException        if the attraction is currently open and cannot be deleted.
     */
    @McpTool(
            name = "delete_attraction",
            description = "Elimina una atracción, siempre que no esté en funcionamiento en el momento actual."
    )
    @Transactional
    public void deleteAttraction(
            @ToolParam(required = true, description = """
            ID de la atracción a eliminar.
            """) Long id) {
        Attraction attractionInDB = this.getAttractionByIdObject(id);

        LocalTime opening = attractionInDB.getOpenAt();
        LocalTime ending = attractionInDB.getCloseAt();

        if (opening.isBefore(LocalTime.now()) && ending.isAfter(LocalTime.now())){
            throw new RuntimeException("Cannot delete an attraction when is working.");
        }

        attractionRepository.deleteById(id);
    }

}
