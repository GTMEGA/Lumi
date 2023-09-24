package com.falsepattern.lumina.api.cache;

import com.falsepattern.lumina.api.storage.LumiBlockStorage;
import org.jetbrains.annotations.NotNull;

public interface LumiBlockCache extends LumiBlockStorage {
    @NotNull LumiBlockCacheRoot lumi$root();

    @NotNull String lumi$BlockCacheID();

    /**
     * Should be called at the end of each tick.
     * <p>
     * On some implementations this will do nothing.
     */
    void lumi$clearCache();
}
