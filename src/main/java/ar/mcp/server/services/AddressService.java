package ar.mcp.server.services;

import ar.mcp.server.domain.dto.AddressDTO;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.Person;
import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.domain.entities.address.States;
import ar.mcp.server.repositories.AddressRepository;
import ar.mcp.server.repositories.HotelRepository;
import ar.mcp.server.repositories.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class responsible for managing {@link Address} entities.
 * Provides CRUD operations for addresses associated with {@link Hotel} and {@link Person} entities.
 * It ensures proper validation and converts entities to {@link AddressDTO} when needed.
 *
 * Relationships:
 * - {@link Address} can be linked to either a {@link Hotel} or a {@link Person}, but never both.
 * - {@link Address} references a {@link States} entity to specify the location.
 * - Interacts with {@link HotelRepository} and {@link PersonRepository} to resolve related entities.
 */
@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final StatesService statesService;
    private final HotelRepository hotelRepository;
    private final PersonRepository personRepository;

    public AddressService(AddressRepository addressRepository, StatesService statesService, HotelRepository hotelRepository, PersonRepository personRepository) {
        this.addressRepository = addressRepository;
        this.statesService = statesService;
        this.hotelRepository = hotelRepository;
        this.personRepository = personRepository;
    }

    /**
     * Validates address information.
     *
     * @param street      Street name. Must not be blank.
     * @param number      Street number. Must not be blank.
     * @param stateCode   State code. Must not be blank.
     * @param personId    ID of a person associated with the address. Nullable.
     * @param hotelId     ID of a hotel associated with the address. Nullable.
     * @throws RuntimeException if validation rules are violated.
     */
    private void validate(String street, String number, String stateCode, Long personId, Long hotelId){
        if (hotelId != null && personId != null){
            throw new RuntimeException("An address registry only cant point to Person or Hotel entity, never both.");
        }
        if (hotelId == null && personId == null){
            throw new RuntimeException("An Address must point to Person or Hotel entity at least.");
        }
        if (stateCode.isBlank()){
            throw new RuntimeException("An Address must point to a state.");
        }
        if (street.isBlank()){
            throw new RuntimeException("Please, insert the Street data.");
        }
        if (number.isBlank()){
            throw new RuntimeException("Please, insert the house number data.");
        }
    }

    /**
     * Converts a list of {@link Address} entities to {@link AddressDTO} objects.
     *
     * @param list List of {@link Address} entities.
     * @return List of {@link AddressDTO} objects.
     */
    private List<AddressDTO> parseFromAddressEntityToAddressDTO(List<Address> list){
        return list.stream().map(address ->
             AddressDTO.builder()
                     .street(address.getStreet())
                     .number(address.getNumber())
                     .floor(address.getFloor())
                     .departmentNumber(address.getDepartmentNumber())
                     .stateId(address.getState().getCode())
                     .subdivisionName(address.getState().getSubdivision())
                     .hotelId(address.getHotel().getId())
                     .personId(address.getPerson().getId()).build()
        ).toList();
    }

    /**
     * Creates a new {@link Address} entity and links it to a {@link Hotel} or {@link Person}.
     *
     * @param dto {@link AddressDTO} containing address data.
     * @return The created {@link Address} entity.
     * @throws RuntimeException if both hotelId and personId are provided.
     * @throws RuntimeException   if required data is missing or invalid.
     */
    @Transactional
    public Address createAddress(AddressDTO dto){
        validate(dto.getStreet(), dto.getStreet(), dto.getStateId(), dto.getPersonId(), dto.getHotelId());

        Address address = Address.builder().build();

        if (dto.getHotelId() != null && dto.getPersonId() != null){
            throw new RuntimeException("An address only can point to Hotel or Person entity.");
        }

        if (dto.getHotelId() != null){
            Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(()-> new RuntimeException("Register cannot be found in the DataBase."));
            address.setHotel(hotel);
        }
        if (dto.getPersonId() != null) {
            Person person = personRepository.findById(dto.getPersonId()).orElseThrow(()-> new RuntimeException("Cannot found a Person register with that ID."));
            address.setPerson(person);
        }

        States state = statesService.getStateByCodeObject(dto.getStateId());

        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setFloor(dto.getFloor());
        address.setDepartmentNumber(dto.getDepartmentNumber());
        address.setState(state);

        addressRepository.save(address);

        return address;
    }

    /**
     * Retrieves an {@link Address} entity by its ID.
     *
     * @param id ID of the address. Must be greater than 0.
     * @return {@link Address} entity.
     * @throws RuntimeException        if ID is invalid.
     * @throws RuntimeException if no entity is found with the provided ID.
     */
    public Address getAddressByIdEntity(Long id){
        if (id == null || id < 1){
            throw new RuntimeException("Insert a valid ID value.");
        }

        return addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Cannot found this register in the DataBase."));
    }

    /**
     * Retrieves an {@link AddressDTO} by the address ID.
     *
     * @param id ID of the address. Must be greater than 0.
     * @return {@link AddressDTO} with all associated information.
     * @throws RuntimeException        if ID is invalid.
     * @throws RuntimeException if no entity is found with the provided ID.
     */
    public AddressDTO getAddressByIdResponse(Long id){
        if (id == null || id < 1){
            throw new RuntimeException("Insert a valid ID value.");
        }

        Address result = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Cannot found this register in the DataBase."));

        return AddressDTO.builder()
                .street(result.getStreet())
                .number(result.getNumber())
                .floor(result.getFloor())
                .departmentNumber(result.getDepartmentNumber())
                .stateId(result.getState().getCode())
                .subdivisionName(result.getState().getSubdivision())
                .hotelId(result.getHotel().getId())
                .personId(result.getPerson().getId()).build();
    }

    /**
     * Retrieves a list of addresses for a house.
     *
     * @param street   Street name.
     * @param number   Street number.
     * @param cityCode State code.
     * @return List of {@link AddressDTO} objects matching the criteria.
     * @throws RuntimeException if any of the required parameters are blank.
     */
    public List<AddressDTO> getAddressByLocationInHouse(String street, String number,String cityCode){
        if (street.isBlank() || number.isBlank() || cityCode.isBlank()){
            throw new RuntimeException("Please, insert all required data to identify a house on the DataBase.");
        }

        return parseFromAddressEntityToAddressDTO(addressRepository.findByStreetAndNumberAndCityCode(street, number, cityCode));
    }

    /**
     * Updates an existing {@link Address} entity.
     *
     * @param id  ID of the address to update.
     * @param dto {@link AddressDTO} containing new address data.
     * @return Updated {@link AddressDTO}.
     * @throws RuntimeException if ID is invalid or required fields are missing.
     */
    @Transactional
    public AddressDTO updateAddress(Long id, AddressDTO dto){
        if (id == null || id < 1){
            throw new RuntimeException("Insert a valid ID value.");
        }

        Address addressInDb = this.getAddressByIdEntity(id);

        if (!dto.getStreet().isBlank()){
            addressInDb.setStreet(dto.getStreet());
        }
        if (!dto.getNumber().isBlank()){
            addressInDb.setNumber(dto.getNumber());
        }
        if (!dto.getFloor().isBlank()){
            addressInDb.setFloor(dto.getFloor());
        }
        if (!dto.getDepartmentNumber().isBlank()){
            addressInDb.setDepartmentNumber(dto.getDepartmentNumber());
        }

        validate(addressInDb.getStreet(), addressInDb.getStreet(), addressInDb.getState().getCode(), addressInDb.getPerson().getId(), addressInDb.getHotel().getId());

        addressRepository.save(addressInDb);

        return AddressDTO.builder()
                .street(addressInDb.getStreet())
                .number(addressInDb.getNumber())
                .floor(addressInDb.getFloor())
                .departmentNumber(addressInDb.getDepartmentNumber())
                .stateId(addressInDb.getState().getCode())
                .subdivisionName(addressInDb.getState().getSubdivision())
                .hotelId(addressInDb.getHotel().getId())
                .personId(addressInDb.getPerson().getId()).build();
    }

    /**
     * Deletes an {@link Address} entity by its ID.
     *
     * @param id ID of the address to delete. Must be greater than 0.
     * @throws RuntimeException if ID is invalid.
     */
    @Transactional
    public void deleteAddress(Long id){
        if (id == null || id < 1){
            throw new RuntimeException("Insert a valid ID value.");
        }

        addressRepository.delete(getAddressByIdEntity(id));
    }

}
