package ar.mcp.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoomType {
    STANDARD(1.10F),
    DELUXE(15),
    SUITE(20),
    EXECUTIVE(1.30F),
    PRESIDENTIAL(1.45F);

    private final float multiplier;
}
