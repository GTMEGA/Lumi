/*
 * Copyright (C) 2023 FalsePattern
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
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
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
    @Override
    public abstract Block getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract int getBlockMetadata(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract void setChunkModified();

    // region LumiChunkRoot
    @Override
    public void markDirty() {
        setChunkModified();
    }

    @Override
    public int getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlockMetadata(subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public void prepareSubChunk(int posY) {
        val ebs = storageArrays[posY >> 4];

        if (ebs == null) {
            storageArrays[posY >> 4] = new ExtendedBlockStorage(posY >> 4 << 4, !worldObj.provider.hasNoSky);
            for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
                val world = LumiWorldManager.getWorld(worldObj, i);
                val lChunk = world.toLumiChunk((Chunk) (Object) this);
                LightingHooks.initSkylightForSection(world, lChunk, lChunk.subChunk(posY >> 4));
            }
        }

        markDirty();
    }

    @Override
    public void rootIsGapLightingUpdated(boolean b) {
        isGapLightingUpdated = b;
    }

    @Override
    public int topExistingSubChunkIndex() {
        return getTopFilledSegment();
    }

    @Override
    public int[] precipitationHeightArray() {
        return precipitationHeightMap;
    }
    // endregion
}
