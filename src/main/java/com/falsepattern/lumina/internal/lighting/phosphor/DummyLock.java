package com.falsepattern.lumina.internal.lighting.phosphor;

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
