package ar.mcp.server.repositories;


import ar.mcp.server.domain.entities.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for accessing and managing {@link Address} entities.
 *
 * <p>Extends {@link org.springframework.data.jpa.repository.JpaRepository} and
 * {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor} to provide
 * CRUD operations and support for dynamic queries using Specifications.</p>
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {

}
