package ar.mcp.server.services;

import ar.mcp.server.domain.dto.AttractionDTO;
import ar.mcp.server.domain.entities.Attraction;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.repositories.AttractionRepository;
import ar.mcp.server.repositories.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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
            Hotel hotel = attraction.getHotel();

            return AttractionDTO.builder()
                    .name(attraction.getName())
                    .description(attraction.getDescription())
                    .peopleCapacity(attraction.getPeopleCapacity())
                    .openAt(attraction.getOpenAt())
                    .closeAt(attraction.getCloseAt())
                    .hotelId(hotel != null ? hotel.getId() : null).build();
        }).toList();
    }

    /**
     * Creates a new {@link Attraction} and associates it with a {@link Hotel}.
     *
     * @param dto     {@link AttractionDTO} containing attraction data.
     * @param hotelId ID of the hotel to associate the attraction with.
     * @return The created {@link Attraction} entity.
     * @throws RuntimeException if required fields are missing.
     * @throws RuntimeException if the hotel does not exist.
     */
    @Transactional
    public Attraction createAttraction(AttractionDTO dto, Long hotelId) {
        validateInfo(dto.getName(), dto.getDescription(), dto.getPeopleCapacity(), dto.getOpenAt(), dto.getCloseAt());

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        Attraction attraction = Attraction.builder()
                .name(dto.getName())
                .peopleCapacity(dto.getPeopleCapacity())
                .openAt(dto.getOpenAt())
                .closeAt(dto.getCloseAt())
                .hotel(hotel).build();

        attractionRepository.save(attraction);

        return attraction;
    }

    /**
     * Retrieves an {@link AttractionDTO} by attraction ID.
     *
     * @param id ID of the attraction.
     * @return {@link AttractionDTO} corresponding to the provided ID.
     * @throws RuntimeException        if ID is null or invalid.
     * @throws RuntimeException if no attraction is found.
     */
    public AttractionDTO getAttractionByIdDTO(Long id) {
        if (id == null || id < 1) {
            throw new RuntimeException("Id cannot be null.");
        }

        Attraction attractionInDB = attractionRepository.findById(id).orElseThrow(()-> new RuntimeException("Attraction cannot be found by id."));

        return AttractionDTO.builder()
                .name(attractionInDB.getName())
                .description(attractionInDB.getDescription())
                .peopleCapacity(attractionInDB.getPeopleCapacity())
                .openAt(attractionInDB.getOpenAt())
                .closeAt(attractionInDB.getCloseAt()).build();
    }

    /**
     * Retrieves an {@link Attraction} entity by its ID.
     *
     * @param id ID of the attraction. Must be greater than 0.
     * @return {@link Attraction} entity.
     * @throws RuntimeException        if ID is invalid.
     * @throws RuntimeException if the attraction is not found.
     */
    public Attraction getAttractionByIdObject(Long id) {
        if (id <= 0) {
            throw new RuntimeException("Id cannot be null.");
        }

        return attractionRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    /**
     * Retrieves a list of attractions whose names contain the specified string.
     *
     * @param name Partial or full name of the attraction. Must not be blank.
     * @return List of {@link AttractionDTO} matching the search criteria.
     * @throws RuntimeException if name is blank.
     */
    public List<AttractionDTO> getAttractionByName(String name) {
        if (name.isBlank()) {
            throw new RuntimeException("Name parameter cannot be empty.");
        }

        return parseFromAttractionListToAttractionDTOList(attractionRepository.findByNameContaining(name));
    }

    /**
     * Retrieves a list of attractions whose descriptions contain the specified string.
     *
     * @param desc Partial or full description of the attraction. Must not be blank.
     * @return List of {@link AttractionDTO} matching the description.
     * @throws RuntimeException if description is blank.
     */
    List<AttractionDTO> getAttractionByDesc(String desc) {
        if (desc.isBlank()) {
            throw new RuntimeException("Description cannot be null.");
        }

        return parseFromAttractionListToAttractionDTOList(attractionRepository.findByDescriptionContaining(desc));
    }

    /**
     * Retrieves a list of attractions whose capacities fall within a given range.
     *
     * @param min Minimum number of people. Must be > 0.
     * @param max Maximum number of people. Must be >= min.
     * @return List of {@link AttractionDTO} matching the capacity range.
     * @throws RuntimeException if the values are invalid.
     */
    List<AttractionDTO> getAttractionByCapacity(int min, int max) {
        if (min <= 0 || min > max) {
            throw new RuntimeException("Insert valid minimum and maximum values.");
        }

        return parseFromAttractionListToAttractionDTOList(attractionRepository.findByPeopleCapacityBetween(min, max));
    }

    /**
     * Retrieves a list of attractions that open after a specified time.
     *
     * @param time Opening time filter. Must not be null.
     * @return List of {@link AttractionDTO} that open after the given time.
     * @throws RuntimeException if time is null.
     */
    List<AttractionDTO> getAttractionByOpening(LocalTime time) {
        if (time == null) {
            throw new RuntimeException("Invalid time format.");
        }

        return parseFromAttractionListToAttractionDTOList(attractionRepository.findByOpenAtGreaterThan(time));
    }

    /**
     * Retrieves a list of attractions that close before a specified time.
     *
     * @param time Closing time filter. Must not be null.
     * @return List of {@link AttractionDTO} that close before the given time.
     * @throws RuntimeException if time is null.
     */
    List<AttractionDTO> getAttractionByEnding(LocalTime time) {
        if (time == null) {
            throw new RuntimeException("Invalid time format.");
        }

        return parseFromAttractionListToAttractionDTOList(attractionRepository.findByCloseAtLessThan(time));
    }

    /**
     * Retrieves a list of attractions that open and close within a specified time range.
     *
     * @param opening Start time filter. Must not be null.
     * @param ending  End time filter. Must not be null.
     * @return List of {@link AttractionDTO} within the specified opening and closing times.
     * @throws RuntimeException if any time parameter is null.
     */
    List<AttractionDTO> getAttractionBetweenOpeningAndEnding(LocalTime opening, LocalTime ending) {
        if (opening == null || ending == null) {
            throw new RuntimeException("Invalid time format.");
        }

        return parseFromAttractionListToAttractionDTOList(attractionRepository.findByOpenAtGreaterThanEqualAndCloseAtLessThanEqual(opening, ending));
    }

    /**
     * Retrieves a list of attractions associated with a specific hotel.
     *
     * @param hotelId ID of the hotel. Must not be null.
     * @return List of {@link AttractionDTO} belonging to the hotel.
     * @throws RuntimeException if hotelId is null.
     */
    List<AttractionDTO> getByHotelId(Long hotelId){
        if (hotelId == null){
            throw new RuntimeException("Id cannot be null.");
        }

        return parseFromAttractionListToAttractionDTOList(attractionRepository.findByHotel(hotelId));
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
    @Transactional
    Attraction updateAttraction(Long id, AttractionDTO dto) {
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
    @Transactional
    public void deleteAttraction(Long id) {
        Attraction attractionInDB = this.getAttractionByIdObject(id);

        LocalTime opening = attractionInDB.getOpenAt();
        LocalTime ending = attractionInDB.getCloseAt();

        if (opening.isBefore(LocalTime.now()) && ending.isAfter(LocalTime.now())){
            throw new RuntimeException("Cannot delete an attraction when is working.");
        }

        attractionRepository.deleteById(id);
    }

}
