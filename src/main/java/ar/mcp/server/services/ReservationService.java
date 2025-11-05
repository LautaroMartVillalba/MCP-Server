package ar.mcp.server.services;

import ar.mcp.server.domain.dto.*;
import ar.mcp.server.domain.entities.Person;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.enums.RoomBookingStatus;
import ar.mcp.server.domain.enums.RoomState;
import ar.mcp.server.repositories.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service layer for managing {@link Reservation} entities.
 * Provides methods to create, retrieve, and query reservations
 * based on number of people, number of nights, or by ID.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PersonService personService;
    private final RoomService roomService;
    private final RoomBookingPeriodService roomBookingPeriodService;

    public ReservationService(ReservationRepository reservationRepository, PersonService personService, RoomService roomService, RoomBookingPeriodService roomBookingPeriodService) {
        this.reservationRepository = reservationRepository;
        this.personService = personService;
        this.roomService = roomService;
        this.roomBookingPeriodService = roomBookingPeriodService;
    }

    /**
     * Validates that an ID is valid for the given entity.
     *
     * @param id     ID to validate.
     * @param entity Entity name used in exception messages.
     * @throws RuntimeException if the ID is null or less than 1.
     */
    private void validateId(Long id, String entity){
        if (id == null || id < 1){
            throw new RuntimeException(entity + " id cannot be null or less than zero.");
        }
    }

    /**
     * Validates reservation dates and returns the number of nights.
     *
     * @param startAt Start date of the reservation.
     * @param endAt   End date of the reservation.
     * @return Number of nights between start and end date.
     * @throws RuntimeException if dates are null or invalid.
     */
    private Long validateAndGetReservationDate(LocalDate startAt, LocalDate endAt){
        if (startAt == null || endAt == null){
            throw new RuntimeException("Both parameters cannot be null.");
        }
        if (endAt.isBefore(startAt)){
            throw new RuntimeException("Please, insert a valid reservation date.");
        }
        return ChronoUnit.DAYS.between(startAt, endAt);
    }

    /**
     * Validates that the number of people is within the allowed range.
     *
     * @param numberOfPeople Number of people for the reservation.
     * @throws RuntimeException if number of people is less than 1 or greater than 4.
     */
    private void validateNumberOfPeople(Long numberOfPeople){
        if (numberOfPeople < 1 || numberOfPeople > 4){
            throw new RuntimeException("A room can only accommodate one to four people.");
        }
    }

    /**
     * Checks if the target room is available for reservation during the given dates.
     *
     * @param roomId  ID of the room to check.
     * @param startAt Start date.
     * @param endAt   End date.
     * @throws RuntimeException if the room is already reserved during the given period.
     */
    private void validateIfTargetRoomIsReserved(Long roomId, LocalDate startAt, LocalDate endAt){
        List<RoomDTO> freeRoomsInReservationDate = roomService.getFreeRoomsByScheduleBetween(startAt, endAt);

        if (freeRoomsInReservationDate.stream().noneMatch(room -> Objects.equals(room.getId(), roomId))){
            throw new RuntimeException("Selected room is not available to reserve between the received date.");
        }
    }

    /**
     * Converts a list of {@link Reservation} entities into a list of {@link ReservationDTO}.
     *
     * @param list List of {@link Reservation} entities.
     * @return List of {@link ReservationDTO}.
     */
    List<ReservationDTO> convertFromEntityListToDTOList(List<Reservation> list){
        if (list.isEmpty()){
            return new ArrayList<>();
        }

        List<ReservationDTO> response = new ArrayList<>();

        list.forEach(res -> {
            ReservationDTO transfer = ReservationDTO.builder()
                    .numberOfPeople(res.getNumberOfPeople())
                    .numberOfNights(res.getNumberOfNights())
                    .endAt(res.getEndAt())
                    .startAt(res.getStartAt())
                    .personId(res.getClient().getId())
                    .totalPrice(res.getTotalPrice())
                    .roomBookedId(res.getRoomBooked().getId()).build();

            response.add(transfer);
        });

        return response;
    }

    /**
     * Creates a new {@link Reservation} entity, associates it with a person and a room,
     * and updates the room booking period and room state accordingly.
     *
     * @param dto        {@link ReservationDTO} containing reservation data.
     * @param personDTO  {@link PersonDTO} containing person data if a new person is created.
     * @param addressDTO {@link AddressDTO} containing the person's address if a new person is created.
     * @return The created {@link Reservation} entity.
     * @throws RuntimeException if validation fails for people, dates, or room availability.
     */
    @Transactional
    public Reservation createReservation(ReservationDTO dto, PersonDTO personDTO, AddressDTO addressDTO) {
        //Validate the received data
        Long numberOfNights = validateAndGetReservationDate(dto.getStartAt(), dto.getEndAt());
        validateNumberOfPeople(dto.getNumberOfPeople());
        validateIfTargetRoomIsReserved(dto.getRoomBookedId(), dto.getStartAt(), dto.getEndAt());

        //Search relationship objects
        Person client = personService.getPersonByIdObject(dto.getPersonId()).orElseGet(() -> personService.createPerson(personDTO, addressDTO));
        Room room = roomService.getRoomById(dto.getRoomBookedId());

        //Create new reservation
        Reservation reservation = Reservation.builder()
                .numberOfPeople(dto.getNumberOfPeople())
                .numberOfNights(numberOfNights)
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .totalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights)))
                .client(client)
                .roomBooked(room).build();

        //Save reservation and save on an object to access to the ID value.
        Reservation reservationSavedInDB = reservationRepository.save(reservation);

        //Immediately create a RoomBookingRegister to with the reservation information
        RoomBookingPeriodDTO roomBookingRegister  = RoomBookingPeriodDTO.builder()
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .status(RoomBookingStatus.RESERVED)
                .roomId(room.getId())
                .reservationId(reservationSavedInDB.getId()).build();
        roomBookingPeriodService.create(roomBookingRegister);

        //Change the Room State from FREE to RESERVED
        roomService.changeRoomState(dto.getRoomBookedId(), RoomState.RESERVED);

        return reservation;
    }

    /**
     * Retrieves a {@link Reservation} entity by its ID.
     *
     * @param id ID of the reservation.
     * @return {@link Reservation} entity.
     * @throws RuntimeException        if the ID is null or invalid.
     * @throws RuntimeException if the reservation cannot be found.
     */
    public Reservation getById(Long id) {
        validateId(id, "Reservation");

        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    /**
     * Retrieves a {@link ReservationDTO} by reservation ID.
     *
     * @param id ID of the reservation.
     * @return {@link ReservationDTO} representing the reservation.
     * @throws RuntimeException        if the ID is null or invalid.
     * @throws RuntimeException if the reservation cannot be found.
     */
    public ReservationDTO getByIdResponse(Long id) {
        validateId(id, "Reservation");

        Reservation result = reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
        
        return ReservationDTO.builder()
                .numberOfPeople(result.getNumberOfPeople())
                .numberOfNights(result.getNumberOfNights())
                .endAt(result.getEndAt())
                .startAt(result.getStartAt())
                .personId(result.getClient().getId())
                .totalPrice(result.getTotalPrice())
                .roomBookedId(result.getRoomBooked().getId()).build();
    }

    /**
     * Retrieves all reservations with the specified number of people.
     *
     * @param people Number of people in the reservation.
     * @return List of {@link ReservationDTO} matching the number of people.
     * @throws RuntimeException if the number of people is not between 1 and 4.
     */
    public List<ReservationDTO> getByNumberOfPeople(int people) {
        if (people < 1 || people > 4) {
            throw new RuntimeException("Number of people must be between 1 and 4 people.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByNumberOfPeople(people));
    }

    /**
     * Retrieves all reservations with the specified number of nights.
     *
     * @param nights Number of nights in the reservation.
     * @return List of {@link ReservationDTO} matching the number of nights.
     * @throws RuntimeException if the number of nights is less than 1.
     */
    public List<ReservationDTO> getByNumberOfNight(int nights) {
        if (nights < 1) {
            throw new RuntimeException("A reservation must be at least at 1 night.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByNumberOfNights(nights));
    }

    /**
     * Retrieves all reservations matching both the specified number of people and nights.
     *
     * @param people Number of people for the reservation.
     * @param night  Number of nights for the reservation.
     * @return List of {@link ReservationDTO} matching both criteria.
     * @throws RuntimeException if the number of people is not between 1 and 4 or nights is less than 1.
     */
    public List<ReservationDTO> getByPeopleAndNights(int people, int night) {
        if (people < 1 || people > 4) {
            throw new RuntimeException("Number of people must be between 1 and 4 people.");
        }
        if (night < 1) {
            throw new RuntimeException("A reservation must be at least at 1 night.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByNumberOfPeopleAndNumberOfNights(people, night));
    }

    /**
     * Retrieves all reservations starting on or after the specified date.
     *
     * @param date Start date filter.
     * @return List of {@link ReservationDTO} starting on or after the given date.
     * @throws RuntimeException if the date is null.
     */
    public List<ReservationDTO> getByStartIn(LocalDate date) {
        if (date == null) {
            throw new RuntimeException("Date cannot be null.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByStartAtGreaterThan(date));
    }

    /**
     * Retrieves all reservations ending on or before the specified date.
     *
     * @param date End date filter.
     * @return List of {@link ReservationDTO} ending on or before the given date.
     * @throws RuntimeException if the date is null.
     */
    public List<ReservationDTO> getByFinishIn(LocalDate date) {
        if (date == null) {
            throw new RuntimeException("Date cannot be null.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByEndAtLessThan(date));
    }

    /**
     * Retrieves all reservations between the specified start and end dates.
     *
     * @param start Start date of the period.
     * @param end   End date of the period.
     * @return List of {@link ReservationDTO} between the given dates.
     * @throws RuntimeException if either date is null.
     */
    public List<ReservationDTO> getByBetweenDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new RuntimeException("Date cannot be null.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByStartAtGreaterThanAndEndAtLessThan(start, end));
    }

    /**
     * Retrieves all reservations associated with a specific room.
     *
     * @param roomId ID of the room.
     * @return List of {@link ReservationDTO} for the specified room.
     * @throws RuntimeException if the room ID is less than 1.
     */
    public List<ReservationDTO> getByRoom(Long roomId) {
        if (roomId < 1) {
            throw new RuntimeException("Id cannot be null.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByRoom(roomId));
    }

    /**
     * Retrieves all reservations associated with a specific client.
     *
     * @param clientId ID of the client.
     * @return List of {@link ReservationDTO} for the specified client.
     * @throws RuntimeException if the client ID is less than 1.
     */
    public List<ReservationDTO> getByClient(Long clientId) {
        if (clientId < 1) {
            throw new RuntimeException("Id cannot be null.");
        }

        return convertFromEntityListToDTOList(reservationRepository.findByPerson(clientId));
    }

    /**
     * Updates an existing reservation with the provided {@link ReservationDTO}.
     * Validates the room availability, number of people, and reservation dates.
     *
     * @param reservationId ID of the reservation to update.
     * @param dto           {@link ReservationDTO} containing updated data.
     * @return Updated {@link Reservation} entity.
     * @throws RuntimeException        if validation fails for people, dates, or room availability.
     * @throws RuntimeException if the reservation cannot be found.
     */
    @Transactional
    public Reservation update(Long reservationId, ReservationDTO dto) {
        validateId(reservationId, "Reservation");

        Reservation reservationInDB = reservationRepository.findById(reservationId).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
        if (dto.getRoomBookedId() > 0) {
            validateIfTargetRoomIsReserved(dto.getRoomBookedId(), dto.getStartAt(), dto.getEndAt());
            Room room = roomService.getRoomById(dto.getRoomBookedId());
            reservationInDB.setRoomBooked(room);
        }
        if (dto.getNumberOfNights() > 0) {
            validateIfTargetRoomIsReserved(reservationInDB.getRoomBooked().getId(), dto.getStartAt(), dto.getEndAt());
            reservationInDB.setNumberOfNights(dto.getNumberOfNights());
        }
        if (dto.getNumberOfPeople() > 0 && dto.getNumberOfPeople() < 5) {
            reservationInDB.setNumberOfPeople(dto.getNumberOfPeople());
        }
        if (dto.getStartAt() != null) {
            validateIfTargetRoomIsReserved(dto.getRoomBookedId(), dto.getStartAt(), dto.getEndAt());
            reservationInDB.setStartAt(dto.getStartAt());
        }
        if (dto.getEndAt() != null) {
            validateIfTargetRoomIsReserved(dto.getRoomBookedId(), dto.getStartAt(), dto.getEndAt());
            reservationInDB.setEndAt(dto.getEndAt());
        }
        // Validate the final state of the updated reservation
        validateAndGetReservationDate(reservationInDB.getStartAt(), reservationInDB.getEndAt());
        validateNumberOfPeople(reservationInDB.getNumberOfPeople());

        return reservationInDB;
    }

    /**
     * Deletes a reservation by its ID.
     * If the reservation is currently active (start date before today and end date after today),
     * it cannot be deleted. Frees the associated room after deletion.
     *
     * @param reservationId ID of the reservation to delete.
     * @throws RuntimeException        if the reservation is active or ID is invalid.
     * @throws RuntimeException if the reservation cannot be found.
     */
    @Transactional
    public void delete(Long reservationId) {
        validateId(reservationId, "Reservation");

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        if (reservation.getStartAt().isBefore(LocalDate.now()) && reservation.getEndAt().isAfter(LocalDate.now())){
            throw new RuntimeException("The reservation is actually available. Cannot be deleted.");
        }
        roomService.changeRoomState(reservation.getRoomBooked().getId(), RoomState.FREE);

        reservationRepository.delete(reservation);
    }

}
