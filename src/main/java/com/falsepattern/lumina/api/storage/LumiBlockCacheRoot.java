package com.falsepattern.lumina.api.storage;

import org.jetbrains.annotations.NotNull;

public interface LumiBlockCacheRoot extends LumiBlockStorageRoot {
    @NotNull String lumi$blockCacheRootID();

    /**
     * Should be called at the end of each tick.
     * <p>
     * On some implementations this will do nothing.
     */
    void lumi$clearCache();
}
