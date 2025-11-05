package ar.mcp.server.services;

import ar.mcp.server.domain.dto.AddressDTO;
import ar.mcp.server.domain.dto.HotelDTO;
import ar.mcp.server.domain.entities.Attraction;
import ar.mcp.server.domain.entities.Benefit;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.repositories.HotelRepository;
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
                    .ubication(stateName + ", " + countryCode)
                    .totalRooms(hotel.getTotalRooms())
                    .freeRooms(hotel.getTotalRooms())
                    .reservedRooms(0)
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
    @Transactional
    public Hotel createHotel (HotelDTO hotelDTO, AddressDTO addressDTO){
        validateInfo(hotelDTO.getName(), hotelDTO.getTotalRooms(), hotelDTO.getContactPhone());

        // Resolve room references from IDs
        List<Room> rooms = new ArrayList<>();
        hotelDTO.getRoomsId().forEach(room -> {
            Room roomInDB = roomService.getRoomById(room);

            rooms.add(roomInDB);
        });

        Address createdAddress = addressService.createAddress(addressDTO);

        // Create hotel entity with initial values
        Hotel hotel = Hotel.builder()
                .name(hotelDTO.getName())
                .stars(0)
                .address(createdAddress)
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

        return hotel;
    }

   /**
     * Retrieves a {@link Hotel} by its ID and converts it into a {@link HotelDTO}.
     *
     * @param id Hotel ID. Must be greater than 0.
     * @return HotelDTO representing the hotel with the given ID.
     * @throws RuntimeException if the ID is invalid or the hotel is not found.
     */
    public HotelDTO getHotelByIdDTO(Long id){
        if (id <= 0){
            throw new RuntimeException("Id cannot be null");
        }

        Hotel hotelInDb = hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        List<Long> roomIdList = new ArrayList<>();
        List<Long> attractionIdList = new ArrayList<>();
        List<Long> benefitsIdList = new ArrayList<>();

        hotelInDb.getRooms().forEach(room -> roomIdList.add(room.getId()));
        hotelInDb.getAttractions().forEach(attraction -> attractionIdList.add(attraction.getId()));
        hotelInDb.getBenefits().forEach(benefit -> benefitsIdList.add(benefit.getId()));

        String countryCode = hotelInDb.getAddress().getState().getCountryCode();
        String stateName = hotelInDb.getAddress().getState().getSubdivision();
        String streetName = hotelInDb.getAddress().getStreet();
        String streetNumber = hotelInDb.getAddress().getNumber();

        return HotelDTO.builder()
                .name(hotelInDb.getName())
                .stars(hotelInDb.getStars())
                .address(streetName + " " + streetNumber)
                .ubication(stateName + ", " + countryCode)
                .freeRooms(hotelInDb.getFreeRooms())
                .roomsId(roomIdList)
                .attractionsId(attractionIdList)
                .benefitsId(benefitsIdList)
                .build();
    }

    /**
     * Retrieves a {@link Hotel} entity by its ID.
     *
     * @param id Hotel ID. Must be greater than 0.
     * @return Hotel entity with the given ID.
     * @throws RuntimeException if the ID is invalid or the hotel is not found.
     */
    public Hotel getHotelByIdObject(Long id){
        if (id <= 0){
            throw new RuntimeException("Id cannot be null");
        }

        return hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the Database"));
    }

    /**
     * Retrieves a list of hotels that match the given star rating.
     *
     * @param stars Star rating to filter hotels. Must be greater than 0.
     * @return List of {@link HotelDTO} objects with the specified star rating.
     * @throws RuntimeException if the stars parameter is less than or equal to zero.
     */
    public List<HotelDTO> getHotelByStars(double stars){
        if (stars <= 0){
            throw new RuntimeException("Stars rating cannot be less than zero.");
        }

        return convertFromHotelListToHotelDTOList(hotelRepository.findByStars(stars));
    }

    /**
     * Retrieves a list of hotels that contain the given name.
     *
     * @param name Name to filter hotels. Must not be blank.
     * @return List of {@link HotelDTO} objects whose names contain the specified value.
     * @throws RuntimeException if the name is blank.
     */
    public List<HotelDTO> getHotelByName(String name){
        if (name.isBlank()){
            throw new RuntimeException("Name cannot be null.");
        }

        return convertFromHotelListToHotelDTOList(hotelRepository.findByNameContaining(name));
    }


    /**
     * Retrieves a list of hotels that offer a benefit with the given name.
     *
     * @param benefitsName Name of the benefit. Must not be blank.
     * @return List of {@link HotelDTO} objects that provide the specified benefit.
     * @throws RuntimeException if the benefitsName is blank.
     */
    public List<HotelDTO> getHotelByBenefits(String benefitsName){
        if (benefitsName.isBlank()){
            throw new RuntimeException("Name cannot be null.");
        }

        return convertFromHotelListToHotelDTOList(hotelRepository.findByBenefits(benefitsName));
    }


    /**
     * Retrieves a list of hotels that include an attraction with the given name.
     *
     * @param attractionName Name of the attraction. Must not be blank.
     * @return List of {@link HotelDTO} objects that include the specified attraction.
     * @throws RuntimeException if the attractionName is blank.
     */
    public List<HotelDTO> getHotelByAttraction(String attractionName){
        if (attractionName.isBlank()){
            throw new RuntimeException("Name cannot be null.");
        }

        return convertFromHotelListToHotelDTOList(hotelRepository.findByAttractions(attractionName));
    }


    /**
     * Updates basic information of an existing hotel without modifying its associated
     * benefits, rooms, or attractions.
     *
     * @param id  ID of the hotel to update.
     * @param dto {@link HotelDTO} containing the new hotel information.
     * @return Updated {@link Hotel} entity.
     * @throws RuntimeException if required fields are invalid after update.
     */
    @Transactional
    public Hotel updateHotelWithoutModifyBenefitsRoomsOrAttractions(Long id, HotelDTO dto){
        Hotel hotelInDb = this.getHotelByIdObject(id);

        if (!dto.getName().isBlank()){
            hotelInDb.setName(dto.getName());
        }
        if (dto.getStars() > 0){
            hotelInDb.setStars(dto.getStars());
        }
        if (dto.getTotalRooms() > 0){
            hotelInDb.setTotalRooms(dto.getTotalRooms());
        }

        validateInfo(hotelInDb.getName(), hotelInDb.getTotalRooms(), hotelInDb.getContactPhone());

        hotelRepository.save(hotelInDb);

        return hotelInDb;
    }


/**
 * Deletes a hotel entity if it exists and has no active reservations.
 *
 * @param id ID of the hotel to delete. Must not be null or less than 1.
 * @throws RuntimeException if the ID is invalid or the hotel has active reservations.
 */
    @Transactional
    public void deleteHotel(Long id){
        if (id == null || id < 1){
            throw new RuntimeException("Id cannot be null");
        }

        Hotel hotelInDb = this.getHotelByIdObject(id);

        if (hotelInDb.getReservedRooms() > 0){
            throw new RuntimeException("Cannot delete a Hotel entity when have active clients.");
        }

        hotelRepository.delete(hotelInDb);
    }

}
