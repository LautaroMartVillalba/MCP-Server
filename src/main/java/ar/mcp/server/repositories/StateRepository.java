package ar.mcp.server.repositories;

import ar.mcp.server.domain.entities.address.States;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<States, String> {
    Optional<States> findByCode(String code);
    List<States> findByCountryCode(String countryCode);
    List<States> findBySubdivisionContaining(String subdivision);
}
