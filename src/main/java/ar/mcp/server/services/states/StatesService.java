package ar.mcp.server.services.states;


import ar.mcp.server.domain.dto.StatesDTO;
import ar.mcp.server.domain.entities.address.States;
import ar.mcp.server.repositories.StateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for managing {@link States} entities.
 * Provides methods to retrieve states by code, country code, or subdivision name,
 * and converts {@link States} entities into {@link StatesDTO} objects for responses.
 */
@Service
public class StatesService {
    private final StateRepository stateRepository;
    private static final Logger log = LoggerFactory.getLogger(StatesService.class);

    public StatesService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    /**
     * Converts a list of {@link States} entities into a list of {@link StatesDTO}.
     *
     * @param list List of States entities.
     * @return List of StatesDTO objects with mapped properties.
     */
    private List<StatesDTO> parseFormStateEntityToStateDTO(List<States> list) {
        return list.stream().map(states -> StatesDTO.builder()
                        .code(states.getCode())
                        .subdivision(states.getSubdivision())
                        .countryCode(states.getCountryCode()).build())
                .toList();
    }

    @McpTool(
            name = "find_states_by_spec",
            description = """
                Realiza una búsqueda dinámica en los registros de estados/provincias según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "code": null,
                  "subdivision": "Andorra la Vella",
                  "countryCode": null
                }

                En este ejemplo, solo se filtrarán los estados por la subdivisión Andorra la Vella."""
    )
    public List<StatesDTO> findStateBySpec(
            @ToolParam(required = false,
                    description = """
                            La unión del código del país y el código de la
                            ciudad del registro. Por ejemplo, AR-K. No obligatorio.""") String code,
            @ToolParam(required = false,
                    description = "Nombre del estado o provincia a la que pertenece. No obligatorio.") String subdivision,
            @ToolParam(required = false,
                    description = "Código de país. En el formato ISO 3166-1 alfa-2. No obligatorio.") String countryCode
    ){
        log.debug("Iniciando búsqueda de estados con parámetros: code={}, subdivision={}, countryCode={}", code, subdivision, countryCode);
        Specification<States> specification = Specification.unrestricted();

        specification.and(StatesSpecifications.hasCode(code));
        specification.and(StatesSpecifications.hasSubdivision(subdivision));
        specification.and(StatesSpecifications.hasCountryCode(countryCode));

        specification = specification.and(StatesSpecifications.fetchEverythingForDTO());

        List<StatesDTO> result = parseFormStateEntityToStateDTO(stateRepository.findAll(specification));
        log.debug("Búsqueda completada. Registros encontrados: {}", result.size());
        return result;
    }

    /**
     * Retrieves a {@link StatesDTO} for a given state code.
     *
     * @param stateCode Code of the state.
     * @return StatesDTO corresponding to the given state code.
     */
    public StatesDTO getStateByCodeResponse(String stateCode) {
        log.debug("Buscando estado por código: {}", stateCode);
        if (stateCode.isBlank()) {
            throw new RuntimeException("Insert a state' code, please.");
        }

        States states = stateRepository.findByCode(stateCode).orElseThrow(() -> new RuntimeException("The resource cannot be found in the DataBase."));

        StatesDTO result = StatesDTO.builder()
                .code(states.getCode())
                .countryCode(states.getCountryCode())
                .subdivision(states.getSubdivision()).build();
        log.debug("Estado encontrado: {}", result.getCode());
        return result;
    }

    /**
     * Retrieves a {@link States} entity for a given state code.
     *
     * @param stateCode Code of the state.
     * @return States entity corresponding to the given state code.
     */
    public States getStateByCodeObject(String stateCode) {
        log.debug("Recuperando entidad States con código: {}", stateCode);
        if (stateCode.isBlank()) {
            throw new RuntimeException("Insert a state code, please.");
        }

        States result = stateRepository.findByCode(stateCode).orElseThrow(() -> new RuntimeException("The resource cannot be found in the DataBase."));
        log.debug("Entidad States recuperada: {}", result.getCode());
        return result;
    }

    /**
     * Retrieves a list of {@link StatesDTO} for a given country code.
     *
     * @param countryCode Code of the country.
     * @return List of StatesDTO for the specified country code.
     */
    public List<StatesDTO> getStateByCountryCode(String countryCode) {
        log.debug("Buscando estados por código de país: {}", countryCode);
        if (countryCode.isBlank()) {
            throw new RuntimeException("Insert a country code, please.");
        }

        List<StatesDTO> result = parseFormStateEntityToStateDTO(stateRepository.findByCountryCode(countryCode));
        log.debug("Estados encontrados: {} registros", result.size());
        return result;
    }

    /**
     * Retrieves a list of {@link StatesDTO} containing a specific subdivision name.
     *
     * @param subdivisionName Subdivision name or partial name.
     * @return List of StatesDTO matching the subdivision name.
     */
    @McpTool(
            name = "get_state_by_subdivision_name",
            description = "Obtiene una lista de StatesDTO que coinciden con un nombre o parte del nombre de subdivisión."
    )
    public List<StatesDTO> getStateBySubdivisionName(
            @ToolParam(
                    required = true,
                    description = "Nombre o parte del nombre de la subdivisión a buscar."
            )
            String subdivisionName) {
        log.debug("Buscando estados por nombre de subdivisión: {}", subdivisionName);
        if (subdivisionName.isBlank()) {
            throw new RuntimeException("Insert a state subdivision same, please.");
        }

        List<StatesDTO> result = parseFormStateEntityToStateDTO(stateRepository.findBySubdivisionContaining(subdivisionName));
        log.debug("Subdivisiones encontradas: {} registros", result.size());
        return result;
    }
}
