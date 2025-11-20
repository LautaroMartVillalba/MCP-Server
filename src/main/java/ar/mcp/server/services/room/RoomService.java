package ar.mcp.server.services.room;


import ar.mcp.server.domain.dto.RoomDTO;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.entities.RoomBookingPeriod;
import ar.mcp.server.domain.enums.BedsType;
import ar.mcp.server.domain.enums.RoomState;
import ar.mcp.server.domain.enums.RoomType;
import ar.mcp.server.repositories.HotelRepository;
import ar.mcp.server.repositories.ReservationRepository;
import ar.mcp.server.repositories.RoomRepository;
import ar.mcp.server.services.RoomPriceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Service class responsible for managing {@link Room} entities.
 * It provides methods for creating, updating, retrieving, and validating rooms.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    private static final int MINS = 1;
    private static final int MAXS = 4;
    private static final int MIN_PEOPLE = 1;
    private static final int MAX_PEOPLE = 4;

    /**
     * Validates a {@link RoomDTO} object before creation.
     *
     * @param room Room data to validate.
     * @throws RuntimeException if any validation rule fails.
     */
    private void checkIfRoomIsValid(RoomDTO room){
        if (room.getNumberOfBeds() <= MINS || room.getNumberOfBeds() > MAXS){
            throw new RuntimeException("Rooms only can have 1 to 4 beds.");
        }
        if (room.getPeopleCapacity() <= MIN_PEOPLE || room.getPeopleCapacity() > MAX_PEOPLE){
            throw new RuntimeException("Rooms only accept 1 to 4 people.");
        }
        if (room.getRoomType() == null){
            throw new RuntimeException("Room category cannot be null.");
        }
        //If you see a warning here, it is because of your IDE.
        if (room.getBedType().equals(BedsType.KING) && room.getNumberOfBeds() != 1){
            throw new RuntimeException("Only one king bed per room.");
        }
        //If you see a warning here, it is because of your IDE.
        if (room.getBedType().equals(BedsType.QUEEN) && room.getNumberOfBeds() != 1){
            throw new RuntimeException("Only one queen bed per room.");
        }
        if (room.getBedType().equals(BedsType.DOUBLE) && room.getNumberOfBeds() > 2){
            throw new RuntimeException("Only two double bed per room.");
        }
    }

    /**
     * Converts a list of {@link Room} entities into a list of {@link RoomDTO}.
     *
     * @param rooms List of Room entities.
     * @return List of RoomDTO objects with mapped properties.
     */
    public List<RoomDTO> parseFromRoomListToRoomDTOList(List<Room> rooms){
        return rooms.stream().map(room -> {
            List<Long> reservationIds = room.getReservation().stream().map(Reservation::getId).toList();
            List<Long> roomBookingPeriodsId = room.getRoomBookingPeriod().stream().map(RoomBookingPeriod::getId).toList();

            return RoomDTO.builder()
                    .id(room.getId())
                    .floor(room.getFloor())
                    .peopleCapacity(room.getPeopleCapacity())
                    .numberOfBeds(room.getNumberOfBeds())
                    .bedType(room.getBedType())
                    .hotelId(room.getHotel().getId())
                    .timesBooked(room.getTimesBooked())
                    .pricePerNight(room.getPricePerNight())
                    .reservationId(reservationIds)
                    .roomBookingPeriodId(roomBookingPeriodsId)
                    .state(room.getState()).build();
        }).toList();
    }

    /**
     * Creates a new {@link Room} entity in the database.
     *
     * @param room Room data transfer object.
     * @return The created Room entity.
     * @throws RuntimeException if room validation fails.
     */
    @McpTool(
            name = "create_room",
            description = "Crea una nueva habitación validando su información y asociándola a un hotel existente."
    )
    @Transactional
    public Room createRoom(
            @ToolParam(required = true, description = """
            Datos de la habitación a crear, incluyendo cantidad de camas, capacidad, tipo, estado y ID del hotel asociado.
            """)
            RoomDTO room
    ){
        log.debug("Iniciando creación de habitación para hotel ID: {}", room.getHotelId());
        checkIfRoomIsValid(room);
        Hotel hotel = hotelRepository.findById(room.getHotelId()).orElseThrow();

        Room newRoom = Room.builder()
                .peopleCapacity(room.getPeopleCapacity())
                .roomType(room.getRoomType())
                .bedType(room.getBedType())
                .numberOfBeds(room.getNumberOfBeds())
                .pricePerNight(RoomPriceGenerator.priceGenerator(room.getRoomType(),
                                                                 room.getBedType(),
                                                                 room.getFloor(),
                                                                 room.getPeopleCapacity()))
                .state(room.getState())
                .hotel(hotel)
                .timesBooked(0L).build();

        roomRepository.save(newRoom);
        log.debug("Habitación creada exitosamente con ID: {} en hotel: {}", newRoom.getId(), hotel.getName());

        return newRoom;
    }

    @McpTool(
            name = "find_room_by_spec",
            description = """
                Realiza una búsqueda dinámica de habitaciones según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "id": null,
                  "floor": 4,
                  "numberOfBeds": null,
                  "peopleCapacity": null,
                  "roomState": null,
                  "timesBooked": null,
                  "maxPrice": null,
                  "minPrice": null
                }

                En este ejemplo, solo se filtrarán las habitaciones que estén en el piso 4."""
    )
    public List<RoomDTO> findRoomBySpec(
            @ToolParam(required = false,
                    description = "Identificador único del registro. No obligatorio.") Long id,
            @ToolParam(required = false,
                    description = "Piso en el que se encuentra la habitación. No obligatorio.") Integer floor,
            @ToolParam(required = false,
                    description = """
                            Tipo de habitación. Sus valores pueden ser
                            STANDARD DELUXE, SUITE, EXECUTIVE o PRESIDENTIAL.
                            No obligatorio.""") RoomType roomType,
            @ToolParam(required = false,
                    description = """
                            Tipos de cama que tiene la habitación. Sus valores pueden ser
                            SINGLE, DOUBLE, QUEEN, KING o TWIN. No obligatorio.""") BedsType bedsType,
            @ToolParam(required = false,
                    description = "Cantidad de camas. No obligatorio.") Integer numberOfBeds,
            @ToolParam(required = false,
                    description = "Cantidad de personas que puede albergar. No obligatorio.") Integer peopleCapacity,
            @ToolParam(required = false,
                    description = """
                            Estado en el que se encuentra la habitación. sus valores
                            pueden ser OCCUPIED, UNOCCUPIED, BEING_CLEANED, CLEANED,
                            FREE o RESERVED. No obligatorio.""") RoomState roomState,
            @ToolParam(required = false,
                    description = "Cantidad de veces que la habitación ha sido reservada. No obligatorio.") Integer timesBooked,
            @ToolParam(required = false,
                    description = "Precio máximo por el cual se filtra. No obligatorio.") BigDecimal maxPrice,
            @ToolParam(required = false,
                    description = "Precio mínimo por el cual se filtra. No obligatorio.") BigDecimal minPrice
    ){
        log.debug("find_room_by_spec");
        Specification<Room> specification = Specification.unrestricted();

        specification.and(RoomSpecifications.hasId(id));
        specification.and(RoomSpecifications.hasFloor(floor));
        specification.and(RoomSpecifications.hasRoomType(roomType));
        specification.and(RoomSpecifications.hasBedType(bedsType));
        specification.and(RoomSpecifications.hasNumberOfBeds(numberOfBeds));
        specification.and(RoomSpecifications.hasPeopleCapacity(peopleCapacity));
        specification.and(RoomSpecifications.hasState(roomState));
        specification.and(RoomSpecifications.hasTimesBooked(timesBooked));
        specification.and(RoomSpecifications.pricePerNightLessThan(maxPrice));
        specification.and(RoomSpecifications.pricePerNightGreaterThan(minPrice));

        specification = specification.and(RoomSpecifications.fetchEverythingForDTO());

        return parseFromRoomListToRoomDTOList(roomRepository.findAll(specification));
    }

    /**
     * Retrieves a {@link Room} entity by ID.
     *
     * @param id Room ID.
     * @return Room entity corresponding to the given ID.
     * @throws RuntimeException if no room is found.
     */
    @McpTool(
            name = "get_room_by_id_object",
            description = "Obtiene una entidad Room completa por su ID."
    )
    public Room getRoomById(
            @ToolParam(required = true, description = """
            ID único de la habitación a obtener como entidad.
            """)
            Long id
    ){
        log.debug("get_room_by_id_object");
        return roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    /**
     * Retrieves rooms available between the given dates.
     *
     * @param startAt Start date of reservation period.
     * @param endAt End date of reservation period.
     * @return List of free RoomDTO objects.
     * @throws RuntimeException if dates are null or invalid.
     */
    @McpTool(
            name = "get_free_rooms_by_schedule_between",
            description = "Obtiene todas las habitaciones disponibles dentro de un rango de fechas dado."
    )
    public List<RoomDTO> getFreeRoomsByScheduleBetween(
            @ToolParam(required = true, description = """
            Fecha de inicio del rango de búsqueda.
            """)
            LocalDate startAt,
            @ToolParam(required = true, description = """
            Fecha de finalización del rango de búsqueda.
            """)
            LocalDate endAt
    ){
        log.debug("get_free_rooms_by_schedule_between");
        if (startAt == null || endAt == null || startAt.isBefore(LocalDate.now()) || endAt.isBefore(startAt)){
            throw new RuntimeException("Insert correct date, please.");
        }

        return parseFromRoomListToRoomDTOList(roomRepository.findByAvailableRoom(startAt, endAt));
    }

    /**
     * Updates room information by its ID.
     *
     * @param id Room ID to update.
     * @param dto RoomDTO with updated data.
     * @return Updated Room entity.
     * @throws IllegalArgumentException if id or dto are invalid.
     */
    @McpTool(
            name = "update_room_info_by_id",
            description = "Actualiza la información de una habitación existente por su ID."
    )
    @Transactional
    public Room updateRoomInfoById(
            @ToolParam(required = true, description = """
            ID único de la habitación a actualizar.
            """)
            Long id,
            @ToolParam(required = true, description = """
            Datos actualizados de la habitación (tipo, camas, capacidad, estado, etc.).
            """)
            RoomDTO dto
    ){
        log.debug("update_room_info_by_id");
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Room ID must be a positive number.");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Updated room data must not be null.");
        }

        Room room = this.getRoomById(id);

        if (dto.getBedType() != null){
            if ((dto.getBedType().equals(BedsType.QUEEN)
                    || dto.getBedType().equals(BedsType.KING))
                    && dto.getNumberOfBeds() == 1){
                room.setBedType(dto.getBedType());
            }
            if ((dto.getBedType().equals(BedsType.DOUBLE)
                    || dto.getBedType().equals(BedsType.SINGLE)
                    || dto.getBedType().equals(BedsType.TWIN))
                    && dto.getNumberOfBeds() > 1 || dto.getNumberOfBeds() < 5){
                room.setBedType(dto.getBedType());
                room.setNumberOfBeds(dto.getNumberOfBeds());
            }
        }
        if (dto.getPeopleCapacity() != 0) {
            room.setPeopleCapacity(dto.getPeopleCapacity());
        }
        if (dto.getRoomType() != null) {
            room.setRoomType(dto.getRoomType());
        }
        if (dto.getState() != null) {
            room.setState(dto.getState());
        }

        roomRepository.save(room);
        return room;
    }

    /**
     * Changes the state of a room.
     *
     * @param roomId Room ID.
     * @param state New {@link RoomState} for the room.
     * @throws RuntimeException if parameters are null.
     */
    @McpTool(
            name = "change_room_state",
            description = "Cambia el estado de una habitación por su ID."
    )
    @Transactional
    public void changeRoomState(
            @ToolParam(required = true, description = """
            ID de la habitación cuyo estado se modificará.
            """)
            Long roomId,
            @ToolParam(required = true, description = """
            Nuevo estado que se desea asignar a la habitación.
            """)
            RoomState state){
        log.debug("Cambiando estado de habitación ID: {} al nuevo estado: {}", roomId, state);
        if (roomId == null || state == null){
            throw new RuntimeException("Insert all data to update room state");
        }

        Room roomInDb = this.getRoomById(roomId);
        roomInDb.setState(state);

        roomRepository.save(roomInDb);
        log.debug("Estado de habitación actualizado exitosamente");
    }

    /**
     * Deletes a room if it is in FREE state.
     *
     * @param roomId Room ID.
     * @throws RuntimeException if room is not free.
     */
    @McpTool(
            name = "delete_room",
            description = "Elimina una habitación si se encuentra en estado FREE."
    )
    @Transactional
    public void deleteRoom(
            @ToolParam(required = true, description = """
            ID de la habitación que se desea eliminar.
            """)
            Long roomId
    ){
        log.debug("Eliminando habitación con ID: {}", roomId);
        Room roomInDbB = this.getRoomById(roomId);

        if (roomInDbB.getState() == RoomState.FREE){
            roomRepository.deleteById(roomId);
            log.debug("Habitación eliminada exitosamente");
        }
        else {
            throw new RuntimeException("Cannot delete a room if it not free.");
        }
    }
}
