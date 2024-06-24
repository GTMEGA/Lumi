/*
 * This file is part of LUMI.
 *
 * LUMI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMI. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumi.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumi.api.chunk.LumiChunkRoot;
import com.falsepattern.lumi.api.storage.LumiBlockStorageRoot;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.falsepattern.lumi.internal.mixin.plugin.MixinPlugin.LUMI_ROOT_IMPL_MIXIN_PRIORITY;

@Unique
@Mixin(value = ChunkCache.class, priority = LUMI_ROOT_IMPL_MIXIN_PRIORITY)
public abstract class LumiBlockCacheRootImplMixin implements IBlockAccess, LumiBlockStorageRoot {
    // region Shadow
    @Shadow
    private World worldObj;

    @Shadow
    public abstract Block getBlock(int posX, int posY, int posZ);

    @Shadow
    public abstract boolean isAirBlock(int posX, int posY, int posZ);

    @Shadow
    public abstract int getBlockMetadata(int posX, int posY, int posZ);

    @Shadow
    public abstract TileEntity getTileEntity(int posX, int posY, int posZ);
    // endregion

    @Shadow private int chunkX;

    @Shadow private int chunkZ;

    @Shadow private Chunk[][] chunkArray;

    @Shadow private boolean isEmpty;

    // region Block Storage Root
    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "lumi_block_cache_root";
    }

    @Override
    public boolean lumi$isClientSide() {
        return worldObj.isRemote;
    }

    @Override
    public boolean lumi$hasSky() {
        return !worldObj.provider.hasNoSky;
    }

    @Override
    public @NotNull Block lumi$getBlock(int posX, int posY, int posZ) {
        return getBlock(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        return getBlockMetadata(posX, posY, posZ);
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        return isAirBlock(posX, posY, posZ);
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        return getTileEntity(posX, posY, posZ);
    }

    @Override
    public @Nullable LumiChunkRoot lumi$getChunkRootFromBlockPosIfExists(int posX, int posZ) {
        val chunkPosX = posX >> 4;
        val chunkPosZ = posZ >> 4;
        return lumi$getChunkRootFromChunkPosIfExists(chunkPosX, chunkPosZ);
    }

    @Override
    public @Nullable LumiChunkRoot lumi$getChunkRootFromChunkPosIfExists(int chunkPosX, int chunkPosZ) {
        checks:
        {
            if (isEmpty)
                break checks;

            val xChunkIndex = chunkPosX - chunkX;
            val zChunkIndex = chunkPosZ - chunkZ;

            if (xChunkIndex < 0 || xChunkIndex >= chunkArray.length)
                break checks;

            val zChunkArray = chunkArray[xChunkIndex];
            if (zChunkArray == null)
                break checks;
            if (zChunkIndex < 0 || zChunkIndex >= zChunkArray.length)
                break checks;

            val chunkBase = zChunkArray[zChunkIndex];
            if (chunkBase instanceof LumiChunkRoot && !(chunkBase instanceof EmptyChunk))
                return (LumiChunkRoot) chunkBase;
        }
        return null;
    }

    // endregion
}
