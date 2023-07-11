/*
 * Copyright (c) 2023 FalsePattern, Ven
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
 */

package com.falsepattern.lumina.api.coordinate;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Accessors(fluent = true, chain = false)
public enum Direction {
    // @formatter:off
    DOWN(   ForgeDirection.DOWN,    EnumFacing.DOWN),
    UP(     ForgeDirection.UP,      EnumFacing.UP),
    NORTH(  ForgeDirection.NORTH,   EnumFacing.NORTH),
    SOUTH(  ForgeDirection.SOUTH,   EnumFacing.SOUTH),
    EAST(   ForgeDirection.EAST,    EnumFacing.EAST),
    WEST(   ForgeDirection.WEST,    EnumFacing.WEST),
    UNKNOWN(ForgeDirection.UNKNOWN, EnumFacing.DOWN),
    // @formatter:on
    ;

    private static final List<Direction> VALID_DIRECTIONS;
    private static final List<Direction> HORIZONTAL_DIRECTIONS;
    private static final List<Direction> VERTICAL_DIRECTIONS;

    static {
        VALID_DIRECTIONS = Collections.unmodifiableList(Arrays.asList(DOWN, UP, NORTH, SOUTH, EAST, WEST));
        HORIZONTAL_DIRECTIONS = Collections.unmodifiableList(Arrays.asList(NORTH, SOUTH, EAST, WEST));
        VERTICAL_DIRECTIONS = Collections.unmodifiableList(Arrays.asList(DOWN, UP));
    }

    private final ForgeDirection forgeDirection;
    private final EnumFacing baseFacing;

    private final int xOffset;
    private final int yOffset;
    private final int zOffset;

    Direction(ForgeDirection forgeDirection, EnumFacing baseFacing) {
        this.forgeDirection = forgeDirection;
        this.baseFacing = baseFacing;

        this.xOffset = forgeDirection.offsetX;
        this.yOffset = forgeDirection.offsetY;
        this.zOffset = forgeDirection.offsetZ;
    }

    public Direction opposite() {
        return opposite(this);
    }

    public static Direction of(ForgeDirection forgeDirection) {
        switch (forgeDirection) {
            case DOWN:
                return DOWN;
            case UP:
                return UP;
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            case EAST:
                return EAST;
            default:
            case UNKNOWN:
                return UNKNOWN;
        }
    }

    public static Direction of(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                return DOWN;
            case UP:
                return UP;
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            case EAST:
                return EAST;
            default:
                return UNKNOWN;
        }
    }

    public static @Unmodifiable List<Direction> validDirections() {
        return VALID_DIRECTIONS;
    }

    public static @Unmodifiable List<Direction> horizontalDirections() {
        return HORIZONTAL_DIRECTIONS;
    }

    public static @Unmodifiable List<Direction> verticalDirections() {
        return VERTICAL_DIRECTIONS;
    }

    private static Direction opposite(Direction direction) {
        switch (direction) {
            case DOWN:
                return UP;
            case UP:
                return DOWN;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            default:
            case UNKNOWN:
                return UNKNOWN;
        }
    }
}
