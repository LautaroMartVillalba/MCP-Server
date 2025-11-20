package ar.mcp.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BedsType {
    SINGLE(1.05F),
    DOUBLE(1.10F),
    QUEEN(1.20F),
    KING(1.25F),
    TWIN(1.35F);

    private final float multiplier;
}
