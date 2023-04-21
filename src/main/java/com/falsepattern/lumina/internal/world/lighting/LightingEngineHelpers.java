package com.falsepattern.lumina.internal.world.lighting;

import com.falsepattern.lumina.api.ILumiWorld;
import com.falsepattern.lumina.api.ILumiChunk;
import com.falsepattern.lumina.api.ILumiEBS;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class LightingEngineHelpers {
    private static final Block DEFAULT_BLOCK_STATE = Blocks.air;

    // Avoids some additional logic in Chunk#getBlockState... 0 is always air
    static Block posToState(final BlockPos pos, final ILumiChunk chunk) {
        return posToState(pos, chunk.lumiEBS(pos.getY() >> 4));
    }

    static Block posToState(final BlockPos pos, final ILumiEBS section) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        if (section != null)
        {
            return section.root().getBlockByExtId(x & 15, y & 15, z & 15);
        }

        return DEFAULT_BLOCK_STATE;
    }

    public static ILumiChunk getLoadedChunk(ILumiWorld world, int chunkX, int chunkZ) {
        val provider = world.root().provider();
        if(!provider.chunkExists(chunkX, chunkZ))
            return null;
        return world.wrap(provider.provideChunk(chunkX, chunkZ));
    }
}
