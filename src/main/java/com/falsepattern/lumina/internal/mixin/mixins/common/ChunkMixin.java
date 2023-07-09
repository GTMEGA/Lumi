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

package com.falsepattern.lumina.internal.mixin.mixins.common;

import com.falsepattern.lumina.internal.engine.LightingHooks;
import com.falsepattern.lumina.internal.world.LumiWorldManager;
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

    @Inject(method = "getBlockLightValue",
            at = @At("HEAD"),
            require = 1)
    private void processLightUpdatesOnSubtract(int subChunkPosX,
                                               int posY,
                                               int subChunkPosZ,
                                               int subtract,
                                               CallbackInfoReturnable<Integer> cir) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val lightingEngine = world.lumi$lightingEngine();
            lightingEngine.processLightUpdate();
        }
    }

    @Inject(method = "onChunkLoad",
            at = @At("RETURN"),
            require = 1)
    private void scheduleRelightOnLoad(CallbackInfo ci) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());
            LightingHooks.scheduleRelightChecksForChunkBoundaries(world, chunk);
        }
    }

    @Redirect(method = "setLightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"),
              require = 1)
    private void initSubChunkSkyLightOnSet(Chunk baseChunk,
                                           EnumSkyBlock lightType,
                                           int subChunkPosX,
                                           int posY,
                                           int subChunkPosZ,
                                           int value) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(baseChunk);

            val chunkPosY = posY / 16;
            val subChunk = chunk.lumi$subChunk(chunkPosY);
            LightingHooks.initSkyLightForSubChunk(world, chunk, subChunk);
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
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());
            LightingHooks.generateSkylightMap(chunk);
        }
    }

    /**
     * @reason Hook for calculating light updates only as needed.
     * @author Angeline
     */
    @Overwrite
    public int getSavedLightValue(EnumSkyBlock lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        var lightValue = 0;
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());
            val lightingEngine = chunk.lumi$lightingEngine();
            lightingEngine.processLightUpdate(lightType);

            val chunkLightValue = chunk.lumi$getLightValue(lightType, subChunkPosX, posY, subChunkPosZ);
            lightValue = Math.max(lightValue, chunkLightValue);
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
        isLightPopulated = true;

        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());
            isLightPopulated &= LightingHooks.checkChunkLighting(world, chunk);
        }
    }

    /**
     * @reason Optimized version of recheckGaps. Avoids chunk fetches as much as possible.
     * @author Angeline
     */
    @Overwrite
    private void recheckGaps(boolean onlyOne) {
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());
            LightingHooks.doRecheckGaps(chunk, onlyOne);
        }
    }

    @Redirect(method = "func_150807_a(IIILnet/minecraft/block/Block;I)Z",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;relightBlock(III)V"),
              require = 2)
    private void skipBlockRelight(Chunk thiz, int posX, int posY, int posZ) {
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"),
              require = 1)
    private void skipSkyLightGeneration(Chunk thiz) {
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;propagateSkylightOcclusion(II)V"),
              require = 1)
    private void skipSkyLightPropagation(Chunk chunk, int posX, int posZ) {
    }

    @Redirect(method = "func_150807_a(IIILnet/minecraft/block/Block;I)Z",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"),
              require = 2)
    private int alwaysZeroLightValue(Chunk chunk, EnumSkyBlock lightType, int posX, int posY, int posZ) {
        return 0;
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;getLightOpacity(Lnet/minecraft/world/IBlockAccess;III)I"),
              require = 2)
    private int alwaysZeroLightOpacity(Block block, IBlockAccess world, int posX, int posY, int posZ) {
        return 0;
    }

    @Inject(method = "func_150807_a",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setExtBlockMetadata(IIII)V",
                     ordinal = 1,
                     shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    private void relightBlocksOnBlockMetaChange(int subChunkPosX,
                                                int posY,
                                                int subChunkPosZ,
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
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());

            val index = subChunkPosX + (subChunkPosZ * 16);
            val maxPosY = chunk.lumi$skyLightHeights()[index];
            if (posY >= maxPosY - 1)
                LightingHooks.relightBlock(chunk, subChunkPosX, posY + 1, subChunkPosZ);
        }
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "NEW",
                       args = "class=net/minecraft/world/chunk/storage/ExtendedBlockStorage"),
              require = 1)
    private ExtendedBlockStorage createSubChunkWithInitializedSkyLight(int posY, boolean hasSky) {
        val baseSubChunk = new ExtendedBlockStorage(posY, hasSky);

        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(worldObj, i);
            val chunk = world.lumi$wrap(thiz());
            val subChunk = world.lumi$wrap(baseSubChunk);
            LightingHooks.initSkyLightForSubChunk(world, chunk, subChunk);

            // TODO: Can we put a world reference in it too?
        }

        return baseSubChunk;
    }

    /**
     * @author embeddedt
     * @reason optimize random light checks so they complete faster
     */
    @Overwrite
    public void enqueueRelightChecks() {
        if (queuedLightChecks >= (16 * 16 * 16))
            return;

        val isActiveChunk = worldObj.activeChunkSet.contains(new ChunkCoordIntPair(xPosition, zPosition));
        final int maxUpdateIterations;
        if (worldObj.isRemote && isActiveChunk) {
            maxUpdateIterations = 256;
        } else if (worldObj.isRemote) {
            maxUpdateIterations = 64;
        } else {
            maxUpdateIterations = 32;
        }

        val minPosX = xPosition * 16;
        val minPosZ = zPosition * 16;

        val worldCount = LumiWorldManager.lumiWorldCount();

        var remainingIterations = maxUpdateIterations;
        while (remainingIterations > 0) {
            if (queuedLightChecks >= (16 * 16 * 16))
                return;

            val chunkPosY = queuedLightChecks % 16;

            val minPosY = chunkPosY * 16;

            val subChunkPosX = (queuedLightChecks / 16) % 16;
            val subChunkPosZ = queuedLightChecks / (16 * 16);

            val posX = minPosX + subChunkPosX;
            val posZ = minPosZ + subChunkPosZ;

            for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                val posY = minPosY + subChunkPosY;
                val baseSubChunk = storageArrays[chunkPosY];
                if (baseSubChunk != null) {
                    for (var i = 0; i < worldCount; i++) {
                        val world = LumiWorldManager.getWorld(worldObj, i);
                        val chunk = world.lumi$wrap(thiz());

                        val blockBrightness = chunk.lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
                        val blockOpacity = chunk.lumi$getBlockOpacity(subChunkPosX, posY, subChunkPosZ);

                        renderUpdateCheck:
                        {
                            if (blockOpacity < 15)
                                break renderUpdateCheck;
                            if (blockBrightness > 0)
                                break renderUpdateCheck;

                            val lightValue = chunk.lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
                            if (lightValue == 0)
                                continue;

                            chunk.lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, 0);
                            worldObj.markBlockRangeForRenderUpdate(posX, posY, posZ, posX, posY, posZ);
                        }

                        performFullLightingUpdate(worldObj, posX, posY, posZ);
                        break;
                    }
                    continue;
                }

                if (subChunkPosX != 0 && subChunkPosX != 15)
                    continue;
                if (subChunkPosY != 0 && subChunkPosY != 15)
                    continue;
                if (subChunkPosZ != 0 && subChunkPosZ != 15)
                    continue;

                performFullLightingUpdate(worldObj, posX, posY, posZ);
            }

            queuedLightChecks++;
            remainingIterations--;
        }
    }

    private static void performFullLightingUpdate(World world, int posX, int posY, int posZ) {
        world.func_147451_t(posX, posY, posZ);
    }

    private Chunk thiz() {
        return (Chunk) (Object) this;
    }
}
