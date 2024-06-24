/*
 * This file is part of LUMI.
 *
 * LUMI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMI. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumi.internal.lighting.phosphor;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;

enum Direction {
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

    static final Direction[] VALID_DIRECTIONS;
    static final Direction[] HORIZONTAL_DIRECTIONS;

    static final int VALID_DIRECTIONS_SIZE = 6;
    static final int HORIZONTAL_DIRECTIONS_SIZE = 4;

    static {
        VALID_DIRECTIONS = new Direction[]{DOWN, UP, NORTH, SOUTH, EAST, WEST};
        HORIZONTAL_DIRECTIONS = new Direction[]{NORTH, SOUTH, EAST, WEST};
    }

    final ForgeDirection forgeDirection;
    final EnumFacing baseFacing;

    final int xOffset;
    final int yOffset;
    final int zOffset;

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
