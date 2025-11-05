package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Hotel entities.
 * Extends JpaRepository to provide basic CRUD operations and custom queries.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByNameContaining(String name);
    List<Hotel> findByStars(double stars);
    List<Hotel> findByStarsGreaterThan(Double stars);
    List<Hotel> findByStarsLessThan(Double stars);
    @Query("SELECT DISTINCT h FROM Hotel h JOIN h.benefits s WHERE s.name LIKE :benefitName")
    List<Hotel> findByBenefits(String benefitName);
    @Query("SELECT DISTINCT h FROM Hotel h JOIN h.attractions a WHERE a.name LIKE :attractionName")
    List<Hotel> findByAttractions(String attractionName);

}
