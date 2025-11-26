package ar.mcp.server.services.room_booking_period;

import ar.mcp.server.domain.entities.Reservation;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.entities.RoomBookingPeriod;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import ar.mcp.server.domain.enums.RoomBookingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class RoomBookingPeriodSpecifications {

    public static Specification<RoomBookingPeriod> hasId(Long id) {
        /**
         * Filters room booking periods by id.
         *
         * @param id identifier to match; ignored when null or < 1
         * @return Specification for equality on `id` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (id == null || id < 1) return null;

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    /**
     * Filters room booking periods where the start date falls within a specified date range (inclusive).
     *
     * @param startDate minimum start date (inclusive). Null will be ignored.
     * @param endDate maximum start date (inclusive). Null will be ignored.
     * @return Specification for date range or null when both inputs are null.
     */
    public static Specification<RoomBookingPeriod> hasStartAtBetween(LocalDate startDate, LocalDate endDate) {
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
     * Filters room booking periods by the hotel ID of the associated room.
     *
     * @param hotelId the hotel ID to filter by. Null or < 1 will be ignored.
     * @return Specification for hotel ID or null when input is invalid.
     */
    public static Specification<RoomBookingPeriod> hasHotelId(Long hotelId) {
        return (root, query, criteriaBuilder) -> {
            if (hotelId == null || hotelId < 1) return null;

            Join<RoomBookingPeriod, Room> roomJoin = root.join("room", JoinType.INNER);
            
            return criteriaBuilder.equal(roomJoin.get("hotel").get("id"), hotelId);
        };
    }

    /**
     * Filters room booking periods by the person/client ID from the reservation.
     *
     * @param personId the person ID to filter by. Null or < 1 will be ignored.
     * @return Specification for person ID or null when input is invalid.
     */
    public static Specification<RoomBookingPeriod> hasPersonId(Long personId) {
        return (root, query, criteriaBuilder) -> {
            if (personId == null || personId < 1) return null;

            Join<RoomBookingPeriod, Reservation> reservationJoin = root.join("reservation", JoinType.INNER);
            
            return criteriaBuilder.equal(reservationJoin.get("person").get("id"), personId);
        };
    }

    public static Specification<RoomBookingPeriod> hasStatus(RoomBookingStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return null;

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
    
    public static Specification<RoomBookingPeriod> fetchEverythingForDTO() {
        /**
         * Fetches associations required to project RoomBookingPeriod to a DTO.
         *
         * @return Specification that performs LEFT fetch joins on `room` and `reservation`
         */
        return (root, query, criteriaBuilder) -> {
            if (RoomBookingPeriod.class.equals(query.getResultType())) {
                root.fetch("room", JoinType.LEFT);
                root.fetch("reservation", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }
}
