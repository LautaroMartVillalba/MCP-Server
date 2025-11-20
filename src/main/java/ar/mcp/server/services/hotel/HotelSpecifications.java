package ar.mcp.server.services.hotel;

import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.address.Address;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA `Specification` factories for {@link ar.mcp.server.domain.entities.Hotel}.
 *
 * <p>
 * Each public static method returns a `Specification<Hotel>` that can be
 * combined with others to compose dynamic queries. Methods return `null`
 * (which Spring Data ignores) when their input parameter is null or invalid.
 * </p>
 */
public class HotelSpecifications {

    public static Specification<Hotel> fetchEverythingForDTO() {
        /**
         * Fetches related associations required to build DTO projections without N+1 queries.
         *
         * @return Specification that performs LEFT fetch joins for rooms, benefits, attractions and address->state
         */
        return (root, query, criteriaBuilder) -> {
            if (Hotel.class.equals(query.getResultType())) {

                root.fetch("rooms", JoinType.LEFT);
                root.fetch("benefits", JoinType.LEFT);
                root.fetch("attractions", JoinType.LEFT);

                Fetch<Hotel, Address> address = root.fetch("address", JoinType.LEFT);
                address.fetch("state", JoinType.LEFT);

                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }


    public static Specification<Hotel> hasName(String name){
        /**
         * Creates a specification that filters hotels by exact name.
         *
         * @param name hotel name to match (exact). If null or empty the specification is ignored.
         * @return Specification for equality on `name` or null when input is invalid.
         */
        return ((root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;

            return criteriaBuilder.equal(root.get("name"), name);
        });
    }
    public static Specification<Hotel> hasStars(Double stars){
        /**
         * Filters hotels by star rating.
         *
         * @param stars exact star rating to match. Null or values < 0.1 will be ignored.
         * @return Specification for equality on `stars` or null when input is invalid.
         */
        return ((root, query, criteriaBuilder) -> {
            if (stars == null || stars < 0.1) return null;

            return criteriaBuilder.equal(root.get("stars"), stars);
        });
    }
    public static Specification<Hotel> hasTotalRooms(Integer totalRooms){
        /**
         * Filters hotels by total number of rooms.
         *
         * @param totalRooms exact total rooms to match. Ignored when null or < 1.
         * @return Specification for equality on `totalRooms` or null when input is invalid.
         */
        return ((root, query, criteriaBuilder) -> {
            if (totalRooms == null || totalRooms < 1) return null;

            return criteriaBuilder.equal(root.get("totalRooms"), totalRooms);
        });
    }
    public static Specification<Hotel> hasFreeRooms(Integer freeRooms){
        /**
         * Filters hotels by currently free rooms count.
         *
         * @param freeRooms exact number of free rooms to match. Ignored when null or < 1.
         * @return Specification for equality on `freeRooms` or null when input is invalid.
         */
        return ((root, query, criteriaBuilder) -> {
            if (freeRooms == null || freeRooms < 1) return null;

            return criteriaBuilder.equal(root.get("freeRooms"), freeRooms);
        });
    }
    public static Specification<Hotel> hasReservedRooms(Integer reservedRooms){
        /**
         * Filters hotels by reserved rooms count.
         *
         * @param reservedRooms exact reserved rooms to match. Ignored when null or negative.
         * @return Specification for equality on `reservedRooms` or null when input is invalid.
         */
        return ((root, query, criteriaBuilder) -> {
            if (reservedRooms == null || reservedRooms < 0) return null;

            return criteriaBuilder.equal(root.get("reservedRooms"), reservedRooms);
        });
    }
    public static Specification<Hotel> hasContactPhone(String contactPhone){
        /**
         * Filters hotels by contact phone number (exact match).
         *
         * @param contactPhone phone string to match. Ignored when null or empty.
         * @return Specification for equality on `contactPhone` or null when input is invalid.
         */
        return ((root, query, criteriaBuilder) -> {
            if (contactPhone == null || contactPhone.isEmpty()) return null;

            return criteriaBuilder.equal(root.get("contactPhone"), contactPhone);
        });
    }


    public static Specification<Hotel> hasAddressRelationship(String name){
        return ((root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;

            return criteriaBuilder.equal(root.get("name"), name);
        });
    }
    public static Specification<Hotel> hasRoomRelationship(String name){
        return ((root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;

            return criteriaBuilder.equal(root.get("name"), name);
        });
    }
    public static Specification<Hotel> hasBenefitRelationship(String name){
        return ((root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;

            return criteriaBuilder.equal(root.get("name"), name);
        });
    }
    public static Specification<Hotel> hasAttractionRelationship(String name){
        /**
         * Placeholder specification to filter by attraction relationship.
         *
         * Note: current implementation is a stub that filters by `name` on root
         * and should be extended to join to the `attractions` association.
         *
         * @param name relationship name or identifier. Ignored when null or empty.
         * @return Specification or null when input is invalid.
         */
        return ((root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;

            return criteriaBuilder.equal(root.get("name"), name);
        });
    }

}
