package ar.mcp.server.services.states;

import ar.mcp.server.domain.entities.address.States;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA `Specification` factories for {@link ar.mcp.server.domain.entities.address.States}.
 *
 * <p>Provides predicates used to build dynamic queries for states and a helper
 * to fetch addresses when building DTOs.</p>
 */
public class StatesSpecifications {

    public static Specification<States> hasCode(String code) {
        /**
         * Filters states by their ISO code or configured code field.
         *
         * @param code state code to match; ignored when null or blank
         * @return Specification for equality on `code` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (code == null || code.isBlank()) return null;

            return criteriaBuilder.equal(root.get("code"), code);
        };
    }

    public static Specification<States> hasCountryCode(String countryCode) {
        return (root, query, criteriaBuilder) -> {
            if (countryCode == null || countryCode.isBlank()) return null;

            return criteriaBuilder.equal(root.get("countryCode"), countryCode);
        };
    }

    public static Specification<States> hasSubdivision(String subdivision) {
        return (root, query, criteriaBuilder) -> {
            if (subdivision == null || subdivision.isBlank()) return null;

            return criteriaBuilder.equal(root.get("subdivision"), subdivision);
        };
    }

    public static Specification<States> fetchEverythingForDTO() {
        /**
         * Performs fetch join for addresses when projecting States to DTOs.
         *
         * @return Specification that fetches `addresses` association with LEFT join
         */
        return (root, query, criteriaBuilder) -> {
            if (States.class.equals(query.getResultType())) {
                root.fetch("addresses", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }
}
