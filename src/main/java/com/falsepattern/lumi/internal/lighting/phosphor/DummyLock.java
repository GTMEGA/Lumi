/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DummyLock implements Lock {
    private static final DummyLock INSTANCE = new DummyLock();

    public static Lock getDummyLock() {
        return INSTANCE;
    }

    @Override
    public void lock() {}

    @Override
    public void lockInterruptibly() {}

    @Override
    public boolean tryLock() {return true;}

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) {return true;}

    @Override
    public void unlock() {}

    @NotNull
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}
