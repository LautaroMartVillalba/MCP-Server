package ar.mcp.server.services.reservation;

import ar.mcp.server.domain.entities.Person;
import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.Room;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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

    /**
     * Filters reservations where the start date falls within a specified date range (inclusive).
     *
     * @param startDate minimum start date (inclusive). Null will be ignored.
     * @param endDate maximum start date (inclusive). Null will be ignored.
     * @return Specification for date range or null when both inputs are null.
     */
    public static Specification<Reservation> hasStartAtBetween(LocalDate startDate, LocalDate endDate){
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) return null;

            Predicate predicate = criteriaBuilder.conjunction();
            
            if (startDate != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startAt"), startDate));
            }
            
            if (endDate != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.lessThanOrEqualTo(root.get("startAt"), endDate));
            }
            
            return predicate;
        };
    }

    /**
     * Filters reservations by the hotel ID of the booked room.
     *
     * @param hotelId the hotel ID to filter by. Null or < 1 will be ignored.
     * @return Specification for hotel ID or null when input is invalid.
     */
    public static Specification<Reservation> hasHotelId(Long hotelId){
        return (root, query, criteriaBuilder) -> {
            if (hotelId == null || hotelId < 1) return null;

            Join<Reservation, Room> roomJoin = root.join("roomBooked", JoinType.INNER);
            
            return criteriaBuilder.equal(roomJoin.get("hotel").get("id"), hotelId);
        };
    }

    /**
     * Filters reservations by the person/client ID who made the reservation.
     *
     * @param personId the person ID to filter by. Null or < 1 will be ignored.
     * @return Specification for person ID or null when input is invalid.
     */
    public static Specification<Reservation> hasPersonId(Long personId){
        return (root, query, criteriaBuilder) -> {
            if (personId == null || personId < 1) return null;

            return criteriaBuilder.equal(root.get("person").get("id"), personId);
        };
    }

    public static Specification<Reservation> fetchEverythingForDTO() {
        /**
         * Fetches associations necessary for reservation DTOs (person and roomBooked).
         *
         * @return Specification that performs LEFT fetch joins and marks the query distinct
         */
        return (root, query, criteriaBuilder) -> {
            if (Reservation.class.equals(query.getResultType())) {
                root.fetch("person", JoinType.LEFT);
                root.fetch("roomBooked", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

}
