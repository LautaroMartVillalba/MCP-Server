package ar.mcp.server.services.address;

import ar.mcp.server.domain.entities.address.Address;
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

}
