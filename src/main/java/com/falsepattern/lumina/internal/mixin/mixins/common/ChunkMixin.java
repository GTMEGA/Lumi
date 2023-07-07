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

import com.falsepattern.lumina.internal.world.LumiWorldManager;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Final
    @Shadow
    public int xPosition;
    @Final
    @Shadow
    public int zPosition;

    @Shadow
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    public World worldObj;
    @Shadow
    public boolean isTerrainPopulated;
    @Shadow
    public boolean isLightPopulated;
    @Shadow
    private int queuedLightChecks;

    /**
     * Callback injected to the head of getLightSubtracted(BlockPos, int) to force deferred light updates to be processed.
     *
     * @author Angeline
     */
    @Inject(method = "getBlockLightValue",
            at = @At("HEAD"))
    private void onGetLightSubtracted(int x, int y, int z, int amount, CallbackInfoReturnable<Integer> cir) {
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++)
            LumiWorldManager.getWorld(worldObj, i).lightingEngine().processLightUpdates();
    }

    /**
     * Callback injected at the end of onLoad() to have previously scheduled light updates scheduled again.
     *
     * @author Angeline
     */
    @Inject(method = "onChunkLoad",
            at = @At("RETURN"))
    private void onLoad(CallbackInfo ci) {
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(worldObj, i);
            val lumiChunk = lumiWorld.toLumiChunk(thiz());
            LightingHooks.scheduleRelightChecksForChunkBoundaries(lumiWorld, lumiChunk);
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
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(worldObj, i);
            val lumiChunk = lumiWorld.toLumiChunk(chunk);
            LightingHooks.initSkylightForSection(lumiWorld, lumiChunk, lumiChunk.subChunk(y >> 4));
        }
    }

    /**
     * @author FalsePattern
     * @reason Blanking, this is not called anymore
     */
    @Overwrite
    private void relightBlock(int x, int y, int z) {
    }

    /**
     * @author FalsePattern
     * @reason Fix
     */
    @Overwrite
    public void generateSkylightMap() {
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(worldObj, i);
            val lumiChunk = lumiWorld.toLumiChunk((Chunk) (Object) this);
            LightingHooks.generateSkylightMap(lumiChunk);
        }
    }

    /**
     * @reason Hook for calculating light updates only as needed.
     * @author Angeline
     */
    @Overwrite
    public int getSavedLightValue(EnumSkyBlock type, int x, int y, int z) {
        var lightValue = 0;

        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(worldObj, i);
            val lumiChunk = lumiWorld.toLumiChunk(thiz());
            lumiChunk.lightingEngine().processLightUpdatesForType(type);
            if (i == 0)
                lightValue = LightingHooks.getCachedLightFor(lumiChunk, type, x, y, z);
        }

        return lightValue;
    }

    /**
     * @reason Hooks into checkLight() to check chunk lighting and returns immediately after, voiding the rest of the function.
     * @author Angeline
     */
    @Overwrite
    public void func_150809_p() {
        isTerrainPopulated = true;
        var doLightPop = true;

        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(worldObj, i);
            val lumiChunk = lumiWorld.toLumiChunk((Chunk) (Object) this);
            doLightPop &= LightingHooks.checkChunkLighting(lumiChunk, lumiWorld);
        }

        if (doLightPop)
            isLightPopulated = true;
    }

    /**
     * @reason Optimized version of recheckGaps. Avoids chunk fetches as much as possible.
     * @author Angeline
     */
    @Overwrite
    private void recheckGaps(boolean onlyOne) {
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(worldObj, i);
            val lumiChunk = lumiWorld.toLumiChunk(thiz());
            LightingHooks.doRecheckGaps(lumiChunk, onlyOne);
        }
    }

    @Redirect(method = "func_150807_a(IIILnet/minecraft/block/Block;I)Z",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;relightBlock(III)V"),
              require = 2)
    private void disableRelight(Chunk thiz, int posX, int posY, int posZ) {
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"),
              require = 1)
    private void disableGenSky(Chunk thiz) {
    }

    /**
     * Prevent propagateSkylightOcclusion from being called.
     *
     * @author embeddedt
     */
    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;propagateSkylightOcclusion(II)V"),
              require = 1)
    private void disableSkyPropagate(Chunk chunk, int posX, int posZ) {
        /* No-op, we don't want skylight propagated */
    }

    /**
     * Prevent getLightFor from being called.
     *
     * @author embeddedt
     */
    @Redirect(method = "func_150807_a(IIILnet/minecraft/block/Block;I)Z",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"),
              require = 2)
    private int disableGetSavedLightValue(Chunk chunk, EnumSkyBlock lightType, int posX, int posY, int posZ) {
        return 0;
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;getLightOpacity(Lnet/minecraft/world/IBlockAccess;III)I"),
              require = 2)
    private int disableGetLightOpacity(Block block, IBlockAccess world, int posX, int posY, int posZ) {
        return 0;
    }

    @Inject(method = "func_150807_a",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setExtBlockMetadata(IIII)V",
                     ordinal = 1,
                     shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    private void doCustomRelightChecks(int cX,
                                       int cY,
                                       int cZ,
                                       Block block,
                                       int p_150807_5_,
                                       CallbackInfoReturnable<Boolean> cir,
                                       int i1,
                                       int k,
                                       Block block1,
                                       int k1,
                                       ExtendedBlockStorage extendedblockstorage,
                                       boolean flag,
                                       int l1,
                                       int i2,
                                       int k2) {
        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (int i = 0; i < lumiWorldCount; i++) {
            val lumiWorld = LumiWorldManager.getWorld(worldObj, i);
            val lumiChunk = lumiWorld.toLumiChunk(thiz());
            val height = lumiChunk.minSkyLightColumns()[cZ << 4 | cX];

            if (cY >= height - 1)
                LightingHooks.relightBlock(lumiChunk, cX, cY + 1, cZ);
        }
    }

    /**
     * Redirects the construction of the ExtendedBlockStorage in setBlockState(BlockPos, IBlockState). We need to initialize
     * the skylight data for the constructed section as soon as possible.
     *
     * @author Angeline
     */
    @Redirect(method = "func_150807_a",
              at = @At(value = "NEW",
                       args = "class=net/minecraft/world/chunk/storage/ExtendedBlockStorage"
              ),
              expect = 0
    )
    private ExtendedBlockStorage setBlockStateCreateSectionVanilla(int y, boolean storeSkylight) {
        return initSubChunk(y, storeSkylight);
    }

    /**
     * @author embeddedt
     * @reason optimize random light checks so they complete faster
     */
    @Overwrite
    public void enqueueRelightChecks() {
        /* Skip object allocation if we weren't going to run checks anyway */
        if (queuedLightChecks >= 4096)
            return;
        final boolean isActiveChunk = worldObj.activeChunkSet.contains(new ChunkCoordIntPair(xPosition, zPosition));
        final int lightRecheckSpeed;
        if (worldObj.isRemote && isActiveChunk) {
            lightRecheckSpeed = 256;
        } else if (worldObj.isRemote)
            lightRecheckSpeed = 64;
        else
            lightRecheckSpeed = 32;
        for (int i = 0; i < lightRecheckSpeed; ++i) {
            if (queuedLightChecks >= 4096) {
                return;
            }

            final int section = queuedLightChecks % 16;
            final int x = queuedLightChecks / 16 % 16;
            final int z = queuedLightChecks / 256;
            ++this.queuedLightChecks;
            final int bx = (xPosition << 4) + x;
            final int bz = (zPosition << 4) + z;

            val lumiCount = LumiWorldManager.lumiWorldCount();
            for (int y = 0; y < 16; ++y) {
                final int by = (section << 4) + y;
                final ExtendedBlockStorage vanillaStorage = storageArrays[section];

                boolean performFullLightUpdate = false;
                if (vanillaStorage == null && (y == 0 || y == 15 || x == 0 || x == 15 || z == 0 || z == 15))
                    performFullLightUpdate = true;
                else if (vanillaStorage != null) {
                    final Block block = vanillaStorage.getBlockByExtId(x, y, z);
                    val meta = vanillaStorage.getExtBlockMetadata(x, y, z);
                    for (int l = 0; l < lumiCount; l++) {
                        val lumiWorld = LumiWorldManager.getWorld(worldObj, l);
                        val lumiStorage = lumiWorld.toLumiSubChunk(vanillaStorage);
                        if (lumiWorld.getBlockLightOpacity(block, meta, bx, by, bz) >= 255 &&
                            lumiWorld.getBlockLightValue(block, meta, bx, by, bz) <= 0) {
                            val bla = lumiStorage.blockLightValues();
                            final int prevLight = bla.get(x, y, z);
                            if (prevLight != 0) {
                                bla.set(x, y, z, 0);
                                worldObj.markBlockRangeForRenderUpdate(bx, by, bz, bx, by, bz);
                            }
                        } else {
                            performFullLightUpdate = true;
                            break;
                        }
                    }
                }
                if (performFullLightUpdate) {
                    worldObj.func_147451_t(bx, by, bz);
                }
            }
        }
    }

    private ExtendedBlockStorage initSubChunk(int posY, boolean hasSky) {
        val vanillaSubChunk = new ExtendedBlockStorage(posY, hasSky);

        val lumiWorldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < lumiWorldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.toLumiChunk(thiz());
            val subChunk = world.toLumiSubChunk(vanillaSubChunk);
            LightingHooks.initSkylightForSection(world, chunk, subChunk);
        }

        return vanillaSubChunk;
    }

    private Chunk thiz() {
        return (Chunk) (Object) this;
    }
}
