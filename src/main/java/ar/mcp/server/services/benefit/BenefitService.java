package ar.mcp.server.services.benefit;

import ar.mcp.server.domain.dto.BenefitDTO;
import ar.mcp.server.domain.entities.Benefit;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.repositories.BenefitRepository;
import ar.mcp.server.repositories.HotelRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import java.time.LocalTime;
import java.util.List;

/**
 * Service layer for managing {@link Benefit} entities.
 * Provides methods to create, retrieve, update, and delete benefits,
 * as well as to query them by different attributes such as name, description, opening/closing times, or hotel.
 */
@Service
public class BenefitService {

    private final BenefitRepository benefitRepository;
    private final HotelRepository hotelRepository;

    public BenefitService(BenefitRepository repository, HotelRepository hotelRepository) {
        this.benefitRepository = repository;
        this.hotelRepository = hotelRepository;
    }

    /**
     * Validates the mandatory fields of a {@link Benefit}.
     *
     * @param name        Name of the benefit.
     * @param description Description of the benefit.
     * @param openAt      Opening time.
     * @param closeAt     Closing time.
     * @throws RuntimeException if any of the parameters are invalid or missing.
     */
    void validateInfo(String name, String description, LocalTime openAt, LocalTime closeAt){

        if (name.isBlank()){
            throw new RuntimeException("Name cannot be null.");
        }
        if (description.isBlank()){
            throw new RuntimeException("Description cannot be null.");
        }
        if (openAt == null){
            throw new RuntimeException("Opening time cannot be null.");
        }
        if (closeAt == null){
            throw new RuntimeException("Ending time cannot be null.");
        }
    }

    /**
     * Converts a list of {@link Benefit} entities into a list of {@link BenefitDTO}.
     *
     * @param list List of {@link Benefit} entities.
     * @return List of {@link BenefitDTO}.
     */
    public List<BenefitDTO> parseBenefitListToBenefitDTOList (List<Benefit> list){
        return list.stream().map(benefit -> {
            List<Long> hotelIds = benefit.getHotels().stream()
                    .map(Hotel::getId)
                    .toList();

            return BenefitDTO.builder()
                    .id(benefit.getId())
                    .name(benefit.getName())
                    .description(benefit.getDescription())
                    .openAt(benefit.getOpenAt())
                    .closeAt(benefit.getCloseAt())
                    .hotelIds(hotelIds).build();
        }).toList();
    }

    /**
     * Creates a new {@link Benefit} entity in the database.
     *
     * @param dto Data transfer object containing the information for the benefit.
     * @return The created {@link Benefit} entity.
     * @throws RuntimeException if any associated hotel cannot be found.
     * @throws RuntimeException        if required fields are missing.
     */
    @McpTool(
            name = "create_benefit",
            description = "Crea un nuevo beneficio en la base de datos a partir de un DTO con la información correspondiente."
    )
    @Transactional
    public Benefit createBenefit(
            @ToolParam(
                    required = true,
                    description = "DTO con la información del beneficio a crear. hotelIds puede estar vacío inicialmente."
            ) BenefitDTO dto){
        validateInfo(dto.getName(), dto.getDescription(), dto.getOpenAt(), dto.getCloseAt());
        
        List<Hotel> hotels = new ArrayList<>();
        if (dto.getHotelIds() != null && !dto.getHotelIds().isEmpty()) {
            hotels = hotelRepository.findAllById(dto.getHotelIds());
            if (hotels.size() != dto.getHotelIds().size()) {
                throw new RuntimeException("One or more hotels not found in the Database.");
            }
        }

        Benefit benefit = Benefit.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .openAt(dto.getOpenAt())
                .closeAt(dto.getCloseAt())
                .hotels(hotels).build();

        benefitRepository.save(benefit);

        return benefit;
    }

