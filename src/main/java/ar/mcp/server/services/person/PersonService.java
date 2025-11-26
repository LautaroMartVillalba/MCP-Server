package ar.mcp.server.services.person;

import ar.mcp.server.domain.dto.AddressDTO;
import ar.mcp.server.domain.dto.PersonDTO;
import ar.mcp.server.domain.entities.Person;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.domain.entities.address.PersonAddress;
import ar.mcp.server.repositories.PersonRepository;
import ar.mcp.server.services.address.AddressService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing {@link Person} entities.
 * Provides methods to create, retrieve, update, and delete persons,
 * as well as to query them by attributes such as email, DNI, name, cellphone, or number of reservations.
 */
@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final AddressService addressService;

    public PersonService(PersonRepository personRepository, AddressService addressService) {
        this.personRepository = personRepository;
        this.addressService = addressService;
    }

    /**
     * Validates a {@link Person} data.
     *
     * @param name      Name of the person.
     * @param dni       DNI (National ID) of the person.
     * @param email     Email address.
     * @param age       Age of the person. Must be 18 or older.
     * @param cellPhone Cellphone number.
     * @throws RuntimeException if any mandatory field is missing or age is under 18.
     */
    private void validatePerson(String name, String dni, String email, int age, String cellPhone){
        if (age < 18){
            throw new RuntimeException("Only an adult can reservate a room.");
        }
        if (name.isBlank() || dni.isBlank() || email.isBlank() || cellPhone.isBlank()){
            throw new RuntimeException("Name, DNI, Email and Cell Phone Number are mandatory parameters.");
        }
    }

    /**
     * Converts a list of {@link Person} entities into a list of {@link PersonDTO}.
     *
     * @param list List of {@link Person} entities.
     * @return List of {@link PersonDTO}.
     */
    private List<PersonDTO> convertFromPersonListToPersonDTOList(List<Person> list){
        return list.stream().map(person -> {
            List<Long> reservationIds = person.getReservations().stream()
                    .map(Reservation::getId)
                    .toList();

            String countryCode = person.getAddress().getState().getCountryCode();
            String stateName = person.getAddress().getState().getSubdivision();
            String streetName = person.getAddress().getStreet();
            String streetNumber = person.getAddress().getNumber();

            return PersonDTO.builder()
                    .name(person.getName())
                    .age(person.getAge())
                    .cellPhone(person.getCellPhone())
                    .address(streetName + " " + streetNumber)
                    .ubication(stateName + ", " + countryCode)
                    .reservationIds(reservationIds)
                    .numberOfReservations(person.getNumberOfReservations())
                    .build();
            }
        ).toList();
    }

    /**
     * Creates a new {@link Person} entity with its associated address.
     *
     * @param personDTO  {@link PersonDTO} containing person data.
     * @param addressDTO {@link AddressDTO} containing the person's address data.
     * @return The created {@link Person} entity.
     * @throws RuntimeException if mandatory fields are missing or invalid.
     */
    @McpTool(
            name = "create_person",
            description = "Crea una nueva persona junto con su dirección asociada y la guarda en la base de datos."
    )
    @Transactional
    public Person createPerson (
            @ToolParam(
                    required = true,
                    description = "Objeto PersonDTO con los datos de la persona."
            ) PersonDTO personDTO,
            @ToolParam(
                    required = true,
                    description = "Objeto AddressDTO con la dirección asociada a la persona."
            ) AddressDTO addressDTO
    ){
        validatePerson(personDTO.getName(), personDTO.getDni(), personDTO.getEmail(), personDTO.getAge(), personDTO.getCellPhone());

        Address address = addressService.createAddress(addressDTO);
        
        // Cast to PersonAddress since Person requires a PersonAddress entity
        PersonAddress personAddress = (PersonAddress) address;

        Person person = Person.builder()
                .name(personDTO.getName())
                .dni(personDTO.getDni())
                .age(personDTO.getAge())
                .email(personDTO.getEmail())
                .address(personAddress)
                .numberOfReservations(0)
                .cellPhone(personDTO.getCellPhone()).build();

        personRepository.save(person);

        return person;
    }

    /**
     * Retrieves a {@link Person} entity by its ID.
     *
     * @param id ID of the person.
     * @return Optional containing the {@link Person} entity.
     * @throws RuntimeException if ID is null.
     */
    public Optional<Person> getPersonByIdObject (Long id){
        if (id == null){
            throw new RuntimeException("Id parameter cannot be null.");
        }

        return personRepository.findById(id);
    }

    @McpTool(
            name = "find_person_by_spec",
            description = """
                Realiza una búsqueda dinámica de personas según los parámetros provistos.

                Todos los parámetros son opcionales. Si un parámetro no se incluye, se ignora en el filtro.
                Ejemplo de uso:
                {
                  "id": null,
                  "email": "gmail.com",
                  "dni": null,
                  "name": null,
                  "age": null,
                  "cellPhone": null,
                  "maxNumberOfReservations": null,
                  "minNumberOfReservations": null
                }

                En este ejemplo, solo se filtrarán las personas que tengan un email que contenga "gmail.com"."""
    )
    public List<PersonDTO> findPersonBySpec(
            @ToolParam(required = false,
                    description = "Identificador único del registro. No obligatorio.") Long id,
            @ToolParam(required = false,
                    description = "Mail de contacto de la persona. No obligatorio.") String email,
            @ToolParam(required = false,
                    description = "Número nacional de identificación. No obligatorio.") String dni,
            @ToolParam(required = false,
                    description = "Nombre completo o parcial de la persona. No obligatorio.") String name,
            @ToolParam(required = false,
                    description = "Edad. No obligatorio.") Integer age,
            @ToolParam(required = false,
                    description = "Número de celular. No obligatorio.") String cellPhone,
            @ToolParam(required = false,
                    description = "Número de reservas hechas límite para el que se buscará una persona. No obligatorio.") Integer maxNumberOfReservations,
            @ToolParam(required = false,
                    description = "Número de reservas hechas mínimas para el que se buscará una persona. No obligatorio.") Integer minNumberOfReservations
    ){
        Specification<Person> specification = Specification.unrestricted();

        specification = specification.and(PersonSpecification.hasId(id));
        specification = specification.and(PersonSpecification.hasEmail(email));
        specification = specification.and(PersonSpecification.hasDni(dni));
        specification = specification.and(PersonSpecification.hasName(name));
        specification = specification.and(PersonSpecification.hasAge(age));
        specification = specification.and(PersonSpecification.hasCellPhone(cellPhone));
        specification = specification.and(PersonSpecification.hasNumberOfReservationsGreaterThan(minNumberOfReservations));
        specification = specification.and(PersonSpecification.hasNumberOfReservationsLessThan(maxNumberOfReservations));

                specification = specification.and(PersonSpecification.fetchEverythingForDTO());

                return convertFromPersonListToPersonDTOList(personRepository.findAll(specification));
    }

    /**
     * Updates an existing {@link Person} entity with the provided data.
     *
     * @param personId ID of the person to update.
     * @param dto      {@link PersonDTO} containing updated values.
     * @return Updated {@link Person} entity.
     * @throws RuntimeException if the person cannot be found.
     * @throws RuntimeException        if updated data is invalid.
     */
    @McpTool(
            name = "update_person_info",
            description = "Actualiza la información de una persona existente con los datos proporcionados."
    )
    @Transactional
    public Person updatePersonInfo(
            @ToolParam(
                    required = true,
                    description = "ID de la persona a actualizar."
            ) Long personId,
            @ToolParam(
                    required = true,
                    description = "Objeto PersonDTO con los datos actualizados."
            ) PersonDTO dto
    ){
        Person personInDB = this.getPersonByIdObject(personId).orElseThrow(() -> new RuntimeException("Register not found in the DataBase"));

        if (!dto.getEmail().isBlank()){
            personInDB.setEmail(dto.getEmail());
        }
        if (!dto.getName().isBlank()){
            personInDB.setName(dto.getName());
        }
        if (!dto.getDni().isBlank()){
            personInDB.setDni(dto.getDni());
        }
        if (!dto.getCellPhone().isBlank()){
            personInDB.setCellPhone(dto.getCellPhone());
        }
        if (dto.getNumberOfReservations() > 0){
            personInDB.setNumberOfReservations(dto.getNumberOfReservations());
        }
        if (dto.getAge() >= 18){
            personInDB.setAge(dto.getAge());
        }

        validatePerson(dto.getName(), dto.getDni(), dto.getEmail(), dto.getAge(), dto.getCellPhone());

        personRepository.save(personInDB);
        return personInDB;
    }

    /**
     * Deletes a {@link Person} entity by ID.
     * Deletion is not allowed if the person has an active reservation.
     *
     * @param id ID of the person to delete.
     * @throws RuntimeException        if ID is null or the person has an active reservation.
     * @throws RuntimeException if the person cannot be found.
     */
    @McpTool(
            name = "delete_person_by_id",
            description = "Elimina una persona por su ID, siempre que no tenga reservaciones activas."
    )
    @Transactional
    public void deletePersonByID(
            @ToolParam(
                    required = true,
                    description = "ID de la persona a eliminar."
            ) Long id
    ){
        if (id == null){
            throw new RuntimeException("Id cannot be null");
        }

        Person personInDB = this.getPersonByIdObject(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase"));
        
        // Validar que la persona no tenga reservaciones activas
        long activeReservations = personInDB.getReservations().size();
        if (activeReservations > 0){
            throw new RuntimeException("Cannot delete a person with " + activeReservations + " active reservation(s). Please cancel them first.");
        }

        personRepository.delete(personInDB);
    }

}
