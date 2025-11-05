package ar.mcp.server.repositories;


import ar.mcp.server.domain.entities.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

//    List<Address> findByStateAndStreetAndNumberAndFloorAndDepartmentNumber(String street, String number,String cityCode, String floor, String departmentNumber);
    @Query("""
            SELECT DISTINCT a FROM Address a WHERE
             a.state.code = :stateCode
            """)
    List<Address> findByStateCode(String stateCode);
    @Query("""
            SELECT DISTINCT a FROM Address a WHERE a.street LIKE :street AND a.number = :number AND a.state.code = :cityCode
            """)
    List<Address> findByStreetAndNumberAndCityCode(String street, String number, String cityCode);

}
