/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.falsepattern.lumi.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumi.api.chunk.LumiChunkRoot;
import com.falsepattern.lumi.api.world.LumiWorld;
import com.falsepattern.lumi.api.world.LumiWorldRoot;
import com.falsepattern.lumi.internal.mixin.interfaces.LumiWorldRootCache;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.lumi.api.init.LumiWorldInitHook.LUMI_WORLD_INIT_HOOK_INFO;
import static com.falsepattern.lumi.api.init.LumiWorldInitHook.LUMI_WORLD_INIT_HOOK_METHOD;
import static com.falsepattern.lumi.internal.mixin.plugin.MixinPlugin.LUMI_ROOT_IMPL_MIXIN_PRIORITY;

@Unique
@Mixin(value = World.class, priority = LUMI_ROOT_IMPL_MIXIN_PRIORITY)
public abstract class LumiWorldRootImplMixin implements IBlockAccess, LumiWorldRoot, LumiWorldRootCache {
    // region Shadow
    @Final
    @Shadow
    public WorldProvider provider;


    @Shadow
    protected IChunkProvider chunkProvider;
    @Shadow
    public boolean isRemote;

    @Shadow
    public abstract Block getBlock(int posX, int posY, int posZ);

    @Shadow
    public abstract boolean isAirBlock(int posX, int posY, int posZ);

    @Shadow
    public abstract boolean doChunksNearChunkExist(int centerPosX, int centerPosY, int centerPosZ, int blockRange);

    @Shadow
    public abstract boolean checkChunksExist(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ);

    @Shadow
    public abstract int getBlockMetadata(int posX, int posY, int posZ);

    @Shadow
    public abstract void func_147479_m(int posX, int posY, int posZ);

    @Shadow
    public abstract TileEntity getTileEntity(int posX, int posY, int posZ);

    @Shadow
    public abstract boolean func_147451_t(int posX, int posY, int posZ);
    // endregion

    private LumiWorld[] lumi$lumiWorlds;

    @Override
    public LumiWorld[] lumi$getLumiWorlds() {
        return lumi$lumiWorlds;
    }

    @Override
    public void lumi$setLumiWorlds(LumiWorld[] lumiWorlds) {
        lumi$lumiWorlds = lumiWorlds;
    }

    @Inject(method = LUMI_WORLD_INIT_HOOK_METHOD,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @Dynamic(LUMI_WORLD_INIT_HOOK_INFO)
    private void lumiWorldRootInit(CallbackInfo ci) {}

    // region World Root
    @Override
    public @NotNull String lumi$worldRootID() {
        return "lumi_world_root";
    }

    @Override
    public void lumi$markBlockForRenderUpdate(int posX, int posY, int posZ) {
        if (isRemote)
            func_147479_m(posX, posY, posZ);
    }

    @Override
    public void lumi$scheduleLightingUpdate(int posX, int posY, int posZ) {
        func_147451_t(posX, posY, posZ);
    }

    @Override
    public @NotNull IChunkProvider lumi$chunkProvider() {
        return chunkProvider;
    }

    @Override
    public boolean lumi$doChunksExistInRange(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ) {
        return checkChunksExist(minPosX, minPosY, minPosZ, maxPosX, maxPosY, maxPosZ);
    }

    @Override
    public boolean lumi$doChunksExistInRange(int centerPosX, int centerPosY, int centerPosZ, int blockRange) {
        return doChunksNearChunkExist(centerPosX, centerPosY, centerPosZ, blockRange);
    }

    @Override
    public @Nullable LumiChunkRoot lumi$getChunkRootFromBlockPosIfExists(int posX, int posZ) {
        val chunkPosX = posX >> 4;
        val chunkPosZ = posZ >> 4;
        return lumi$getChunkRootFromChunkPosIfExists(chunkPosX, chunkPosZ);
    }

    @Override
    public @Nullable LumiChunkRoot lumi$getChunkRootFromChunkPosIfExists(int chunkPosX, int chunkPosZ) {
        if (chunkProvider == null)
            return null;

        if (chunkProvider instanceof ChunkProviderServer) {
            val chunkProviderServer = (ChunkProviderServer) chunkProvider;
            val loadedChunks = chunkProviderServer.loadedChunkHashMap;
            if (loadedChunks != null) {
                val chunk = loadedChunks.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(chunkPosX, chunkPosZ));
                if (chunk instanceof LumiChunkRoot && !(chunk instanceof EmptyChunk))
                    return (LumiChunkRoot) chunk;
            }
            return null;
        }

        if (chunkProvider.chunkExists(chunkPosX, chunkPosZ)) {
            val chunk = chunkProvider.provideChunk(chunkPosX, chunkPosZ);
            if (chunk instanceof LumiChunkRoot && !(chunk instanceof EmptyChunk))
                return (LumiChunkRoot) chunk;
        }
        return null;
    }
    // endregion

    // region Block Storage Root
    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "lumi_world_root";
    }

    @Override
    public boolean lumi$isClientSide() {
        return isRemote;
    }

    @Override
    public boolean lumi$hasSky() {
        return !provider.hasNoSky;
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
    // endregion
}
