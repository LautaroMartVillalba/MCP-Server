package ar.mcp.server.services;

import ar.mcp.server.domain.dto.RoomBookingPeriodDTO;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.entities.RoomBookingPeriod;
import ar.mcp.server.domain.enums.RoomBookingStatus;
import ar.mcp.server.repositories.ReservationRepository;
import ar.mcp.server.repositories.RoomBookingPeriodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class to manage RoomBookingPeriod entities.
 * Provides methods to create, retrieve, update, and delete room booking periods.
 */
@Service
public class RoomBookingPeriodService {

    private final RoomBookingPeriodRepository repository;
    private final RoomService roomService;
    private final ReservationRepository reservationRepository;

    public RoomBookingPeriodService(RoomBookingPeriodRepository repository, RoomService roomService, ReservationRepository reservationRepository) {
        this.repository = repository;
        this.roomService = roomService;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Validates the parameters for creating or updating a RoomBookingPeriod.
     *
     * @param startAt       Start date of the booking period.
     * @param endAt         End date of the booking period.
     * @param roomId        ID of the associated room.
     * @param reservationId ID of the associated reservation.
     * @throws RuntimeException if any parameter is invalid or dates are in the past.
     */
    private void validate(LocalDate startAt, LocalDate endAt, Long roomId, Long reservationId){
        if (startAt.isBefore(LocalDate.now()) || endAt.isBefore(LocalDate.now())){
            throw new RuntimeException("Cannot create registers in the past.");
        }
        if (roomId == null || roomId < 1 || reservationId == null || reservationId < 1){
            throw new RuntimeException("Insert both room and reservation id numbers.");
        }
        if (startAt.isBefore(LocalDate.now()) || endAt.isBefore(startAt)){
            throw new RuntimeException("Please, insert a valid reservation date.");
        }
    }

    /**
     * Retrieves a RoomBookingPeriod entity by its ID.
     *
     * @param id ID of the RoomBookingPeriod.
     * @return {@link RoomBookingPeriod} entity.
     * @throws RuntimeException        if the ID is null or less than 1.
     * @throws RuntimeException if the entity cannot be found.
     */
    public RoomBookingPeriod getByIdObject (Long id){
        if (id == null || id < 1){
            throw new RuntimeException("Insert a valid id.");
        }

        return repository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    /**
     * Retrieves a RoomBookingPeriod as a DTO by its ID.
     *
     * @param id ID of the RoomBookingPeriod.
     * @return {@link RoomBookingPeriodDTO} with all details.
     * @throws RuntimeException        if the ID is null or less than 1.
     * @throws RuntimeException if the entity cannot be found.
     */
    public RoomBookingPeriodDTO getByIdResponse (Long id){
        if (id == null || id < 1){
            throw new RuntimeException("Insert a valid id.");
        }

        RoomBookingPeriod result = repository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        return RoomBookingPeriodDTO.builder()
                .startAt(result.getStartAt())
                .endAt(result.getEndAt())
                .status(result.getStatus())
                .roomId(result.getRoom().getId())
                .reservationId(result.getReservation().getId()).build();
    }

    /**
     * Converts a list of RoomBookingPeriod entities to a list of DTOs.
     *
     * @param list List of {@link RoomBookingPeriod} entities.
     * @return List of {@link RoomBookingPeriodDTO}.
     */
    private List<RoomBookingPeriodDTO> convertEntityToDTO(List<RoomBookingPeriod> list){
        return list.stream().map(roomBookingPeriod ->
                RoomBookingPeriodDTO.builder()
                .startAt(roomBookingPeriod.getStartAt())
                .endAt(roomBookingPeriod.getEndAt())
                .status(roomBookingPeriod.getStatus())
                .roomId(roomBookingPeriod.getRoom().getId())
                .reservationId(roomBookingPeriod.getReservation().getId())
                .build()).toList();
    }

    /**
     * Creates a new RoomBookingPeriod.
     *
     * @param dto {@link RoomBookingPeriodDTO} containing booking details.
     * @return The saved {@link RoomBookingPeriod} entity.
     * @throws RuntimeException if validation fails.
     */
    @Transactional
    public RoomBookingPeriod create(RoomBookingPeriodDTO dto){
        validate(dto.getStartAt(), dto.getEndAt(), dto.getRoomId(), dto.getReservationId());
        Room room = roomService.getRoomById(dto.getRoomId());
        Reservation reservation = reservationRepository.findById(dto.getReservationId()).orElseThrow();

        RoomBookingPeriod register = RoomBookingPeriod.builder()
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .status(RoomBookingStatus.RESERVED)
                .room(room)
                .reservation(reservation).build();

        repository.save(register);

        return register;
    }

    /**
     * Retrieves all RoomBookingPeriods starting after a given date.
     *
     * @param startAt Start date filter.
     * @return List of {@link RoomBookingPeriodDTO}.
     * @throws RuntimeException if the date is null.
     */
    public List<RoomBookingPeriodDTO> getByStartAt(LocalDate startAt){
        if (startAt == null){
            throw new RuntimeException("Date cannot be null.");
        }

        return convertEntityToDTO(repository.findByStartAtGreaterThan(startAt));
    }

    /**
     * Retrieves all RoomBookingPeriods ending before a given date.
     *
     * @param endAt End date filter.
     * @return List of {@link RoomBookingPeriodDTO}.
     * @throws RuntimeException if the date is null or before today.
     */
    public List<RoomBookingPeriodDTO> getByEndAt(LocalDate endAt){
        if (endAt == null || endAt.isBefore(LocalDate.now())){

            throw new RuntimeException("Date cannot be null.");
        }

        return convertEntityToDTO(repository.findByEndAtLessThan(endAt));
    }

    /**
     * Retrieves all RoomBookingPeriods between the specified start and end dates.
     *
     * @param startAt Start date.
     * @param endAt   End date.
     * @return List of {@link RoomBookingPeriodDTO}.
     * @throws RuntimeException if dates are invalid or in the wrong order.
     */
    public List<RoomBookingPeriodDTO> getByStarAndEndBetween(LocalDate startAt, LocalDate endAt){
        if (startAt == null || endAt == null || endAt.isBefore(LocalDate.now()) || startAt.isAfter(endAt)){
            throw new RuntimeException("Start date have to be before end date. Please, insert dates data correctly.");
        }

        return convertEntityToDTO(repository.findByStartAtGreaterThanAndEndAtLessThan(startAt, endAt));
    }

    /**
     * Retrieves all RoomBookingPeriods filtered by status.
     *
     * @param status {@link RoomBookingStatus} to filter by.
     * @return List of {@link RoomBookingPeriodDTO}.
     * @throws RuntimeException if status is null.
     */
    public List<RoomBookingPeriodDTO> getByStatusRegister(RoomBookingStatus status){
        if (status == null){
            throw new RuntimeException("Please, set a valid status.");
        }

        return convertEntityToDTO(repository.findByStatus(status));
    }

    /**
     * Retrieves all RoomBookingPeriods for a specific room.
     *
     * @param roomId ID of the room.
     * @return List of {@link RoomBookingPeriodDTO}.
     * @throws RuntimeException if roomId is null or invalid.
     */
    public List<RoomBookingPeriodDTO> getByRoomId(Long roomId){
        if (roomId == null || roomId < 1){
            throw new RuntimeException("Insert a valid room id.");
        }

        return convertEntityToDTO(repository.findByRoom(roomId));
    }

    /**
     * Updates the details (dates or room) of a RoomBookingPeriod.
     *
     * @param roomBookingPeriodId ID of the period to update.
     * @param dto                 {@link RoomBookingPeriodDTO} with updated details.
     * @return Updated {@link RoomBookingPeriodDTO}.
     * @throws RuntimeException if validation fails.
     */
    @Transactional
    public RoomBookingPeriodDTO updateInfo(Long roomBookingPeriodId, RoomBookingPeriodDTO dto){
        RoomBookingPeriod registerInDB = this.getByIdObject(roomBookingPeriodId);

        if (dto.getStartAt() != null){
            registerInDB.setStartAt(dto.getStartAt());
        }
        if (dto.getEndAt() != null){
            registerInDB.setEndAt(dto.getEndAt());
        }
        if (dto.getRoomId() != null && dto.getRoomId() > 0){
            Room roomInDB = roomService.getRoomById(dto.getRoomId());
            registerInDB.setRoom(roomInDB);
        }

        validate(registerInDB.getStartAt(), registerInDB.getEndAt(), registerInDB.getRoom().getId(), registerInDB.getReservation().getId());

        repository.save(registerInDB);

        return RoomBookingPeriodDTO.builder()
                .startAt(registerInDB.getStartAt())
                .endAt(registerInDB.getEndAt())
                .roomId(registerInDB.getRoom().getId())
                .reservationId(registerInDB.getReservation().getId()).build();
    }

    /**
     * Updates the status of a RoomBookingPeriod.
     *
     * @param roomBookingPeriodId ID of the period to update.
     * @param status              {@link RoomBookingStatus} to set.
     * @return Updated {@link RoomBookingPeriodDTO}.
     */
    @Transactional
    public RoomBookingPeriodDTO updateStatus(Long roomBookingPeriodId, RoomBookingStatus status){
        RoomBookingPeriod registerInDB = this.getByIdObject(roomBookingPeriodId);

        if (status != null){
            registerInDB.setStatus(status);
        }

        repository.save(registerInDB);

        return RoomBookingPeriodDTO.builder()
                .startAt(registerInDB.getStartAt())
                .endAt(registerInDB.getEndAt())
                .status(registerInDB.getStatus())
                .roomId(registerInDB.getRoom().getId())
                .reservationId(registerInDB.getReservation().getId()).build();
    }

    /**
     * Deletes a RoomBookingPeriod by its ID.
     * Only periods with status CANCELED or COMPLETED can be deleted.
     *
     * @param id ID of the RoomBookingPeriod to delete.
     * @throws RuntimeException        if the period cannot be deleted or ID is invalid.
     */
    @Transactional
    public void delete (Long id){
        if (id == null || id < 1){
            throw new RuntimeException("Id cannot be null.");
        }

        RoomBookingPeriod resultInDB = this.getByIdObject(id);

        if (resultInDB.getStatus() == RoomBookingStatus.RESERVED || resultInDB.getStatus() == RoomBookingStatus.COMPLETED){
            throw new RuntimeException("Only canceled or completes reservations register can be deleted.");
        }

        repository.delete(resultInDB);
    }

}