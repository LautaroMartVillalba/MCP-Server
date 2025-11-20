package ar.mcp.server.services.room_booking_period;

import ar.mcp.server.domain.entities.RoomBookingPeriod;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
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

    public static Specification<RoomBookingPeriod> hasStartAt(LocalDate startAt) {
        return (root, query, criteriaBuilder) -> {
            if (startAt == null) return null;

            return criteriaBuilder.equal(root.get("startAt"), startAt);
        };
    }

    public static Specification<RoomBookingPeriod> hasEndAt(LocalDate endAt) {
        return (root, query, criteriaBuilder) -> {
            if (endAt == null) return null;

            return criteriaBuilder.equal(root.get("endAt"), endAt);
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
