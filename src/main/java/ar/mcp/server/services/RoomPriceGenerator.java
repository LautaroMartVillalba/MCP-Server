package ar.mcp.server.services;


import ar.mcp.server.domain.enums.BedsType;
import ar.mcp.server.domain.enums.RoomType;

import java.math.BigDecimal;

/**
 * Utility class to calculate the price of a hotel room based on multiple factors.
 * Factors include base price, room type, bed type, floor, and people capacity.
 */
public class RoomPriceGenerator {

    /** Base price for a room before applying any multipliers. */
    private static final BigDecimal BASE_ROOM_PRICE = BigDecimal.valueOf(20);

    /**
     * Calculates the price increase factor based on the floor number.
     *
     * @param floor Floor number of the room.
     * @return Multiplier to apply for floor-based price increase.
     * @throws RuntimeException if floor is less than or equal to 0.
     */
    private static float priceIncreaseByFloor (int floor){
        if (floor <= 0){
            throw new RuntimeException("Please, insert a valid floor number");
        }
        if (floor > 15){
            return 1.15F;
        }
        return (float) floor/100+1;
    }


    /**
     * Calculates the price increase factor based on the room's people capacity.
     *
     * @param peopleCapacity Number of people the room can accommodate.
     * @return Multiplier to apply for people capacity.
     * @throws RuntimeException if peopleCapacity is not between 1 and 4.
     */
    private static float priceIncreaseByPeopleCapacity(int peopleCapacity){
        switch (peopleCapacity){
            case 1 -> {
                return 1.10F;
            }
            case 2 -> {
                return  1.18F;
            }
            case 3 -> {
                return 1.25F;
            }
            case 4 -> {
                return 1.33F;
            }
        }
        throw new RuntimeException("Please, insert a valid people capacity.");
    }

    /**
     * Generates the final price of a room based on its attributes.
     *
     * @param roomType       {@link RoomType} of the room.
     * @param bedsType       {@link BedsType} of the room.
     * @param floor          Floor number of the room.
     * @param peopleCapacity Number of people the room can accommodate.
     * @return {@link BigDecimal} representing the calculated room price.
     * @throws RuntimeException if any input is invalid.
     */
    public static BigDecimal priceGenerator(RoomType roomType, BedsType bedsType, int floor, int peopleCapacity){
        return BASE_ROOM_PRICE
                .multiply(BigDecimal.valueOf(roomType.getMultiplier()))
                .multiply(BigDecimal.valueOf(bedsType.getMultiplier()))
                .multiply(BigDecimal.valueOf(priceIncreaseByFloor(floor)))
                .multiply(BigDecimal.valueOf(priceIncreaseByPeopleCapacity(peopleCapacity)));
    }

}
