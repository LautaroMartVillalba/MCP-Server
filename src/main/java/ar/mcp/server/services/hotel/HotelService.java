package ar.mcp.server.services.hotel;

import ar.mcp.server.domain.dto.AddressDTO;
import ar.mcp.server.domain.dto.HotelDTO;
import ar.mcp.server.domain.entities.Attraction;
import ar.mcp.server.domain.entities.Benefit;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.domain.entities.address.HotelAddress;
import ar.mcp.server.repositories.HotelRepository;
import ar.mcp.server.services.address.AddressService;
import ar.mcp.server.services.attraction.AttractionService;
import ar.mcp.server.services.benefit.BenefitService;
import ar.mcp.server.services.room.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for handling business logic related to {@link Hotel} entities.
 * <p>
 * This service provides operations for creating hotels, retrieving hotel data,
 * and converting hotel entities to DTOs for data transfer between layers.
 * It coordinates with {@link RoomService}, {@link BenefitService}, {@link AttractionService},
 * and {@link AddressService} to manage related entities.
 * </p>
 */
@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomService roomService;
    private final BenefitService benefitService;
    private final AttractionService attractionService;
    private final AddressService addressService;
    private static final Logger log = LoggerFactory.getLogger(HotelService.class);

    public HotelService(HotelRepository repository, RoomService roomService, BenefitService benefitService, AttractionService attractionService, AddressService addressService) {
        this.hotelRepository = repository;
        this.roomService = roomService;
        this.benefitService = benefitService;
        this.attractionService = attractionService;
        this.addressService = addressService;
    }

    /**
     * Validates hotel input information.
     *
     * @param name        Name of the hotel. Must not be blank.
     * @param totalRooms  Total number of rooms. Must be at least 1.
     * @param phoneNumber Contact phone number. Must not be null or blank.
     * @throws RuntimeException if any validation fails.
     */
    private void validateInfo(String name, int totalRooms, String phoneNumber){
        if(name.isBlank()){
            throw new RuntimeException("Hotel name cannot be null.");
        }
        if (totalRooms <1){
            throw new RuntimeException("A hotel must have at leas one room.");
        }
        if (phoneNumber == null || phoneNumber.isBlank()){
            throw new RuntimeException("Hotel's contact cell phone number cannot be null.");
        }
    }

    /**
     * Converts a list of {@link Hotel} entities into a list of {@link HotelDTO}.
     *
     * @param list List of Hotel entities.
     * @return List of HotelDTO objects representing the given hotels.
     */
    private List<HotelDTO> convertFromHotelListToHotelDTOList(List<Hotel> list){
        return list.stream().map(hotel -> {
            List<Long> roomsId = hotel.getRooms().stream().map(Room::getId).toList();
            List<Long> benefitsId = hotel.getBenefits().stream().map(Benefit::getId).toList();
            List<Long> attractionsId = hotel.getAttractions().stream().map(Attraction::getId).toList();

            String countryCode = hotel.getAddress().getState().getCountryCode();
            String stateName = hotel.getAddress().getState().getSubdivision();
            String streetName = hotel.getAddress().getStreet();
            String streetNumber = hotel.getAddress().getNumber();


            return HotelDTO.builder()
                    .name(hotel.getName())
                    .stars(hotel.getStars())
                    .address(streetName + " " + streetNumber)
                    .location(stateName + ", " + countryCode)
                    .totalRooms(hotel.getTotalRooms())
                    .freeRooms(hotel.getFreeRooms())
                    .reservedRooms(hotel.getReservedRooms())
                    .contactPhone(hotel.getContactPhone())
                    .roomsId(roomsId)
                    .benefitsId(benefitsId)
                    .attractionsId(attractionsId)
                    .build();
        }).toList();
    }

    /**
     * Creates a new {@link Hotel} entity along with its related {@link Address}, {@link Room},
     * {@link Benefit}, and {@link Attraction} entities if provided.
     *
     * @param hotelDTO   Data transfer object containing hotel information.
     * @param addressDTO Data transfer object containing address information.
     * @return The persisted Hotel entity.
     * @throws RuntimeException if required, hotel information is missing.
     */
    @McpTool(
            name = "create_hotel",
            description = "Crea un nuevo hotel junto con su dirección, habitaciones, beneficios y atracciones relacionadas."
    )
    @Transactional
    public Hotel createHotel (
            @ToolParam(
                    required = true,
                    description = "Datos del hotel a crear."
            ) HotelDTO hotelDTO,
            @ToolParam(
                    required = true,
                    description = "Datos de la dirección asociada al hotel."
            ) AddressDTO addressDTO
    ){
        log.debug("Iniciando creación de hotel: {}", hotelDTO.getName());
        validateInfo(hotelDTO.getName(), hotelDTO.getTotalRooms(), hotelDTO.getContactPhone());

        // Resolve room references from IDs
        List<Room> rooms = new ArrayList<>();
        hotelDTO.getRoomsId().forEach(room -> {
            Room roomInDB = roomService.getRoomById(room);

            rooms.add(roomInDB);
        });

        Address createdAddress = addressService.createAddress(addressDTO);
        
        // Cast to HotelAddress since Hotel requires a HotelAddress entity
        HotelAddress hotelAddress = (HotelAddress) createdAddress;

        // Create hotel entity with initial values
        Hotel hotel = Hotel.builder()
                .name(hotelDTO.getName())
                .stars(0)
                .address(hotelAddress)
                .totalRooms(hotelDTO.getTotalRooms())
                .freeRooms(hotelDTO.getTotalRooms())
                .contactPhone(hotelDTO.getContactPhone())
                .rooms(rooms)
                .reservedRooms(0)
                .build();

        // Attach attractions to hotel if any are provided
        List<Attraction> attractionsList = new ArrayList<>();
        if (!hotelDTO.getAttractionsId().isEmpty()){
            hotelDTO.getAttractionsId().forEach(attraction -> {
                Attraction attractionInDb = attractionService.getAttractionByIdObject(attraction);

                attractionsList.add(attractionInDb);
            });
        }
        hotel.setAttractions(attractionsList);

        // Attach benefits to hotel if any are provided
        List<Benefit> benefitList = new ArrayList<>();
        if (!hotelDTO.getBenefitsId().isEmpty()){
            hotelDTO.getBenefitsId().forEach(benefit -> {
                Benefit benefitInDb = benefitService.getBenefitByIdObject(benefit);

                benefitList.add(benefitInDb);
            });
        }
        hotel.setBenefits(benefitList);

        hotelRepository.save(hotel);
        log.debug("Hotel creado exitosamente: {}", hotel.getId());

        return hotel;
    }

    /**
    * Performs a dynamic search of {@link Hotel} entities based on multiple optional parameters.
    * Each non-null parameter in the provided {@link HotelDTO} will be included as a filtering criterion.
    * <p>
    * The search criteria may include hotel name, star rating, total rooms, free rooms, reserved rooms,
    * and contact phone number.
    * <p>
    * Example JSON format expected by the LLM:
    * <pre>
    * {
    *   "name": "Hotel Central",
    *   "stars": 4,
    *   "totalRooms": 300,
    *   "freeRooms": null,
    *   "reservedRooms": null,
    *   "contactPhone": null
    * }
    * </pre>
    *
    * @return List of {@link Hotel} entities that match the specified criteria.
    */
    @McpTool(name = "find_hotel_by_spec",
            description = """
                Realiza una búsqueda dinámica de hoteles según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "name": null,
                  "stars": 5,
                  "totalRooms": null,
                  "freeRooms": null,
                  "reservedRooms": null,
                  "contactPhone": null
                }

                En este ejemplo, solo se filtrarán los hoteles con 5 estrellas.
                """
    )
    public List<HotelDTO> findHotelBySpec(
            @ToolParam(required = false,
                    description = "Nombre del hotel. No obligatorio.") String name,
            @ToolParam(required = false,
                    description = "Puntuación del hotel. Puede ser decimal o entero. No obligatorio.") Double stars,
            @ToolParam(required = false,
                    description = "Total de habitaciones que tiene el hotel. No obligatorio.") Integer totalRooms,
            @ToolParam(required = false,
                    description = "Cantidad de habitaciones que no tienen reservaciones. No obligatorio.") Integer freeRooms,
            @ToolParam(required = false,
                    description = "Cantidad de habitaciones que se encuentran reservadas. No obligatorio.") Integer reservedRooms,
            @ToolParam(required = false,
                    description = "Número telefónico de contacto del hotel. No obligatorio.") String contactPhone

    ){
        log.debug("Buscando hoteles con parámetros: name={}, stars={}, totalRooms={}", name, stars, totalRooms);

        Specification<Hotel> specification = Specification.unrestricted();

        specification.and(HotelSpecifications.hasName(name));
        specification.and(HotelSpecifications.hasStars(stars));
        specification.and(HotelSpecifications.hasTotalRooms(totalRooms));
        specification.and(HotelSpecifications.hasFreeRooms(freeRooms));
        specification.and(HotelSpecifications.hasReservedRooms(reservedRooms));
        specification.and(HotelSpecifications.hasContactPhone(contactPhone));

        List<HotelDTO> result = convertFromHotelListToHotelDTOList(hotelRepository.findAll(specification));
        log.debug("Búsqueda completada. Hoteles encontrados: {}", result.size());
        return result;
    }

    /**
     * Retrieves a {@link Hotel} entity by its ID.
     *
     * @param id Hotel ID. Must be greater than 0.
     * @return Hotel entity with the given ID.
     * @throws RuntimeException if the ID is invalid or the hotel is not found.
     */
    @McpTool(
            name = "get_hotel_by_id_object",
            description = "Obtiene una entidad Hotel por su ID."
    )
    public Hotel getHotelByIdObject(
            @ToolParam(
                    required = true,
                    description = "ID del hotel a buscar."
            ) Long id
    ){
        log.debug("Recuperando hotel con ID: {}", id);
        if (id <= 0){
            throw new RuntimeException("Id cannot be null");
        }

        Hotel result = hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the Database"));
        log.debug("Hotel recuperado: {}", result.getName());
        return result;
    }

    /**
     * Updates basic information of an existing hotel (name and stars only).
     * Does not modify benefits, rooms, attractions, address, or contact phone.
     *
     * @param id  ID of the hotel to update.
     * @param dto {@link HotelDTO} containing the new hotel information.
     * @return Updated {@link Hotel} entity.
     * @throws RuntimeException if required fields are invalid after update.
     */
    @McpTool(
            name = "update_hotel_basic_info",
            description = "Actualiza solo el nombre y calificación de estrellas de un hotel. No se pueden modificar dirección, beneficios, habitaciones, atracciones o teléfono de contacto."
    )
    @Transactional
    public Hotel updateHotelBasicInfo(
            @ToolParam(
                    required = true,
                    description = "ID del hotel a actualizar."
            ) Long id,
            @ToolParam(
                    required = true,
                    description = "Datos actualizados del hotel (nombre y/o calificación de estrellas)."
            ) HotelDTO dto
    ){
        log.debug("Actualizando información básica del hotel con ID: {}", id);
        Hotel hotelInDb = this.getHotelByIdObject(id);

        if (dto.getName() != null && !dto.getName().isBlank()){
            hotelInDb.setName(dto.getName());
        }
        if (dto.getStars() > 0){
            hotelInDb.setStars(dto.getStars());
        }

        validateInfo(hotelInDb.getName(), hotelInDb.getTotalRooms(), hotelInDb.getContactPhone());

        hotelRepository.save(hotelInDb);
        log.debug("Información básica del hotel actualizada exitosamente: {}", hotelInDb.getName());

        return hotelInDb;
    }


    /**
     * Deletes a hotel entity if it exists and has no active reservations.
     *
     * @param id ID of the hotel to delete. Must not be null or less than 1.
     * @throws RuntimeException if the ID is invalid or the hotel has active reservations.
     */
    @McpTool(
            name = "delete_hotel",
            description = "Elimina un hotel si no tiene reservas activas. Valida que todas las habitaciones estén libres de reservaciones."
    )
    @Transactional
    public void deleteHotel(
            @ToolParam(
                    required = true,
                    description = "ID del hotel a eliminar."
            ) Long id
    ){
        log.debug("Eliminando hotel con ID: {}", id);
        if (id == null || id < 1){
            throw new RuntimeException("Id cannot be null");
        }

        Hotel hotelInDb = this.getHotelByIdObject(id);

        // Validar que no haya reservaciones activas en ninguna habitación
        long totalReservations = hotelInDb.getRooms().stream()
                .flatMap(room -> room.getReservation().stream())
                .count();
        
        if (totalReservations > 0){
            throw new RuntimeException("Cannot delete a Hotel with " + totalReservations + " active reservation(s). Total rooms: " + 
                    hotelInDb.getRooms().size() + ". Please cancel all reservations first.");
        }

        hotelRepository.delete(hotelInDb);
        log.debug("Hotel eliminado exitosamente: {}", hotelInDb.getName());
    }

}
