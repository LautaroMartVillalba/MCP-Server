package ar.mcp.server.services.attraction;

import ar.mcp.server.domain.entities.Attraction;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

public class AttractionSpecifications {

    public static Specification<Attraction> hasId(Long id){
        /**
         * Filters attractions by id.
         *
         * @param id attraction id to match; ignored when null or < 1
         * @return Specification for equality on `id` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (id == null || id < 1) return null;

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }
    public static Specification<Attraction> hasName(String name){
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank())return null;

            return criteriaBuilder.equal(root.get("name"), name);
        };
    }
    public static Specification<Attraction> hasDescription(String description){
        return (root, query, criteriaBuilder) -> {
            if (description == null || description.isBlank())return null;

            return criteriaBuilder.like(root.get("description"), description);
        };
    }
    public static Specification<Attraction> hasCapacityGreaterThan(Integer min){
        return (root, query, criteriaBuilder) -> {
            if (min == null || min < 1) return null;

            return criteriaBuilder.greaterThan(root.get("peopleCapacity"), min);
        };
    }
    public static Specification<Attraction> hasCapacityLessThan(Integer max){
        return (root, query, criteriaBuilder) -> {
            if (max == null || max < 1) return null;

            return criteriaBuilder.lessThan(root.get("peopleCapacity"), max);
        };
    }
    public static Specification<Attraction> hasOpenAt(LocalTime opening){
        return (root, query, criteriaBuilder) -> {
            if (opening == null) return null;

            return criteriaBuilder.greaterThan(root.get("openAt"), opening);
        };
    }
    public static Specification<Attraction> hasEndAt(LocalTime ending){
        return (root, query, criteriaBuilder) -> {
            if (ending == null) return null;

            return criteriaBuilder.lessThan(root.get("closeAt"), ending);
        };
    }

    public static Specification<Attraction> fetchEverythingForDTO() {
        /**
         * Fetches `hotel` association to avoid additional queries during DTO mapping.
         *
         * @return Specification that performs a LEFT fetch join on `hotel`
         */
        return (root, query, criteriaBuilder) -> {
            if (Attraction.class.equals(query.getResultType())) {
                root.fetch("hotel", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

}
