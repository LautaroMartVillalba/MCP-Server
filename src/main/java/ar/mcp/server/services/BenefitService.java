package ar.mcp.server.services;

import ar.mcp.server.domain.dto.BenefitDTO;
import ar.mcp.server.domain.entities.Benefit;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.repositories.BenefitRepository;
import ar.mcp.server.repositories.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            Hotel hotel = benefit.getHotel();

            return BenefitDTO.builder()
                    .name(benefit.getName())
                    .description(benefit.getDescription())
                    .openAt(benefit.getOpenAt())
                    .closeAt(benefit.getCloseAt())
                    .hotelId(hotel != null ? hotel.getId() : null).build();
        }).toList();
    }

    /**
     * Creates a new {@link Benefit} entity in the database.
     *
     * @param dto Data transfer object containing the information for the benefit.
     * @return The created {@link Benefit} entity.
     * @throws RuntimeException if the associated hotel cannot be found.
     * @throws RuntimeException        if required fields are missing.
     */
    @Transactional
    public Benefit createBenefit(BenefitDTO dto){
        validateInfo(dto.getName(), dto.getDescription(), dto.getOpenAt(), dto.getCloseAt());
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        Benefit benefit = Benefit.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .openAt(dto.getOpenAt())
                .closeAt(dto.getCloseAt())
                .hotel(hotel).build();

        benefitRepository.save(benefit);

        return benefit;
    }

    /**
     * Retrieves a {@link BenefitDTO} by its ID.
     *
     * @param id ID of the benefit.
     * @return {@link BenefitDTO} representing the benefit.
     * @throws RuntimeException        if ID is invalid.
     * @throws RuntimeException if the benefit cannot be found.
     */
    public BenefitDTO getBenefitByIdResponse(Long id){
        if (id == 0){
            throw new RuntimeException("Insert a valid id number.");
        }

        Benefit result = benefitRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        return BenefitDTO.builder()
                .name(result.getName())
                .description(result.getDescription())
                .openAt(result.getOpenAt())
                .closeAt(result.getCloseAt())
                .hotelId(result.getHotel().getId()).build();
    }

    /**
     * Retrieves a {@link Benefit} entity by its ID.
     *
     * @param id ID of the benefit.
     * @return {@link Benefit} entity.
     * @throws RuntimeException        if ID is invalid.
     * @throws RuntimeException if the benefit cannot be found.
     */
    public Benefit getBenefitByIdObject(Long id) {
        if (id == 0 || id < 1) {
            throw new RuntimeException("Insert a valid id number.");
        }

        return benefitRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    /**
     * Finds benefits whose name contains the given string.
     *
     * @param name Name filter.
     * @return List of {@link BenefitDTO}.
     * @throws RuntimeException if name is empty.
     */
    public List<BenefitDTO> getBenefitByName(String name){
        if (name.isBlank()){
            throw new RuntimeException("Name cannot be null.");
        }

        return parseBenefitListToBenefitDTOList(benefitRepository.findByNameContaining(name));
    }

    /**
     * Finds benefits whose description contains the given string.
     *
     * @param desc Description filter.
     * @return List of {@link BenefitDTO}.
     * @throws RuntimeException if description is empty.
     */
    public List<BenefitDTO> getBenefitByDescription(String desc){
        if (desc.isBlank()){
            throw new RuntimeException("Description cannot be null.");
        }

        return parseBenefitListToBenefitDTOList(benefitRepository.findByDescriptionContaining(desc));
    }

    /**
     * Finds benefits that open after the given time.
     *
     * @param opening Opening time filter.
     * @return List of {@link BenefitDTO}.
     * @throws RuntimeException if opening time is null.
     */
    public List<BenefitDTO> getBenefitByOpening(LocalTime opening){
        if (opening == null){
            throw new RuntimeException("Opening time cannot be null.");
        }

        return parseBenefitListToBenefitDTOList(benefitRepository.findByOpenAtGreaterThan(opening));
    }

    /**
     * Finds benefits that close before the given time.
     *
     * @param ending Closing time filter.
     * @return List of {@link BenefitDTO}.
     * @throws RuntimeException if closing time is null.
     */
    public List<BenefitDTO> getBenefitByEnding(LocalTime ending){
        if (ending == null){
            throw new RuntimeException("ending time cannot be null.");
        }

        return parseBenefitListToBenefitDTOList(benefitRepository.findByCloseAtLessThan(ending));
    }

    /**
     * Finds benefits whose opening and closing times are within the given range.
     *
     * @param open  Minimum opening time.
     * @param close Maximum closing time.
     * @return List of {@link BenefitDTO}.
     * @throws RuntimeException if any of the parameters are null.
     */
    public List<BenefitDTO> getByOpenBetween(LocalTime open, LocalTime close){
        if (open == null || close == null){
            throw new RuntimeException("Both ending or opening cannot be null.");
        }

        return parseBenefitListToBenefitDTOList(benefitRepository.findByOpenAtGreaterThanEqualAndCloseAtLessThanEqual(open, close));
    }

    /**
     * Retrieves all benefits associated with a specific hotel.
     *
     * @param hotelId ID of the hotel.
     * @return List of {@link BenefitDTO}.
     * @throws RuntimeException if hotelId is null.
     */
    public List<BenefitDTO> getByHotelId(Long hotelId){
        if (hotelId == null){
            throw new RuntimeException("Id cannot be null");
        }

        return parseBenefitListToBenefitDTOList(benefitRepository.findByHotel(hotelId));
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
    @Transactional
    public Benefit updateBenefit(Long id, BenefitDTO dto){
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
    @Transactional
    public void deleteBenefitById(Long id){
        Benefit benefitInDB = this.getBenefitByIdObject(id);

        if (benefitInDB.getOpenAt().isBefore(LocalTime.now()) && benefitInDB.getCloseAt().isAfter(LocalTime.now())){
            throw new RuntimeException("Cannot delete a Benefit when is working.");
        }

        benefitRepository.deleteById(id);
    }

}
