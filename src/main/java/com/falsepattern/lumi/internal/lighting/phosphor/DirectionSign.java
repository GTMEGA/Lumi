/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.falsepattern.lumi.internal.lighting.phosphor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;

import static com.falsepattern.lumi.internal.lighting.phosphor.Direction.EAST;
import static com.falsepattern.lumi.internal.lighting.phosphor.Direction.WEST;
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
