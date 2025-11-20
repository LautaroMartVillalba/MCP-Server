package ar.mcp.server.services.room_booking_period;

import ar.mcp.server.domain.dto.RoomBookingPeriodDTO;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.entities.RoomBookingPeriod;
import ar.mcp.server.domain.enums.RoomBookingStatus;
import ar.mcp.server.repositories.ReservationRepository;
import ar.mcp.server.repositories.RoomBookingPeriodRepository;
import ar.mcp.server.services.room.RoomService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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
    @McpTool(
            name = "get_room_booking_period_by_id_object",
            description = "Obtiene una entidad RoomBookingPeriod por su ID."
    )
    public RoomBookingPeriod getByIdObject (
            @ToolParam(required = true, description = """
            ID del registro RoomBookingPeriod a obtener.
            """)
            Long id
    ){
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
    @McpTool(
            name = "get_room_booking_period_by_id_response",
            description = "Obtiene un RoomBookingPeriod en formato DTO por su ID."
    )
    public RoomBookingPeriodDTO getByIdResponse (
            @ToolParam(required = true, description = """
            ID del registro RoomBookingPeriod a obtener en formato DTO.
            """)
            Long id
    ){
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
    @McpTool(
            name = "create_room_booking_period",
            description = "Crea un nuevo RoomBookingPeriod con los datos proporcionados."
    )
    @Transactional
    public RoomBookingPeriod create(
            @ToolParam(required = true, description = """
            Objeto DTO con los datos del nuevo RoomBookingPeriod.
            """)
            RoomBookingPeriodDTO dto
    ){
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

    @McpTool(
            name = "find_roombookingperiod_by_spec",
            description = """
                Realiza una búsqueda dinámica en los registros de habitaciones reservadas según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "id": null,
                  "status": "RESERVED",
                  "startAt": null,
                  "endAt": null
                }

                En este ejemplo, solo se filtrarán los registros que tengan el estado RESERVED."""
    )
    public List<RoomBookingPeriodDTO> findRoomBookingPeriodBySpec(
            @ToolParam(required = false,
                    description = "Identificador único del registro. No obligatorio.") Long id,
            @ToolParam(required = false,
                    description = """
                            Estado del registro de reservación.
                            Sus valores pueden ser RESERVED, CANCELLED, COMPLETED o BLOCKED. No obligatorio.""") RoomBookingStatus status,
            @ToolParam(required = false,
                    description = "Fecha de inicio de la reservación. No obligatorio.") LocalDate startAt,
            @ToolParam(required = false,
                    description = "Fecha de finalización de la reservación. No obligatorio.") LocalDate endAt
    ){
        Specification<RoomBookingPeriod> specification = Specification.unrestricted();

        specification = specification.and(RoomBookingPeriodSpecifications.hasId(id));
        specification = specification.and(RoomBookingPeriodSpecifications.hasStatus(status));
        specification = specification.and(RoomBookingPeriodSpecifications.hasStartAt(startAt));
        specification = specification.and(RoomBookingPeriodSpecifications.hasEndAt(endAt));

        specification = specification.and(RoomBookingPeriodSpecifications.fetchEverythingForDTO());

        return convertEntityToDTO(repository.findAll(specification));
    }

    /**
     * Updates the details (dates or room) of a RoomBookingPeriod.
     *
     * @param roomBookingPeriodId ID of the period to update.
     * @param dto                 {@link RoomBookingPeriodDTO} with updated details.
     * @return Updated {@link RoomBookingPeriodDTO}.
     * @throws RuntimeException if validation fails.
     */
    @McpTool(
            name = "update_room_booking_period_info",
            description = "Actualiza fechas o habitación asociada de un RoomBookingPeriod."
    )
    @Transactional
    public RoomBookingPeriodDTO updateInfo(
            @ToolParam(required = true, description = """
            ID del RoomBookingPeriod que se desea actualizar.
            """)
            Long roomBookingPeriodId,
            @ToolParam(required = true, description = """
            Objeto DTO con los nuevos datos de la reserva (fechas o habitación).
            """)
            RoomBookingPeriodDTO dto
    ){
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
    @McpTool(
            name = "update_room_booking_period_status",
            description = "Actualiza el estado de un RoomBookingPeriod."
    )
    @Transactional
    public RoomBookingPeriodDTO updateStatus(
            @ToolParam(required = true, description = """
            ID del RoomBookingPeriod a modificar.
            """)
            Long roomBookingPeriodId,
            @ToolParam(required = true, description = """
            Nuevo estado que se desea asignar al registro (por ejemplo: RESERVED, CANCELED, COMPLETED).
            """)
            RoomBookingStatus status
    ){
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
    @McpTool(
            name = "delete_room_booking_period",
            description = "Elimina un RoomBookingPeriod si su estado es CANCELED o COMPLETED."
    )
    @Transactional
    public void delete (
            @ToolParam(required = true, description = """
            ID del RoomBookingPeriod a eliminar.
            """)
            Long id
    ){
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