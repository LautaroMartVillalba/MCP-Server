package ar.mcp.server.services.attraction;

import ar.mcp.server.domain.entities.Attraction;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    /**
     * Creates a specification to filter attractions by keywords in their description.
     * Matches attractions where the description contains ANY of the provided keywords (OR logic).
     *
     * @param keywords List of keywords to search for in the description (case-insensitive).
     * @return A specification that filters by keywords, or null if the input is null/empty.
     */
    public static Specification<Attraction> hasDescriptionContainingKeywords(List<String> keywords){
        return (root, query, criteriaBuilder) -> {
            if (keywords == null || keywords.isEmpty()) return null;
            
            List<Predicate> predicates = keywords.stream()
                    .filter(keyword -> keyword != null && !keyword.isBlank())
                    .map(keyword -> criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            "%" + keyword.toLowerCase() + "%"
                    ))
                    .collect(Collectors.toList());
            
            if(predicates.isEmpty()) {
                return null;
            }
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
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
         * Fetches associations for DTO mapping.
         * Note: Attraction has ManyToMany with hotels (mapped by Hotel), not a single hotel reference.
         * No fetch needed here as hotels collection would cause circular reference issues.
         *
         * @return Specification that marks query as distinct
         */
        return (root, query, criteriaBuilder) -> {
            if (Attraction.class.equals(query.getResultType())) {
                // No fetch: 'hotels' is a collection managed by Hotel side
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

}
