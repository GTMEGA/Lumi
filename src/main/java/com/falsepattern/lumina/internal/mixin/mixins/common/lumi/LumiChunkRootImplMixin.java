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

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.internal.lighting.LightingHooksOld;
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Chunk.class)
public abstract class LumiChunkRootImplMixin implements LumiChunkRoot {
    @Shadow
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    public int[] precipitationHeightMap;
    @Shadow
    public World worldObj;
    @Shadow
    private boolean isGapLightingUpdated;

    @Shadow
    public abstract int getTopFilledSegment();

    @Shadow
    public abstract Block getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract int getBlockMetadata(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract void setChunkModified();

    @Override
    public Chunk lumi$base() {
        return (Chunk) (Object) this;
    }

    @Override
    public Block lumi$getBlock(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlock(subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlockMetadata(subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public void lumi$prepareSubChunk(int chunkPosY) {
        chunkPosY &= 15;
        val subChunk = storageArrays[chunkPosY];

        if (subChunk == null) {
            storageArrays[chunkPosY] = new ExtendedBlockStorage(chunkPosY * 16, !worldObj.provider.hasNoSky);
            for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
                val world = LumiWorldManager.getWorld(worldObj, i);
                val lChunk = world.lumi$wrap((Chunk) (Object) this);
                LightingHooksOld.initSkyLightForSubChunk(world, lChunk, lChunk.lumi$subChunk(chunkPosY));
            }
        }

        lumi$markDirty();
    }

    @Override
    public boolean lumi$isSubChunkPrepared(int chunkPosY) {
        chunkPosY &= 15;
        val subChunk = storageArrays[chunkPosY];
        return subChunk != null;
    }

    @Override
    public int lumi$topPreparedSubChunkPosY() {
        return getTopFilledSegment();
    }

    @Override
    public void lumi$shouldRecheckLightingGaps(boolean shouldRecheckLightingGaps) {
        this.isGapLightingUpdated = shouldRecheckLightingGaps;
    }

    @Override
    public void lumi$markDirty() {
        setChunkModified();
    }
}
