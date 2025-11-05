package ar.mcp.server.services;


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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Service class responsible for managing {@link Room} entities.
 * It provides methods for creating, updating, retrieving, and validating rooms.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    private static final int MIN_BEDS = 1;
    private static final int MAX_BEDS = 4;
    private static final int MIN_PEOPLE = 1;
    private static final int MAX_PEOPLE = 4;

    /**
     * Validates a {@link RoomDTO} object before creation.
     *
     * @param room Room data to validate.
     * @throws RuntimeException if any validation rule fails.
     */
    private void checkIfRoomIsValid(RoomDTO room){
        if (room.getNumberOfBeds() <= MIN_BEDS || room.getNumberOfBeds() > MAX_BEDS){
            throw new RuntimeException("Rooms only can have 1 to 4 beds.");
        }
        if (room.getPeopleCapacity() <= MIN_PEOPLE || room.getPeopleCapacity() > MAX_PEOPLE){
            throw new RuntimeException("Rooms only accept 1 to 4 people.");
        }
        if (room.getRoomType() == null){
            throw new RuntimeException("Room category cannot be null.");
        }
        //If you see a warning here, it is because of your IDE.
        if (room.getBedType().equals(BedsType.KING_BED) && room.getNumberOfBeds() != 1){
            throw new RuntimeException("Only one king bed per room.");
        }
        //If you see a warning here, it is because of your IDE.
        if (room.getBedType().equals(BedsType.QUEEN_BED) && room.getNumberOfBeds() != 1){
            throw new RuntimeException("Only one queen bed per room.");
        }
        if (room.getBedType().equals(BedsType.DOUBLE_BED) && room.getNumberOfBeds() > 2){
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
    @Transactional
    public Room createRoom(RoomDTO room){
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

        return newRoom;
    }

    /**
     * Retrieves a {@link RoomDTO} for a given room ID.
     *
     * @param id Room ID to search.
     * @return RoomDTO corresponding to the given ID.
     * @throws RuntimeException if no room is found.
     */
    public RoomDTO getRoomByIdResponse(Long id){
        Room result = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        return RoomDTO.builder()
                .id(result.getId())
                .floor(result.getFloor())
                .peopleCapacity(result.getPeopleCapacity())
                .numberOfBeds(result.getNumberOfBeds())
                .pricePerNight(result.getPricePerNight())
                .bedType(result.getBedType())
                .state(result.getState()).build();
    }

    /**
     * Retrieves a {@link Room} entity by ID.
     *
     * @param id Room ID.
     * @return Room entity corresponding to the given ID.
     * @throws RuntimeException if no room is found.
     */
    public Room getRoomById(Long id){
        return roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));
    }

    /**
     * Retrieves rooms by number of beds.
     *
     * @param number Number of beds.
     * @return List of RoomDTO matching the number of beds.
     * @throws RuntimeException if number is invalid.
     */
    public List<RoomDTO> getRoomsByBedsNumber(int number){
        if (number < 1 || number > 4){
            throw new RuntimeException("No room will have less than 1 bed or more than 4 beds.");
        }

        List<Room> result = roomRepository.findByNumberOfBeds(number);

        if (result.isEmpty()){
            return Collections.emptyList();
        }

        return parseFromRoomListToRoomDTOList(result);
    }

    /**
     * Retrieves rooms by bed type.
     *
     * @param bedsType {@link BedsType} to search.
     * @return List of RoomDTO matching the bed type.
     */
    public List<RoomDTO> getRoomsByBedsTypes(BedsType bedsType){
        List<Room> result = roomRepository.findByBedType(bedsType);

        return parseFromRoomListToRoomDTOList(result);
    }

    /**
     * Retrieves rooms by people capacity.
     *
     * @param people Number of people the room accommodates.
     * @return List of RoomDTO matching the people capacity.
     * @throws RuntimeException if capacity is outside allowed range.
     */
    public List<RoomDTO> getRoomsByPeopleCapacity(int people){
        if (people < 1 || people > 4){
            throw new RuntimeException("A room only can accommodate between 1 and 4 people over 13 years old.");
        }
        List<Room> result = roomRepository.findByPeopleCapacity(people);

        if (result.isEmpty()){
            return Collections.emptyList();
        }

        return parseFromRoomListToRoomDTOList(result);
    }

    /**
     * Retrieves rooms by room type.
     *
     * @param roomType {@link RoomType} to search.
     * @return List of RoomDTO matching the room type.
     * @throws RuntimeException if roomType is null.
     */
    public List<RoomDTO> getRoomsByRoomType(RoomType roomType){
        if (roomType == null){
            throw new RuntimeException("You must search a valid type of room.");
        }

        List<Room> result = roomRepository.findByRoomType(roomType);

        if (result.isEmpty()){
            return Collections.emptyList();
        }

        return parseFromRoomListToRoomDTOList(result);
    }

    /**
     * Retrieves rooms by room state.
     *
     * @param state {@link RoomState} to filter by.
     * @return List of RoomDTO matching the state.
     * @throws RuntimeException if state is null.
     */
    public List<RoomDTO> getRoomsByState(RoomState state){
        if (state == null){
            throw new RuntimeException("You must search a valid room state.");
        }

        List<Room> result = roomRepository.findByState(state);

        if (result.isEmpty()){
            return Collections.emptyList();
        }

        return parseFromRoomListToRoomDTOList(result);
    }

    /**
     * Retrieves rooms belonging to a specific hotel.
     *
     * @param hotelId Hotel ID.
     * @return List of RoomDTO for the hotel.
     * @throws RuntimeException if hotelId is null.
     */
    List<RoomDTO> getByHotelId(Long hotelId){
        if(hotelId == null){
            throw new RuntimeException("Id cannot be null");
        }

        List<Room> result = roomRepository.findByHotel(hotelId);

        if (result.isEmpty()){
            return Collections.emptyList();
        }

        return parseFromRoomListToRoomDTOList(result);
    }

    /**
     * Retrieves rooms available between the given dates.
     *
     * @param startAt Start date of reservation period.
     * @param endAt End date of reservation period.
     * @return List of free RoomDTO objects.
     * @throws RuntimeException if dates are null or invalid.
     */
    public List<RoomDTO> getFreeRoomsByScheduleBetween(LocalDate startAt, LocalDate endAt){
        if (startAt == null || endAt == null || startAt.isBefore(LocalDate.now()) || endAt.isBefore(startAt)){
            throw new RuntimeException("Insert correct date, please.");
        }

        return parseFromRoomListToRoomDTOList(roomRepository.findByAvailableRoom(startAt, endAt));
    }

    /**
     * Retrieves rooms associated with a specific reservation.
     *
     * @param reservationId Reservation ID.
     * @return List of RoomDTO related to the reservation.
     * @throws RuntimeException if reservationId is null.
     */
    List<RoomDTO> getByReservationId(Long reservationId){
        if(reservationId == null){
            throw new RuntimeException("Id cannot be null");
        }

        List<Room> result = roomRepository.findByReservation(reservationId);

        if (result.isEmpty()){
            return Collections.emptyList();
        }

        return parseFromRoomListToRoomDTOList(result);
    }

    /**
     * Updates room information by its ID.
     *
     * @param id Room ID to update.
     * @param dto RoomDTO with updated data.
     * @return Updated Room entity.
     * @throws IllegalArgumentException if id or dto are invalid.
     */
    @Transactional
    public Room updateRoomInfoById(Long id, RoomDTO dto){
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Room ID must be a positive number.");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Updated room data must not be null.");
        }

        Room room = this.getRoomById(id);

        if (dto.getBedType() != null){
            if ((dto.getBedType().equals(BedsType.QUEEN_BED)
                    || dto.getBedType().equals(BedsType.KING_BED))
                    && dto.getNumberOfBeds() == 1){
                room.setBedType(dto.getBedType());
            }
            if ((dto.getBedType().equals(BedsType.DOUBLE_BED)
                    || dto.getBedType().equals(BedsType.SINGLE_BED)
                    || dto.getBedType().equals(BedsType.TWIN_BED))
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
    @Transactional
    public void changeRoomState(Long roomId, RoomState state){
        if (roomId == null || state == null){
            throw new RuntimeException("Insert all data to update room state");
        }

        Room roomInDb = this.getRoomById(roomId);
        roomInDb.setState(state);

        roomRepository.save(roomInDb);
    }

    /**
     * Deletes a room if it is in FREE state.
     *
     * @param roomId Room ID.
     * @throws RuntimeException if room is not free.
     */
    @Transactional
    public void deleteRoom(Long roomId){
        Room roomInDbB = this.getRoomById(roomId);

        if (roomInDbB.getState() == RoomState.FREE){
            roomRepository.deleteById(roomId);
        }
        else {
            throw new RuntimeException("Cannot delete a room if it not free.");
        }
    }
}
