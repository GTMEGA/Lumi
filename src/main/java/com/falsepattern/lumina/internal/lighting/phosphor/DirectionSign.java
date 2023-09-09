/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;

import static com.falsepattern.lumina.internal.lighting.phosphor.Direction.EAST;
import static com.falsepattern.lumina.internal.lighting.phosphor.Direction.WEST;
import static lombok.AccessLevel.PACKAGE;

@Getter(PACKAGE)
@Accessors(fluent = true, chain = false)
@RequiredArgsConstructor
enum DirectionSign {
    POSITIVE(1),
    NEGATIVE(-1),
    ;

    @Getter
    private final int sign;

    DirectionSign opposite() {
        return opposite(this);
    }

    static DirectionSign of(Direction direction) {
        switch (direction) {
            default:
            case UP:
            case SOUTH:
            case EAST:
                return POSITIVE;
            case DOWN:
            case NORTH:
            case WEST:
                return NEGATIVE;
        }
    }

    static DirectionSign of(Direction direction, int facingOffsetX, int facingOffsetZ) {
        val subChunkPosX = facingOffsetX & 15;
        val subChunkPosZ = facingOffsetZ & 15;

        if (direction == EAST || direction == WEST) {
            if (subChunkPosZ < 8)
                return NEGATIVE;
        } else {
            if (subChunkPosX < 8)
                return NEGATIVE;
        }
        return POSITIVE;
    }

    private static DirectionSign opposite(DirectionSign directionSign) {
        switch (directionSign) {
            case POSITIVE:
                return NEGATIVE;
            default:
            case NEGATIVE:
                return POSITIVE;
        }
    }
}