    /**
     * Retrieves a {@link Benefit} entity by its ID.
     *
     * @param id ID of the benefit.
     * @return {@link Benefit} entity.
     * @throws RuntimeException        if ID is invalid.
     * @throws RuntimeException if the benefit cannot be found.
     */
    @McpTool(
            name = "get_benefit_by_id_object",
            description = "Obtiene un beneficio por su ID y devuelve la entidad Benefit completa."
    )
    public Benefit getBenefitByIdObject(
            @ToolParam(
                    required = true,
                    description = "ID del beneficio que se desea obtener."
            ) Long id) {
        if (id == 0 || id < 1) {
            throw new RuntimeException("Insert a valid id number.");
        }

        return benefitRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    @McpTool(
            name = "find_benefit_by_spec",
            description = """
                Realiza una búsqueda dinámica de beneficios que brinda un hotel según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "id": null,
                  "name": "Desayuno",
                  "description": null,
                  "openAt": null,
                  "closeAt": null
                }

                En este ejemplo, solo se filtrarán los beneficios de hoteles que tengan "desayuno" en su nombre."""
    )
    public List<BenefitDTO> findBenefitBySpec(
            @ToolParam(required = false,
                    description = "Identificador único del registro. No obligatorio.") Long id,
            @ToolParam(required = false,
                    description = """
                                    Nombre del beneficio, tratar de resumir
                                    o utilizar palabras clave para evitar falsos
                                    negativos. No obligatorio.""") String name,
            @ToolParam(required = false,
                    description = """
                                    Idea general en frases cortas o con una o
                                    dos palabras clave para evitar falsos negativos en coincidencias de
                                    descripciones. No obligatorio.""") String description,
            @ToolParam(required = false,
                    description = "Horario en el cual el beneficio empieza a estar disponible. No obligatorio.") LocalTime openAt,
            @ToolParam(required = false,
                    description = "Horario en el cual el beneficio ya no está disponible. No obligatorio.") LocalTime closeAt
    ){
        Specification<Benefit> specification = Specification.unrestricted();

        specification = specification.and(BenefitSpecifications.hasId(id));
        specification = specification.and(BenefitSpecifications.hasName(name));
        specification = specification.and(BenefitSpecifications.hasDescription(description));
        specification = specification.and(BenefitSpecifications.hasOpenAtGraterThan(openAt));
        specification = specification.and(BenefitSpecifications.hasCloseAtLessThan(closeAt));

        specification = specification.and(BenefitSpecifications.fetchEverythingForDTO());

        return parseBenefitListToBenefitDTOList(benefitRepository.findAll(specification));
    }

    /**
     * Updates an existing {@link Benefit} entity with the provided data.
     *
     * @param id  ID of the benefit to update.
     * @param dto {@link BenefitDTO} containing new values.
     * @return Updated {@link Benefit} entity.
     * @throws RuntimeException if the benefit cannot be found.
     * @throws RuntimeException        if updated data is invalid.
     */
    @McpTool(
            name = "update_benefit",
            description = "Actualiza un beneficio existente con nuevos valores proporcionados en un DTO."
    )
    @Transactional
    public Benefit updateBenefit(
            @ToolParam(
                    required = true,
                    description = "ID del beneficio que se desea actualizar."
            ) Long id,
            @ToolParam(
                    required = true,
                    description = "DTO con los nuevos valores del beneficio."
            ) BenefitDTO dto){
        Benefit benefitInDB = this.getBenefitByIdObject(id);

        if (!dto.getName().isBlank()){
            benefitInDB.setName(dto.getName());
        }
        if (!dto.getDescription().isBlank()){
            benefitInDB.setDescription(dto.getDescription());
        }
        if (dto.getOpenAt() != null){
            benefitInDB.setOpenAt(dto.getOpenAt());
        }
        if (dto.getCloseAt() != null){
            benefitInDB.setCloseAt(dto.getCloseAt());
        }

        validateInfo(benefitInDB.getName()
                    , benefitInDB.getDescription()
                    , benefitInDB.getOpenAt()
                    , benefitInDB.getCloseAt());
        benefitRepository.save(benefitInDB);

        return benefitInDB;
    }

    /**
     * Deletes a {@link Benefit} entity by ID.
     * Deletion is not allowed if the benefit is currently operating.
     *
     * @param id ID of the benefit to delete.
     * @throws RuntimeException if the benefit cannot be found.
     * @throws RuntimeException        if the benefit is currently open.
     */
    @McpTool(
            name = "delete_benefit_by_id",
            description = "Elimina un beneficio por su ID, siempre que no se encuentre actualmente en horario de funcionamiento."
    )
    @Transactional
    public void deleteBenefitById(
            @ToolParam(
                    required = true,
                    description = "ID del beneficio que se desea eliminar."
            ) Long id){
        Benefit benefitInDB = this.getBenefitByIdObject(id);

        if (benefitInDB.getOpenAt().isBefore(LocalTime.now()) && benefitInDB.getCloseAt().isAfter(LocalTime.now())){
            throw new RuntimeException("Cannot delete a Benefit when is working.");
        }

        benefitRepository.deleteById(id);
    }

}
