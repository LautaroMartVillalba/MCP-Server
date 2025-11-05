package ar.mcp.server.services;

import ar.mcp.server.domain.dto.AddressDTO;
import ar.mcp.server.domain.dto.PersonDTO;
import ar.mcp.server.domain.entities.Person;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.repositories.PersonRepository;
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
            Reservation reservation = person.getReservation();

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
                    .reservationId(reservation != null ? reservation.getId() : null)
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
    @Transactional
    public Person createPerson (PersonDTO personDTO, AddressDTO addressDTO){
        validatePerson(personDTO.getName(), personDTO.getDni(), personDTO.getEmail(), personDTO.getAge(), personDTO.getCellPhone());

        Address address = addressService.createAddress(addressDTO);

        Person person = Person.builder()
                .name(personDTO.getName())
                .dni(personDTO.getDni())
                .age(personDTO.getAge())
                .email(personDTO.getEmail())
                .address(address)
                .numberOfReservations(0)
                .cellPhone(personDTO.getCellPhone()).build();

        personRepository.save(person);

        return person;
    }

    /**
     * Retrieves a {@link PersonDTO} by its ID.
     *
     * @param id ID of the person.
     * @return {@link PersonDTO} representing the person.
     * @throws RuntimeException        if ID is null.
     * @throws RuntimeException if the person cannot be found.
     */
    public PersonDTO getPersonByIdDTO (Long id){
        if (id == null){
            throw new RuntimeException("Id parameter cannot be null.");
        }

        Person result = personRepository.findById(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase."));

        String countryCode = result.getAddress().getState().getCountryCode();
        String stateName = result.getAddress().getState().getSubdivision();
        String streetName = result.getAddress().getStreet();
        String streetNumber = result.getAddress().getNumber();

        return PersonDTO.builder()
                .name(result.getName())
                .age(result.getAge())
                .cellPhone(result.getCellPhone())
                .address(streetName + " " + streetNumber)
                .ubication(stateName + ", " + countryCode)
                .reservationId(result.getReservation().getId())
                .numberOfReservations(result.getNumberOfReservations()).build();
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

    /**
     * Finds persons whose email contains the given string.
     *
     * @param email Email filter.
     * @return List of {@link PersonDTO}.
     * @throws RuntimeException if email is blank.
     */
    public List<PersonDTO> getPersonByEmail (String email){
        if (email.isBlank()){
            throw new RuntimeException("Email parameter cannot be null.");
        }

        return convertFromPersonListToPersonDTOList(personRepository.findByEmailContaining(email));
    }

    /**
     * Finds persons whose DNI contains the given string.
     *
     * @param DNI DNI filter.
     * @return List of {@link PersonDTO}.
     * @throws RuntimeException if DNI is blank.
     */
    public List<PersonDTO> getPersonByDNI (String DNI){
        if (DNI.isBlank()){
            throw new RuntimeException("DNI parameter cannot be null.");
        }

        return convertFromPersonListToPersonDTOList(personRepository.findByDniContaining(DNI));
    }

    /**
     * Finds persons whose name contains the given string.
     *
     * @param name Name filter.
     * @return List of {@link PersonDTO}.
     * @throws RuntimeException if name is blank.
     */
    public List<PersonDTO> getPersonByName (String name){
        if (name.isBlank()){
            throw new RuntimeException("Name parameter cannot be null.");
        }

        return convertFromPersonListToPersonDTOList(personRepository.findByNameContaining(name));
    }

    /**
     * Finds persons whose cellphone contains the given string.
     *
     * @param cellPhoneNumber Cellphone filter.
     * @return List of {@link PersonDTO}.
     * @throws RuntimeException if cellphone is blank.
     */
    public List<PersonDTO> getPersonByCellphone (String cellPhoneNumber){
        if (cellPhoneNumber.isBlank()){
            throw new RuntimeException("Cellphone number parameter cannot be null.");
        }

        return convertFromPersonListToPersonDTOList(personRepository.findByCellPhoneContaining(cellPhoneNumber));
    }

    /**
     * Finds persons with exactly the given number of reservations.
     *
     * @param numberOfReservations Number of reservations.
     * @return List of {@link PersonDTO}.
     * @throws RuntimeException if numberOfReservations is negative.
     */
    public List<PersonDTO> getPersonByReservations (int numberOfReservations){
        if (numberOfReservations < 0){
            throw new RuntimeException("Number of reservations must be at least zero.");
        }

        return convertFromPersonListToPersonDTOList(personRepository.findByNumberOfReservations(numberOfReservations));
    }

    /**
     * Finds persons with more than the given number of reservations.
     *
     * @param numberOfReservations Number of reservations.
     * @return List of {@link PersonDTO}.
     * @throws RuntimeException if numberOfReservations is negative.
     */
    public List<PersonDTO> getPersonByReservationsGreaterThan (int numberOfReservations){
        if (numberOfReservations < 0){
            throw new RuntimeException("Number of reservations must be at least zero.");
        }

        return convertFromPersonListToPersonDTOList(personRepository.findByNumberOfReservationsGreaterThan(numberOfReservations));
    }

    /**
     * Finds persons with fewer than the given number of reservations.
     *
     * @param numberOfReservations Number of reservations.
     * @return List of {@link PersonDTO}.
     * @throws RuntimeException if numberOfReservations is negative.
     */
    public List<PersonDTO> getPersonByReservationsLessThan (int numberOfReservations){
        if (numberOfReservations < 0){
            throw new RuntimeException("Number of reservations must be at least zero.");
        }

        return convertFromPersonListToPersonDTOList(personRepository.findByNumberOfReservationsLessThan(numberOfReservations));
    }

    /**
     * Finds a person by the reservation ID.
     *
     * @param reservation Reservation ID.
     * @return {@link PersonDTO} representing the person.
     * @throws RuntimeException        if reservation ID is negative.
     * @throws RuntimeException if the person cannot be found.
     */
    public PersonDTO getPersonByReservationId (int reservation){
        if (reservation < 0){
            throw new RuntimeException("Reservation id cannot be null or less than zero.");
        }

        Person result = personRepository.findByReservation(reservation).orElseThrow(() -> new RuntimeException("Register not found in the DataBase"));

        return PersonDTO.builder()
                .name(result.getName())
                .age(result.getAge())
                .cellPhone(result.getCellPhone())
                .reservationId(result.getReservation().getId())
                .numberOfReservations(result.getNumberOfReservations()).build();
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
    @Transactional
    public Person updatePersonInfo(Long personId, PersonDTO dto){
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
    @Transactional
    public void deletePersonByID(Long id){
        if (id == null){
            throw new RuntimeException("Id cannot be null");
        }

        Person personInDB = this.getPersonByIdObject(id).orElseThrow(() -> new RuntimeException("Register not found in the DataBase"));
        Reservation reservationRelatedWithPerson = personInDB.getReservation();

        if (reservationRelatedWithPerson != null){
            throw new RuntimeException("Cannot delete a client when his reservation is active.");
        }

        personRepository.delete(personInDB);
    }

}
