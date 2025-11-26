package ar.mcp.server.services.address;

import ar.mcp.server.domain.dto.AddressDTO;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.Person;
import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.domain.entities.address.States;
import ar.mcp.server.repositories.AddressRepository;
import ar.mcp.server.repositories.HotelRepository;
import ar.mcp.server.repositories.PersonRepository;
import ar.mcp.server.services.states.StatesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class responsible for managing {@link Address} entities.
 * Provides CRUD operations for addresses associated with {@link Hotel} and {@link Person} entities.
 * It ensures proper validation and converts entities to {@link AddressDTO} when needed.
 * <p>
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
    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    public AddressService(AddressRepository addressRepository, StatesService statesService, HotelRepository hotelRepository, PersonRepository personRepository) {
        this.addressRepository = addressRepository;
        this.statesService = statesService;
        this.hotelRepository = hotelRepository;
        this.personRepository = personRepository;
    }

    /**
     * Validates address information.
     *
     * @param street    Street name. Must not be blank.
     * @param number    Street number. Must not be blank.
     * @param stateCode State code. Must not be blank.
     * @param personId  ID of a person associated with the address. Nullable.
     * @param hotelId   ID of a hotel associated with the address. Nullable.
     * @throws RuntimeException if validation rules are violated.
     */
    private void validate(String street, String number, String stateCode) {
        if (stateCode.isBlank()) {
            throw new RuntimeException("An Address must point to a state.");
        }
        if (street.isBlank()) {
            throw new RuntimeException("Please, insert the Street data.");
        }
        if (number.isBlank()) {
            throw new RuntimeException("Please, insert the house number data.");
        }
    }

    /**
     * Converts a list of {@link Address} entities to {@link AddressDTO} objects.
     *
     * @param list List of {@link Address} entities.
     * @return List of {@link AddressDTO} objects.
     */
    private List<AddressDTO> parseFromAddressEntityToAddressDTO(List<Address> list) {
        return list.stream().map(address ->
                AddressDTO.builder()
                        .street(address.getStreet())
                        .number(address.getNumber())
                        .floor(address.getFloor())
                        .departmentNumber(address.getDepartmentNumber())
                        .stateId(address.getState().getCode())
                        .subdivisionName(address.getState().getSubdivision()).build()
        ).toList();
    }

    /**
     * Creates a new {@link Address} entity and links it to a {@link Hotel} or {@link Person}.
     *
     * @param dto {@link AddressDTO} containing address data.
     * @return The created {@link Address} entity.
     * @throws RuntimeException if both hotelId and personId are provided.
     * @throws RuntimeException if required data is missing or invalid.
     */
    @McpTool(
            name = "create_address",
            description = "Crea una nueva dirección y la asocia a un hotel o persona."
    )
    @Transactional
    public Address createAddress(
            @ToolParam(required = true, description = """
                    Data transfer object que debe tener el siguiente formato (en JSON):
                    {
                        "street": "string",
                        "number": "string",
                        "floor": "string",
                        "departmentNumber": "string",
                        "stateId": "string",
                        "personId": long | null,
                        "hotelId": long | null
                    }
                    """) AddressDTO dto
    ) {
        log.debug("Iniciando creación de dirección en {}", dto.getStreet());
        validate(dto.getStreet(), dto.getNumber(), dto.getStateId());

        Address address = Address.builder().build();

        if (dto.getHotelId() != null && dto.getPersonId() != null) {
            throw new RuntimeException("An address only can point to Hotel or Person entity.");
        }

        States state = statesService.getStateByCodeObject(dto.getStateId());

        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setFloor(dto.getFloor());
        address.setDepartmentNumber(dto.getDepartmentNumber());
        address.setState(state);

        addressRepository.save(address);
        log.debug("Dirección creada exitosamente con ID: {}", address.getId());

        return address;
    }

    /**
     * Retrieves an {@link Address} entity by its ID.
     *
     * @param id ID of the address. Must be greater than 0.
     * @return {@link Address} entity.
     * @throws RuntimeException if ID is invalid.
     * @throws RuntimeException if no entity is found with the provided ID.
     */
    public Address getAddressByIdEntity(Long id) {
        if (id == null || id < 1) {
            throw new RuntimeException("Insert a valid ID value.");
        }

        return addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Cannot found this register in the DataBase."));
    }

    @McpTool(
            name = "find_address_by_spec",
            description = """
                Realiza una búsqueda dinámica de direcciones según los parámetros provistos.
                Permite filtrar direcciones por tipo (Hotel o Person) y opcionalmente cargar 
                la entidad relacionada para obtener información completa.
                
                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                
                Parámetro 'addressType' controla el tipo de dirección:
                - "HOTEL": Filtra solo direcciones de hoteles (HotelAddress)
                - "PERSON": Filtra solo direcciones de personas (PersonAddress)
                - null: No filtra por tipo (retorna ambos tipos)
                
                Parámetro 'fetchRelatedEntity':
                - true: Carga la entidad relacionada (Hotel o Person) con la dirección para evitar consultas adicionales
                - false/null: Solo retorna datos básicos de la dirección
                
                Parámetros 'hotelId' y 'personId' permiten filtrar por ID específico de la entidad relacionada.
                NOTA: No se puede usar hotelId y personId simultáneamente.
                
                Ejemplo de uso para buscar direcciones de hoteles y cargar los datos del hotel:
                {
                  "street": "Groove",
                  "addressType": "HOTEL",
                  "fetchRelatedEntity": true
                }
                
                Ejemplo para buscar direcciones de una persona específica:
                {
                  "personId": 5,
                  "fetchRelatedEntity": true
                }"""
    )
    public List<AddressDTO> findAddressSpect(
            @ToolParam(required = false,
                    description = "Identificador único del registro. No obligatorio.") Long id,
            @ToolParam(required = false,
                    description = "Nombre de la calle del registro. No obligatorio.") String street,
            @ToolParam(required = false,
                    description = "Número del inmueble de la dirección, dependiente de la calle (street). No obligatorio.") String number,
            @ToolParam(required = false,
                    description = "Piso en el cual se encuentra la dirección en caso de ser un departamento. No obligatorio.") String floor,
            @ToolParam(required = false,
                    description = "Número de puerta/departamento en el cual se encuentra la dirección en caso de ser un departamento. No obligatorio.") String departmentNumber,
            @ToolParam(required = false,
                    description = """
                            Tipo de dirección a filtrar. Valores posibles:
                            - "HOTEL": Solo direcciones de hoteles (HotelAddress)
                            - "PERSON": Solo direcciones de personas (PersonAddress)
                            - null: Sin filtro por tipo (retorna ambos). No obligatorio.""") String addressType,
            @ToolParam(required = false,
                    description = """
                            Si es true, carga la entidad relacionada (Hotel o Person) junto con la dirección.
                            Útil para obtener información completa sin consultas adicionales.
                            Solo tiene efecto cuando addressType está especificado o cuando se usa hotelId/personId.
                            Default: false. No obligatorio.""") Boolean fetchRelatedEntity,
            @ToolParam(required = false,
                    description = """
                            ID del hotel asociado a la dirección. 
                            Filtra automáticamente por tipo HotelAddress.
                            No puede usarse junto con personId. No obligatorio.""") Long hotelId,
            @ToolParam(required = false,
                    description = """
                            ID de la persona asociada a la dirección.
                            Filtra automáticamente por tipo PersonAddress.
                            No puede usarse junto con hotelId. No obligatorio.""") Long personId
    ){
        log.debug("Buscando direcciones con parámetros: street={}, number={}, addressType={}, fetchRelatedEntity={}, hotelId={}, personId={}", 
                  street, number, addressType, fetchRelatedEntity, hotelId, personId);
        
        if (hotelId != null && hotelId > 0 && personId != null && personId > 0) {
            throw new RuntimeException("Cannot filter by both hotelId and personId. An address belongs to either a Hotel or a Person, not both.");
        }
        
        Specification<Address> specification = Specification.unrestricted();

        // basic filters
        specification = specification.and(AddressSpecifications.hasId(id));
        specification = specification.and(AddressSpecifications.hasStreet(street));
        specification = specification.and(AddressSpecifications.hasNumber(number));
        specification = specification.and(AddressSpecifications.hasFloor(floor));
        specification = specification.and(AddressSpecifications.hasDepartmentNumber(departmentNumber));
        
        specification = specification.and(AddressSpecifications.hasHotelId(hotelId));
        specification = specification.and(AddressSpecifications.hasPersonId(personId));
        
        if ("HOTEL".equalsIgnoreCase(addressType)) {
            specification = specification.and(AddressSpecifications.isHotelAddress());
        } else if ("PERSON".equalsIgnoreCase(addressType)) {
            specification = specification.and(AddressSpecifications.isPersonAddress());
        }
        
        if (Boolean.TRUE.equals(fetchRelatedEntity)) {
            specification = specification.and(AddressSpecifications.fetchHotelRelationship());
            specification = specification.and(AddressSpecifications.fetchPersonRelationship());
        }
        
        // Always fetch state for DTO mapping
        specification = specification.and(AddressSpecifications.fetchEverythingForDTO());

        List<AddressDTO> result = parseFromAddressEntityToAddressDTO(addressRepository.findAll(specification));
        log.debug("Búsqueda completada. Direcciones encontradas: {}", result.size());
        return result;
    }

    /**
     * Updates an existing {@link Address} entity.
     *
     * @param id  ID of the address to update.
     * @param dto {@link AddressDTO} containing new address data.
     * @return Updated {@link AddressDTO}.
     * @throws RuntimeException if ID is invalid or required fields are missing.
     */
    @McpTool(
            name = "update_address",
            description = "Actualiza los datos de una dirección existente."
    )
    @Transactional
    public AddressDTO updateAddress(
            @ToolParam(required = true, description = """
                    ID de la dirección a actualizar. Debe ser mayor a 0.
                    """) Long id,
            @ToolParam(required = true, description = """
                    Objeto AddressDTO con los nuevos datos a actualizar. Los campos vacíos serán ignorados.
                    """) AddressDTO dto
    ) {
        log.debug("Actualizando dirección con ID: {}", id);
        if (id == null || id < 1) {
            throw new RuntimeException("Insert a valid ID value.");
        }

        Address addressInDb = this.getAddressByIdEntity(id);

        if (!dto.getStreet().isBlank()) {
            addressInDb.setStreet(dto.getStreet());
        }
        if (!dto.getNumber().isBlank()) {
            addressInDb.setNumber(dto.getNumber());
        }
        if (!dto.getFloor().isBlank()) {
            addressInDb.setFloor(dto.getFloor());
        }
        if (!dto.getDepartmentNumber().isBlank()) {
            addressInDb.setDepartmentNumber(dto.getDepartmentNumber());
        }

        validate(addressInDb.getStreet(), addressInDb.getStreet(), addressInDb.getState().getCode());

        addressRepository.save(addressInDb);
        log.debug("Dirección actualizada exitosamente: {}", addressInDb.getStreet());

        return AddressDTO.builder()
                .street(addressInDb.getStreet())
                .number(addressInDb.getNumber())
                .floor(addressInDb.getFloor())
                .departmentNumber(addressInDb.getDepartmentNumber())
                .stateId(addressInDb.getState().getCode())
                .subdivisionName(addressInDb.getState().getSubdivision()).build();
    }

    /**
     * Deletes an {@link Address} entity by its ID.
     *
     * @param id ID of the address to delete. Must be greater than 0.
     * @throws RuntimeException if ID is invalid.
     */
    @McpTool(
            name = "delete_address",
            description = "Elimina una dirección de la base de datos según su ID."
    )
    @Transactional
    public void deleteAddress(
            @ToolParam(required = true, description = """
                    ID de la dirección a eliminar. Debe ser mayor a 0.
                    """) Long id
    ) {
        log.debug("Eliminando dirección con ID: {}", id);
        if (id == null || id < 1) {
            throw new RuntimeException("Insert a valid ID value.");
        }

        addressRepository.delete(getAddressByIdEntity(id));
        log.debug("Dirección eliminada exitosamente");
    }

}
