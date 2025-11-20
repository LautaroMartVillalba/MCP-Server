package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * Repository interface for {@link Benefit} entity.
 * Provides database access methods for Benefit-related queries.
 */
@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long>, JpaSpecificationExecutor<Benefit> {
}