package ar.mcp.server.services.hotel;

import ar.mcp.server.domain.entities.Attraction;
import ar.mcp.server.domain.entities.Benefit;
import ar.mcp.server.domain.entities.Hotel;
import ar.mcp.server.domain.entities.Room;
import ar.mcp.server.domain.entities.address.Address;
import ar.mcp.server.domain.enums.RoomType;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
         * Note: Only fetches address and rooms to avoid "cannot simultaneously fetch multiple bags" error.
         * Collections (benefits, attractions) should be loaded separately or use JOIN instead of FETCH.
         *
         * @return Specification that performs LEFT fetch joins for address->state and rooms
         */
        return (root, query, criteriaBuilder) -> {
            if (Hotel.class.equals(query.getResultType())) {

                root.fetch("rooms", JoinType.LEFT);
                // Note: Cannot fetch both 'benefits' and 'attractions' simultaneously (multiple bags error)
                // These will be lazy-loaded or loaded in separate queries

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

    /**
     * Filters hotels by star rating within a range (inclusive).
     *
     * @param minStars minimum star rating (inclusive). Null or < 0.1 will be ignored.
     * @param maxStars maximum star rating (inclusive). Null or < 0.1 will be ignored.
     * @return Specification for star range or null when both inputs are invalid.
     */
    public static Specification<Hotel> hasStarsInRange(Double minStars, Double maxStars){
        return ((root, query, criteriaBuilder) -> {
            if ((minStars == null || minStars < 0.1) && (maxStars == null || maxStars < 0.1)) return null;

            Predicate predicate = criteriaBuilder.conjunction();
            
            if (minStars != null && minStars >= 0.1) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.greaterThanOrEqualTo(root.get("stars"), minStars));
            }
            
            if (maxStars != null && maxStars >= 0.1) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.lessThanOrEqualTo(root.get("stars"), maxStars));
            }
            
            return predicate;
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

    /**
     * Filters hotels that have rooms with prices within the specified range (inclusive).
     *
     * @param minPrice minimum price per night (inclusive). Null will be ignored.
     * @param maxPrice maximum price per night (inclusive). Null will be ignored.
     * @return Specification for room price range or null when both inputs are null.
     */
    public static Specification<Hotel> hasRoomsWithPriceInRange(BigDecimal minPrice, BigDecimal maxPrice){
        return ((root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) return null;

            Join<Hotel, Room> roomJoin = root.join("rooms", JoinType.INNER);
            
            Predicate predicate = criteriaBuilder.conjunction();
            
            if (minPrice != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.greaterThanOrEqualTo(roomJoin.get("pricePerNight"), minPrice));
            }
            
            if (maxPrice != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.lessThanOrEqualTo(roomJoin.get("pricePerNight"), maxPrice));
            }
            
            return predicate;
        });
    }

    /**
     * Filters hotels that have rooms of the specified category/type.
     *
     * @param roomType the room type/category to filter by (e.g., PRESIDENTIAL, SUITE).
     * @return Specification for room type or null when input is null.
     */
    public static Specification<Hotel> hasRoomsOfType(RoomType roomType){
        return ((root, query, criteriaBuilder) -> {
            if (roomType == null) return null;

            Join<Hotel, Room> roomJoin = root.join("rooms", JoinType.INNER);
            
            return criteriaBuilder.equal(roomJoin.get("roomType"), roomType);
        });
    }

    /**
     * Filters hotels that have attractions matching any of the provided keywords in their description.
     * Uses OR logic - hotel matches if ANY attraction contains ANY keyword.
     *
     * @param keywords List of keywords to search for in attraction descriptions (case-insensitive).
     * @return Specification for attraction description keywords or null when input is null/empty.
     */
    public static Specification<Hotel> hasAttractionsWithDescriptionKeywords(List<String> keywords){
        return ((root, query, criteriaBuilder) -> {
            if (keywords == null || keywords.isEmpty()) return null;

            Join<Hotel, Attraction> attractionJoin = root.join("attractions", JoinType.INNER);
            
            List<Predicate> predicates = keywords.stream()
                    .filter(keyword -> keyword != null && !keyword.isBlank())
                    .map(keyword -> criteriaBuilder.like(
                            criteriaBuilder.lower(attractionJoin.get("description")),
                            "%" + keyword.toLowerCase() + "%"
                    ))
                    .collect(Collectors.toList());
            
            if(predicates.isEmpty()) {
                return null;
            }
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        });
    }

    /**
     * Filters hotels that have benefits matching any of the provided keywords in their description.
     * Uses OR logic - hotel matches if ANY benefit contains ANY keyword.
     *
     * @param keywords List of keywords to search for in benefit descriptions (case-insensitive).
     * @return Specification for benefit description keywords or null when input is null/empty.
     */
    public static Specification<Hotel> hasBenefitsWithDescriptionKeywords(List<String> keywords){
        return ((root, query, criteriaBuilder) -> {
            if (keywords == null || keywords.isEmpty()) return null;

            Join<Hotel, Benefit> benefitJoin = root.join("benefits", JoinType.INNER);
            
            List<Predicate> predicates = keywords.stream()
                    .filter(keyword -> keyword != null && !keyword.isBlank())
                    .map(keyword -> criteriaBuilder.like(
                            criteriaBuilder.lower(benefitJoin.get("description")),
                            "%" + keyword.toLowerCase() + "%"
                    ))
                    .collect(Collectors.toList());
            
            if(predicates.isEmpty()) {
                return null;
            }
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        });
    }

}
