package com.falsepattern.lumina.internal.cache;

import com.falsepattern.lumina.api.cache.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.world.LumiWorldRoot;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class BlockCaches {
    public static LumiBlockCacheRoot createFallbackBlockCacheRoot(@NotNull LumiWorldRoot worldRoot) {
        return worldRoot.lumi$isClientSide() ?
               new ReadThroughBlockCacheRoot(worldRoot) : new FocusedBlockCacheRoot(worldRoot);
    }
}
