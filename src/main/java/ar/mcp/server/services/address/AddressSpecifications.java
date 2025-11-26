package ar.mcp.server.services.address;

import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.domain.entities.address.HotelAddress;
import ar.mcp.server.domain.entities.address.PersonAddress;
import ar.mcp.server.domain.entities.address.States;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA `Specification` factories for {@link ar.mcp.server.domain.entities.address.Address}.
 *
 * <p>Contains predicates to filter addresses by fields such as street, number,
 * floor and departmentNumber and a helper to fetch the associated state for DTOs.</p>
 */
public class AddressSpecifications {

    public static Specification<Address> hasId(Long id){
        /**
         * Filters addresses by ID.
         *
         * @param id address id to match; ignored when null or < 1
         * @return Specification for equality on `id` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (id == null || id < 1) return null;

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }
    public static Specification<Address> hasStreet(String street){
        return (root, query, criteriaBuilder) -> {
            if (street == null || street.isBlank()) return null;

            return criteriaBuilder.equal(root.get("street"), street);
        };
    }
    public static Specification<Address> hasNumber(String number){
        return (root, query, criteriaBuilder) -> {
            if (number == null || number.isBlank()) return null;

            return criteriaBuilder.equal(root.get("number"), number);
        };
    }
    public static Specification<Address> hasFloor(String floor){
        return (root, query, criteriaBuilder) -> {
            if (floor == null || floor.isBlank()) return null;

            return criteriaBuilder.equal(root.get("floor"), floor);
        };
    }
    public static Specification<Address> hasDepartmentNumber(String departmentNumber){
        return (root, query, criteriaBuilder) -> {
            if (departmentNumber == null || departmentNumber.isBlank()) return null;

            return criteriaBuilder.equal(root.get("departmentNumber"), departmentNumber);
        };
    }

    public static Specification<Address> fetchEverythingForDTO() {
        /**
         * Fetches the `state` association to avoid additional queries when building DTOs.
         *
         * @return Specification that performs a LEFT fetch join on `state` association
         */
        return (root, query, criteriaBuilder) -> {
            if (Address.class.equals(query.getResultType())) {
                root.fetch("state", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

    /**
     * Filters addresses to only those of type HotelAddress.
     *
     * @return Specification that matches only addresses with discriminator value "HOTEL"
     */
    public static Specification<Address> isHotelAddress(){
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.type(), HotelAddress.class);
    }

    /**
     * Filters addresses to only those of type PersonAddress.
     *
     * @return Specification that matches only addresses with discriminator value "PERSON"
     */
    public static Specification<Address> isPersonAddress(){
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.type(), PersonAddress.class);
    }

    /**
     * Fetches the associated Hotel entity for HotelAddress instances.
     * Should be combined with isHotelAddress() specification.
     *
     * @return Specification that performs a LEFT fetch join on the `hotel` association
     */
    public static Specification<Address> fetchHotelRelationship() {
        return (root, query, criteriaBuilder) -> {
            if (Address.class.equals(query.getResultType())) {
                root.fetch("hotel", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

    /**
     * Fetches the associated Person entity for PersonAddress instances.
     * Should be combined with isPersonAddress() specification.
     *
     * @return Specification that performs a LEFT fetch join on the `person` association
     */
    public static Specification<Address> fetchPersonRelationship() {
        return (root, query, criteriaBuilder) -> {
            if (Address.class.equals(query.getResultType())) {
                root.fetch("person", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

    /**
     * Filters HotelAddress instances by the ID of the associated hotel.
     *
     * @param hotelId hotel ID to match; ignored when null or < 1
     * @return Specification that filters by hotel.id or null when input invalid
     */
    public static Specification<Address> hasHotelId(Long hotelId){
        return (root, query, criteriaBuilder) -> {
            if (hotelId == null || hotelId < 1) return null;

            return criteriaBuilder.and(
                criteriaBuilder.equal(root.type(), HotelAddress.class),
                criteriaBuilder.equal(root.get("hotel").get("id"), hotelId)
            );
        };
    }

    /**
     * Filters PersonAddress instances by the ID of the associated person.
     *
     * @param personId person ID to match; ignored when null or < 1
     * @return Specification that filters by person.id or null when input invalid
     */
    public static Specification<Address> hasPersonId(Long personId){
        return (root, query, criteriaBuilder) -> {
            if (personId == null || personId < 1) return null;

            return criteriaBuilder.and(
                criteriaBuilder.equal(root.type(), PersonAddress.class),
                criteriaBuilder.equal(root.get("person").get("id"), personId)
            );
        };
    }

}
