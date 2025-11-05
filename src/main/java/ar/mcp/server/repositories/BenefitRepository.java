package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * Repository interface for {@link Benefit} entity.
 * Provides database access methods for Benefit-related queries.
 */
@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    List<Benefit> findByNameContaining(String name);
    List<Benefit> findByDescriptionContaining(String description);
    List<Benefit> findByOpenAtGreaterThan(LocalTime opening);
    List<Benefit> findByCloseAtLessThan(LocalTime ending);
    List<Benefit> findByOpenAtGreaterThanEqualAndCloseAtLessThanEqual(LocalTime opening, LocalTime ending);
    @Query("SELECT b FROM Benefit b JOIN b.hotel h WHERE h.id = :id")
    List<Benefit> findByHotel(Long id);

}