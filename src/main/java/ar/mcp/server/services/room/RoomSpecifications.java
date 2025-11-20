package ar.mcp.server.services.room;

import ar.mcp.server.domain.entities.Room;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import ar.mcp.server.domain.enums.BedsType;
import ar.mcp.server.domain.enums.RoomState;
import ar.mcp.server.domain.enums.RoomType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class RoomSpecifications {

    public static Specification<Room> hasId(Long id) {
        /**
         * Filters by primary identifier.
         *
         * @param id entity id to match; ignored when null or < 1
         * @return Specification for equality on `id` or null when input is invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (id == null || id < 1) return null;

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    public static Specification<Room> hasFloor(Integer floor) {
        /**
         * Filters rooms by floor number.
         *
         * @param floor floor number to match; ignored when null or < 1
         * @return Specification for equality on `floor` or null when input is invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (floor == null || floor < 1) return null;

            return criteriaBuilder.equal(root.get("floor"), floor);
        };
    }

    public static Specification<Room> hasNumberOfBeds(Integer numberOfBeds) {
        return (root, query, criteriaBuilder) -> {
            if (numberOfBeds == null || numberOfBeds < 1) return null;

            return criteriaBuilder.equal(root.get("numberOfBeds"), numberOfBeds);
        };
    }

    public static Specification<Room> hasBedType(BedsType bedType) {
        /**
         * Filters rooms by `BedsType` enum.
         *
         * @param bedType beds type to match; ignored when null
         * @return Specification for equality on `bedType` or null when input is invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (bedType == null) return null;

            return criteriaBuilder.equal(root.get("bedType"), bedType);
        };
    }

    public static Specification<Room> hasPeopleCapacity(Integer peopleCapacity) {
        return (root, query, criteriaBuilder) -> {
            if (peopleCapacity == null || peopleCapacity < 1) return null;

            return criteriaBuilder.equal(root.get("peopleCapacity"), peopleCapacity);
        };
    }

    public static Specification<Room> hasRoomType(RoomType roomType) {
        return (root, query, criteriaBuilder) -> {
            if (roomType == null) return null;

            return criteriaBuilder.equal(root.get("roomType"), roomType);
        };
    }

    public static Specification<Room> hasState(RoomState state) {
        return (root, query, criteriaBuilder) -> {
            if (state == null) return null;

            return criteriaBuilder.equal(root.get("state"), state);
        };
    }

    public static Specification<Room> hasTimesBooked(Integer timesBooked) {
        return (root, query, criteriaBuilder) -> {
            if (timesBooked == null || timesBooked < 0) return null;

            return criteriaBuilder.equal(root.get("timesBooked"), timesBooked);
        };
    }

    public static Specification<Room> pricePerNightGreaterThan(BigDecimal price) {
        /**
         * Filters rooms with price per night greater than provided value.
         *
         * @param price threshold price; ignored when null or <= 0
         * @return Specification comparing `pricePerNight` greater than `price` or null when input invalid
         */
        return (root, query, criteriaBuilder) -> {
            if (price == null || price.doubleValue() <= 0) return null;

            return criteriaBuilder.greaterThan(root.get("pricePerNight"), price);
        };
    }

    public static Specification<Room> pricePerNightLessThan(BigDecimal price) {
        return (root, query, criteriaBuilder) -> {
            if (price == null || price.doubleValue() <= 0) return null;

            return criteriaBuilder.lessThan(root.get("pricePerNight"), price);
        };
    }

    public static Specification<Room> fetchEverythingForDTO() {
        /**
         * Fetches associations required to map `Room` to its DTO without N+1.
         *
         * @return Specification that performs LEFT fetch joins for hotel, reservation and roomBookingPeriod
         */
        return (root, query, criteriaBuilder) -> {
            if (Room.class.equals(query.getResultType())) {
                root.fetch("hotel", JoinType.LEFT);
                root.fetch("reservation", JoinType.LEFT);
                root.fetch("roomBookingPeriod", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

}
