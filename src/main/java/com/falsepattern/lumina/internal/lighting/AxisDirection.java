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

package com.falsepattern.lumina.internal.lighting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true, chain = false)
@RequiredArgsConstructor
public enum AxisDirection {
    POSITIVE(1),
    NEGATIVE(-1);

    @Getter
    private final int sign;

    public AxisDirection opposite() {
        return opposite(this);
    }

    private static AxisDirection opposite(AxisDirection axisDirection) {
        switch (axisDirection) {
            case POSITIVE:
                return NEGATIVE;
            default:
            case NEGATIVE:
                return POSITIVE;
        }
    }
}
