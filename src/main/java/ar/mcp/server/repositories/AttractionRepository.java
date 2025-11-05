package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * Repository interface for managing {@link Attraction} entities.
 * Provides CRUD operations and custom query methods to filter attractions
 * based on various attributes.
 */
@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    List<Attraction> findByNameContaining(String name);
    List<Attraction> findByDescriptionContaining(String description);
    List<Attraction> findByPeopleCapacityBetween(int min, int max);
    List<Attraction> findByOpenAtGreaterThan(LocalTime open);
    List<Attraction> findByCloseAtLessThan(LocalTime close);
    List<Attraction> findByOpenAtGreaterThanEqualAndCloseAtLessThanEqual(LocalTime open, LocalTime close);
    @Query("SELECT a FROM Attraction a JOIN a.hotel h WHERE h.id = :id")
    List<Attraction> findByHotel(Long id);

}
