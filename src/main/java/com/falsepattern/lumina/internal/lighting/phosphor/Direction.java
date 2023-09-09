/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static lombok.AccessLevel.PACKAGE;

@Getter(PACKAGE)
@Accessors(fluent = true, chain = false)
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
