package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.address.States;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing geographical {@link States} entities.
 *
 * <p>Provides basic CRUD operations and supports Specifications for dynamic queries.
 * Also defines convenience query methods to lookup states by code, country code, and subdivision.</p>
 */
@Repository
public interface StateRepository extends JpaRepository<States, String>, JpaSpecificationExecutor<States> {
    Optional<States> findByCode(String code);
    List<States> findByCountryCode(String countryCode);
    List<States> findBySubdivisionContaining(String subdivision);
}
