package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing {@link Person} entities from the database.
 * <p>
 * Extends {@link JpaRepository} to provide standard CRUD operations,
 * as well as custom query methods for searching by various attributes.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByEmailContaining (String email);
    List<Person> findByDniContaining (String dni);
    List<Person> findByNameContaining (String name);
    List<Person> findByCellPhoneContaining (String cellPhoneNumber);
    List<Person> findByNumberOfReservations (int number);
    List<Person> findByNumberOfReservationsGreaterThan (int number);
    List<Person> findByNumberOfReservationsLessThan (int number);
    @Query("SELECT p FROM Person p JOIN p.reservation r WHERE r.id = :reservationId")
    Optional<Person> findByReservation(int reservationId);

}
