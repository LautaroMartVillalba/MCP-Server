package ar.mcp.server.services.benefit;

import ar.mcp.server.domain.entities.Benefit;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

public class BenefitSpecifications {

    public static Specification<Benefit> hasId(Long id){
        /**
         * Filters benefits by id.
         *
         * @param id benefit id to match; ignored when null or < 1
         * @return Specification for equality on `id` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (id == null || id < 1) return null;

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }
    public static Specification<Benefit> hasName(String name){
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) return null;

            return criteriaBuilder.like(root.get("name"), name);
        };
    }
    public static Specification<Benefit> hasDescription(String description){
        return (root, query, criteriaBuilder) -> {
            if (description == null || description.isBlank()) return null;

            return criteriaBuilder.like(root.get("description"), description);
        };
    }
    public static Specification<Benefit> hasOpenAtGraterThan(LocalTime openAt){
        return (root, query, criteriaBuilder) -> {
            if (openAt == null) return null;

            return criteriaBuilder.greaterThan(root.get("openAt"), openAt);
        };
    }
    public static Specification<Benefit> hasCloseAtLessThan(LocalTime closeAt){
        return (root, query, criteriaBuilder) -> {
            if (closeAt == null) return null;

            return criteriaBuilder.lessThan(root.get("closeAt"), closeAt);
        };
    }

    public static Specification<Benefit> fetchEverythingForDTO() {
        /**
         * Fetches associations for DTO mapping.
         * Note: Benefit has ManyToMany with hotels (mapped by Hotel), not a single hotel reference.
         * No fetch needed here as hotels collection would cause circular reference issues.
         *
         * @return Specification that marks query as distinct
         */
        return (root, query, criteriaBuilder) -> {
            if (Benefit.class.equals(query.getResultType())) {
                // No fetch: 'hotels' is a collection managed by Hotel side
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

}
