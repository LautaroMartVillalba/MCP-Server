package ar.mcp.server.services.reservation;

import ar.mcp.server.domain.dto.*;
import ar.mcp.server.domain.entities.Person;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.enums.RoomBookingStatus;
import ar.mcp.server.domain.enums.RoomState;
import ar.mcp.server.repositories.ReservationRepository;
import ar.mcp.server.services.room_booking_period.RoomBookingPeriodService;
import ar.mcp.server.services.room.RoomService;
import ar.mcp.server.services.person.PersonService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
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
                    .personId(res.getPerson().getId())
                    .totalPrice(res.getTotalPrice())
                    .roomBookedId(res.getRoomBooked().getId()).build();

            response.add(transfer);
        });

        return response;
    }

    /**
     * Creates a new {@link Reservation} entity for an existing person and a room,
     * and updates the room booking period and room state accordingly.
     *
     * @param dto      {@link ReservationDTO} containing reservation data.
     * @param personId ID of the existing person making the reservation.
     * @return The created {@link Reservation} entity.
     * @throws RuntimeException if validation fails for people, dates, or room availability.
     */
    @McpTool(
            name = "create_reservation",
            description = "Crea una nueva reserva asociando una persona, habitación y actualizando su disponibilidad."
    )
    @Transactional
    public Reservation createReservation(
            @ToolParam(
                    required = true,
                    description = "Objeto ReservationDTO con los datos de la reserva (roomBookedId, startAt, endAt, numberOfPeople)."
            ) ReservationDTO dto,
            @ToolParam(
                    required = true,
                    description = "ID de la persona que realiza la reserva. Debe ser una persona existente en el sistema."
            ) Long personId
    ) {
        //Validate the received data
        Long numberOfNights = validateAndGetReservationDate(dto.getStartAt(), dto.getEndAt());
        validateNumberOfPeople(dto.getNumberOfPeople());
        validateIfTargetRoomIsReserved(dto.getRoomBookedId(), dto.getStartAt(), dto.getEndAt());

        //Search relationship objects
        Person person = personService.getPersonByIdObject(personId)
                .orElseThrow(() -> new RuntimeException("Person with ID " + personId + " not found. Create the person first using create_person."));
        Room room = roomService.getRoomById(dto.getRoomBookedId());

        //Create new reservation
        Reservation reservation = Reservation.builder()
                .numberOfPeople(dto.getNumberOfPeople())
                .numberOfNights(numberOfNights)
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .totalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights)))
                .person(person)
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
    @McpTool(
            name = "get_reservation_by_id_object",
            description = "Obtiene una reserva completa por su ID y la devuelve como entidad Reservation."
    )
    public Reservation getById(
            @ToolParam(
                    required = true,
                    description = "ID de la reserva."
            ) Long id
    ) {
        validateId(id, "Reservation");

        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    @McpTool(
            name = "find_reservation_by_spec",
            description = """
                Realiza una búsqueda dinámica de reservaciones según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "id": null,
                  "startAt": null,
                  "endAt": null,
                  "numberOfNights": null,
                  "numberOfPeople": 4,
                  "totalPrice": null
                }

                En este ejemplo, solo se filtrarán las reservaciones que tengan 4 personas."""
    )
    public List<ReservationDTO> findReservationBySpec(
            @ToolParam(required = false,
                    description = "Identificador único del registro. No obligatorio.") Long id,
            @ToolParam(required = false,
                    description = "Fecha en la que inicia la reservación. No obligatorio.") LocalDate startAt,
            @ToolParam(required = false,
                    description = "Fecha en la que finaliza la reservación. No obligatorio.") LocalDate endAt,
            @ToolParam(required = false,
                    description = "Número total de noches que dura la reservación. No obligatorio.") Integer numberOfNights,
            @ToolParam(required = false,
                    description = "Cantidad de personas incluidas en la reservación. No obligatorio.") Integer numberOfPeople,
            @ToolParam(required = false,
                    description = "Precio total de la reservación. No obligatorio.") BigDecimal totalPrice
    ){
        Specification<Reservation> specification = Specification.unrestricted();

        specification = specification.and(ReservationSpecifications.hasId(id));
        specification = specification.and(ReservationSpecifications.hasStartAt(startAt));
        specification = specification.and(ReservationSpecifications.hasEndAt(endAt));
        specification = specification.and(ReservationSpecifications.hasNumberOfNights(numberOfNights));
        specification = specification.and(ReservationSpecifications.hasNumberOfPeople(numberOfPeople));
        specification = specification.and(ReservationSpecifications.hasTotalPrice(totalPrice));

        specification = specification.and(ReservationSpecifications.fetchEverythingForDTO());

        return convertFromEntityListToDTOList(reservationRepository.findAll(specification));
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
    @McpTool(
            name = "update_reservation",
            description = "Actualiza una reserva existente validando disponibilidad, cantidad de personas y fechas."
    )
    @Transactional
    public Reservation update(
            @ToolParam(
                    required = true,
                    description = "ID de la reserva a actualizar."
            ) Long reservationId,
            @ToolParam(
                    required = true,
                    description = "Objeto ReservationDTO con los datos actualizados."
            ) ReservationDTO dto
    ) {
        validateId(reservationId, "Reservation");

        Reservation reservationInDB = reservationRepository.findById(reservationId).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
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
    @McpTool(
            name = "delete_reservation",
            description = "Elimina una reserva por su ID, liberando la habitación si no se encuentra activa."
    )
    @Transactional
    public void delete(
            @ToolParam(
                    required = true,
                    description = "ID de la reserva a eliminar."
            ) Long reservationId
    ) {
        validateId(reservationId, "Reservation");

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        if (reservation.getStartAt().isBefore(LocalDate.now()) && reservation.getEndAt().isAfter(LocalDate.now())){
            throw new RuntimeException("The reservation is actually available. Cannot be deleted.");
        }
        roomService.changeRoomState(reservation.getRoomBooked().getId(), RoomState.FREE);

        reservationRepository.delete(reservation);
    }

}
