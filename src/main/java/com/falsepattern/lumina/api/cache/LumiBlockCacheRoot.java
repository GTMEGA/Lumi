package com.falsepattern.lumina.api.cache;

import com.falsepattern.lumina.api.storage.LumiBlockStorageRoot;
import com.falsepattern.lumina.api.world.LumiWorld;
import org.jetbrains.annotations.NotNull;

public interface LumiBlockCacheRoot extends LumiBlockStorageRoot {
    @NotNull String lumi$blockCacheRootID();

    @NotNull LumiBlockCache lumi$createBlockCache(LumiWorld world);

    int lumi$minChunkPosX();

    int lumi$minChunkPosZ();

    int lumi$maxChunkPosX();

    int lumi$maxChunkPosZ();

    /**
     * Should be called at the end of each tick.
     * <p>
     * On some implementations this will do nothing.
     */
    void lumi$clearCache();
}
