package ar.mcp.server.services.person;

import ar.mcp.server.domain.entities.Person;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class PersonSpecification {

    /**
     * JPA `Specification` factories for {@link ar.mcp.server.domain.entities.Person}.
     *
     * <p>Provides helper predicates to filter by person attributes and a
     * fetch helper used when constructing Person DTOs.</p>
     */

    public static Specification<Person> hasId(Long id){
        /**
         * Filters persons by id.
         *
         * @param id person id to match; ignored when null or < 1
         * @return Specification for equality on `id` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (id == null || id < 1) return null;

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }
    public static Specification<Person> hasEmail(String email){
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isBlank()) return null;

            return criteriaBuilder.like(root.get("email"), email);
        };
    }
    public static Specification<Person> hasDni(String dni){
        return (root, query, criteriaBuilder) -> {
            if (dni == null || dni.isBlank()) return null;

            return criteriaBuilder.like(root.get("dni"), dni);
        };
    }
    public static Specification<Person> hasName(String name){
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) return null;

            return criteriaBuilder.like(root.get("name"), name);
        };
    }
    public static Specification<Person> hasAge(Integer age){
        return (root, query, criteriaBuilder) -> {
            if (age == null || age < 1) return null;

            return criteriaBuilder.equal(root.get("age"), age);
        };
    }
    public static Specification<Person> hasCellPhone(String cellPhone){
        return (root, query, criteriaBuilder) -> {
            if (cellPhone == null || cellPhone.isBlank()) return null;

            return criteriaBuilder.like(root.get("cellPhone"), cellPhone);
        };
    }
    public static Specification<Person> hasNumberOfReservationsGreaterThan(Integer reservation){
        return (root, query, criteriaBuilder) -> {
            if (reservation == null || reservation < 0) return null;

            return criteriaBuilder.greaterThan(root.get("reservation"), reservation);
        };
    }
    public static Specification<Person> hasNumberOfReservationsLessThan(Integer reservation){
        return (root, query, criteriaBuilder) -> {
            if (reservation == null || reservation < 0) return null;

            return criteriaBuilder.lessThan(root.get("reservation"), reservation);
        };
    }

    public static Specification<Person> fetchEverythingForDTO() {
        /**
         * Fetches associations (`reservation` and `address`) required for Person DTOs.
         *
         * @return Specification that performs LEFT fetch joins and marks the query distinct
         */
        return (root, query, criteriaBuilder) -> {
            if (Person.class.equals(query.getResultType())) {
                root.fetch("reservation", JoinType.LEFT);
                root.fetch("address", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

}
