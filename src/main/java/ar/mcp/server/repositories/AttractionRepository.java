package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface AttractionRepository extends JpaRepository<Attraction, Long>, JpaSpecificationExecutor<Attraction> {

}
