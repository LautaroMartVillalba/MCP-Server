package ar.mcp.server.services;


import ar.mcp.server.domain.dto.StatesDTO;
import ar.mcp.server.domain.entities.address.States;
import ar.mcp.server.repositories.StateRepository;
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

    public StatesService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    /**
     * Converts a list of {@link States} entities into a list of {@link StatesDTO}.
     *
     * @param list List of States entities.
     * @return List of StatesDTO objects with mapped properties.
     */
    private List<StatesDTO> parseFormStateEntityToStateDTO(List<States> list){
        return list.stream().map(states -> StatesDTO.builder()
                                                           .code(states.getCode())
                                                           .subdivision(states.getSubdivision())
                                                           .countryCode(states.getCountryCode()).build())
                .toList();
    }

    /**
     * Retrieves a {@link StatesDTO} for a given state code.
     *
     * @param stateCode Code of the state.
     * @return StatesDTO corresponding to the given state code.
     */
    public StatesDTO getStateByCodeResponse(String stateCode){
        if (stateCode.isBlank()){
            throw new RuntimeException("Insert a state' code, please.");
        }

        States states = stateRepository.findByCode(stateCode).orElseThrow(() -> new RuntimeException("The resource cannot be found in the DataBase."));

        return StatesDTO.builder()
                .code(states.getCode())
                .countryCode(states.getCountryCode())
                .subdivision(states.getSubdivision()).build();
    }

    /**
     * Retrieves a {@link States} entity for a given state code.
     *
     * @param stateCode Code of the state.
     * @return States entity corresponding to the given state code.
     */
    public States getStateByCodeObject(String stateCode){
        if (stateCode.isBlank()){
            throw new RuntimeException("Insert a state code, please.");
        }

        return stateRepository.findByCode(stateCode).orElseThrow(() -> new RuntimeException("The resource cannot be found in the DataBase."));
    }

    /**
     * Retrieves a list of {@link StatesDTO} for a given country code.
     *
     * @param countryCode Code of the country.
     * @return List of StatesDTO for the specified country code.
     */
    public List<StatesDTO> getStateByCountryCode(String countryCode){
        if (countryCode.isBlank()){
            throw new RuntimeException("Insert a country code, please.");
        }

        return parseFormStateEntityToStateDTO(stateRepository.findByCountryCode(countryCode));
    }

    /**
     * Retrieves a list of {@link StatesDTO} containing a specific subdivision name.
     *
     * @param subdivisionName Subdivision name or partial name.
     * @return List of StatesDTO matching the subdivision name.
     */
    public List<StatesDTO> getStateBySubdivisionName(String subdivisionName){
        if (subdivisionName.isBlank()){
            throw new RuntimeException("Insert a state subdivision same, please.");
        }

        return parseFormStateEntityToStateDTO(stateRepository.findBySubdivisionContaining(subdivisionName));
    }
}
