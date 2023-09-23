package com.falsepattern.lumina.api.storage;

import org.jetbrains.annotations.NotNull;

public interface LumiBlockCache extends LumiBlockStorage {
    @NotNull LumiBlockCacheRoot lumi$root();

    @NotNull String lumi$BlockCacheID();
}
