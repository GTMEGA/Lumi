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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.api.ILumiWorldProvider;
import com.falsepattern.lumina.internal.LumiWorldManager;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(value = Chunk.class)
public abstract class MixinChunk {
    private static final String SET_BLOCK_STATE_VANILLA = "func_150807_a(IIILnet/minecraft/block/Block;I)Z";

    @Shadow
    public World worldObj;
    @Shadow
    public boolean isTerrainPopulated;

    /**
     * Callback injected to the head of getLightSubtracted(BlockPos, int) to force deferred light updates to be processed.
     *
     * @author Angeline
     */
    @Inject(method = "getBlockLightValue",
            at = @At("HEAD"))
    private void onGetLightSubtracted(int x, int y, int z, int amount, CallbackInfoReturnable<Integer> cir) {
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            LumiWorldManager.getWorld(worldObj, i).getLightingEngine().processLightUpdates();
        }
    }

    /**
     * Callback injected at the end of onLoad() to have previously scheduled light updates scheduled again.
     *
     * @author Angeline
     */
    @Inject(method = "onChunkLoad",
            at = @At("RETURN"))
    private void onLoad(CallbackInfo ci) {
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.wrap((Chunk) (Object) this);
            LightingHooks.scheduleRelightChecksForChunkBoundaries(world, chunk);
        }
    }

    // === REPLACEMENTS ===

    /**
     * Replaces the call in setLightFor(Chunk, EnumSkyBlock, BlockPos) with our hook.
     *
     * @author Angeline
     */
    @Redirect(method = "setLightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"),
              expect = 0)
    private void setLightForRedirectGenerateSkylightMap(Chunk chunk, EnumSkyBlock type, int x, int y, int z, int value) {
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val lChunk = world.wrap(chunk);
            LightingHooks.initSkylightForSection(world, lChunk, lChunk.getLumiEBS(y >> 4));
        }
    }

    /**
     * @reason Overwrites relightBlock with a more efficient implementation.
     * @author Angeline
     */
    @Overwrite
    public void relightBlock(int x, int y, int z) {
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val lChunk = world.wrap((Chunk) (Object)this);
            LightingHooks.relightBlock(lChunk, x, y, z);
        }
    }

    /**
     * @reason Hook for calculating light updates only as needed. {@link MixinChunk#getCachedLightFor(EnumSkyBlock, int, int, int)} does not
     * call this hook.
     * @author Angeline
     */
    @Overwrite
    public int getSavedLightValue(EnumSkyBlock type, int x, int y, int z) {
        int ret = 0;
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.wrap((Chunk) (Object) this);
            chunk.getLightingEngine().processLightUpdatesForType(type);
            if (i == 0) {
                ret = chunk.getCachedLightFor(type, x, y, z);
            }
        }
        return ret;
    }

    /**
     * @reason Hooks into checkLight() to check chunk lighting and returns immediately after, voiding the rest of the function.
     * @author Angeline
     */
    @Overwrite
    public void func_150809_p() {
        this.isTerrainPopulated = true;

        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.wrap((Chunk) (Object) this);
            LightingHooks.checkChunkLighting(chunk, world);
        }
    }

    /**
     * @reason Optimized version of recheckGaps. Avoids chunk fetches as much as possible.
     * @author Angeline
     */
    @Overwrite
    public void recheckGaps(boolean onlyOne) {
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.wrap((Chunk) (Object) this);
            LightingHooks.doRecheckGaps(chunk, onlyOne);
        }
    }


    /**
     * Redirects the construction of the ExtendedBlockStorage in setBlockState(BlockPos, IBlockState). We need to initialize
     * the skylight data for the constructed section as soon as possible.
     *
     * @author Angeline
     */
    @Redirect(method = SET_BLOCK_STATE_VANILLA,
              at = @At(value = "NEW",
                       args = "class=net/minecraft/world/chunk/storage/ExtendedBlockStorage"
              ),
              expect = 0
    )
    private ExtendedBlockStorage setBlockStateCreateSectionVanilla(int y, boolean storeSkylight) {
        return this.initSection(y, storeSkylight);
    }

    private ExtendedBlockStorage initSection(int y, boolean storeSkylight) {
        ExtendedBlockStorage storage = new ExtendedBlockStorage(y, storeSkylight);
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.wrap((Chunk) (Object) this);
            val ebs = world.wrap(storage);
            LightingHooks.initSkylightForSection(world, chunk, ebs);
        }

        return storage;
    }

    /**
     * Modifies the flag variable of setBlockState(BlockPos, IBlockState) to always be false after it is set.
     *
     * @author Angeline
     */
    @ModifyVariable(
            method = SET_BLOCK_STATE_VANILLA,
            at = @At(
                    value = "STORE",
                    ordinal = 1
            ),
            name = "flag",
            index = 11,
            allow = 1
    )
    private boolean setBlockStateInjectGenerateSkylightMapVanilla(boolean generateSkylight) {
        return false;
    }

    /**
     * Prevent propagateSkylightOcclusion from being called.
     * @author embeddedt
     */
    @Redirect(method = SET_BLOCK_STATE_VANILLA, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;propagateSkylightOcclusion(II)V"))
    private void doPropagateSkylight(Chunk chunk, int i1, int i2) {
        /* No-op, we don't want skylight propagated */
    }

    /**
     * Prevent getLightFor from being called.
     * @author embeddedt
     */
    @Redirect(method = SET_BLOCK_STATE_VANILLA, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"))
    private int getFakeLightFor(Chunk chunk, EnumSkyBlock skyBlock, int x, int y, int z) {
        return 0;
    }

    //TODO port
//    /**
//     * @author embeddedt
//     * @reason optimize random light checks so they complete faster
//     */
//    @Overwrite
//    public void enqueueRelightChecks() {
//        /* Skip object allocation if we weren't going to run checks anyway */
//        if (this.queuedLightChecks >= 4096) {
//            return;
//        }
//        boolean isActiveChunk = worldObj.activeChunkSet.contains(new ChunkCoordIntPair(this.xPosition, this.zPosition));
//        int lightRecheckSpeed;
//        if (worldObj.isRemote && isActiveChunk) {
//            lightRecheckSpeed = 256;
//        } else if (worldObj.isRemote) {
//            lightRecheckSpeed = 64;
//        } else {
//            lightRecheckSpeed = 32;
//        }
//        for (int i = 0; i < lightRecheckSpeed; ++i) {
//            if (this.queuedLightChecks >= 4096) {
//                return;
//            }
//
//            int section = this.queuedLightChecks % 16;
//            int x = this.queuedLightChecks / 16 % 16;
//            int z = this.queuedLightChecks / 256;
//            ++this.queuedLightChecks;
//            int bx = (this.xPosition << 4) + x;
//            int bz = (this.zPosition << 4) + z;
//
//            for (int y = 0; y < 16; ++y) {
//                int by = (section << 4) + y;
//                ExtendedBlockStorage storage = this.storageArrays[section];
//
//                boolean performFullLightUpdate = false;
//                if (storage == null && (y == 0 || y == 15 || x == 0 || x == 15 || z == 0 || z == 15)) {
//                    performFullLightUpdate = true;
//                } else if (storage != null) {
//                    Block block = storage.getBlockByExtId(x, y, z);
//                    if (block.getLightOpacity(this.worldObj, bx, by, bz) >= 255 &&
//                        block.getLightValue(this.worldObj, bx, by, bz) <= 0) {
//                        int prevLight = storage.getExtBlocklightValue(x, y, z);
//                        if (prevLight != 0) {
//                            storage.setExtBlocklightValue(x, y, z, 0);
//                            this.worldObj.markBlockRangeForRenderUpdate(bx, by, bz, bx, by, bz);
//                        }
//                    } else {
//                        performFullLightUpdate = true;
//                    }
//                }
//                if (performFullLightUpdate) {
//                    this.worldObj.func_147451_t(bx, by, bz);
//                }
//            }
//        }
//    }
}
