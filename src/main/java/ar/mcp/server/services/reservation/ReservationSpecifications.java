package ar.mcp.server.services.reservation;

import ar.mcp.server.domain.entities.Reservation;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservationSpecifications {

    public static Specification<Reservation> hasId(Long id){
        /**
         * Filters reservations by id.
         *
         * @param id reservation id to match; ignored when null or < 1
         * @return Specification for equality on `id` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (id == null || id < 1) return null;

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }
    public static Specification<Reservation> hasNumberOfPeople(Integer numberOfPeople){
        return (root, query, criteriaBuilder) -> {
            if (numberOfPeople == null || numberOfPeople < 1) return null;

            return criteriaBuilder.equal(root.get("numberOfPeople"), numberOfPeople);
        };
    }
    public static Specification<Reservation> hasNumberOfNights(Integer numberOfNights){
        return (root, query, criteriaBuilder) -> {
            if (numberOfNights == null || numberOfNights < 1) return null;

            return criteriaBuilder.equal(root.get("numberOfNights"), numberOfNights);
        };
    }
    public static Specification<Reservation> hasStartAt(LocalDate startAt){
        return (root, query, criteriaBuilder) -> {
            if (startAt == null) return null;

            return criteriaBuilder.equal(root.get("startAt"), startAt);
        };
    }
    public static Specification<Reservation> hasEndAt(LocalDate endAt){
        return (root, query, criteriaBuilder) -> {
            if (endAt == null) return null;

            return criteriaBuilder.equal(root.get("startAt"), endAt);
        };
    }
    public static Specification<Reservation> hasTotalPrice(BigDecimal totalPrice){
        return (root, query, criteriaBuilder) -> {
            if (totalPrice == null || totalPrice.doubleValue() < 1) return null;

            return criteriaBuilder.equal(root.get("totalPrice"), totalPrice);
        };
    }

    public static Specification<Reservation> fetchEverythingForDTO() {
        /**
         * Fetches associations necessary for reservation DTOs (client and roomBooked).
         *
         * @return Specification that performs LEFT fetch joins and marks the query distinct
         */
        return (root, query, criteriaBuilder) -> {
            if (Reservation.class.equals(query.getResultType())) {
                root.fetch("client", JoinType.LEFT);
                root.fetch("roomBooked", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

}
