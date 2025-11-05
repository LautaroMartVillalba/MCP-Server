package ar.mcp.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BedsType {
    SINGLE_BED(1.05F),
    DOUBLE_BED(1.10F),
    QUEEN_BED(1.20F),
    KING_BED(1.25F),
    TWIN_BED(1.35F);

    private final float multiplier;
}
