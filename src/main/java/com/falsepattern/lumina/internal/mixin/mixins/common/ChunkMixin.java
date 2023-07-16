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

import com.falsepattern.lumina.internal.lighting.LightingHooks;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
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
    @Shadow
    public World worldObj;
    @Shadow
    public boolean isTerrainPopulated;
    @Shadow
    public boolean isLightPopulated;

    @Inject(method = "getBlockLightValue",
            at = @At("HEAD"),
            require = 1)
    private void processLightUpdatesOnSubtract(int subChunkPosX,
                                               int posY,
                                               int subChunkPosZ,
                                               int subtract,
                                               CallbackInfoReturnable<Integer> cir) {
        LightingHooks.processLightUpdates(worldObj);
    }

    @Inject(method = "onChunkLoad",
            at = @At("RETURN"),
            require = 1)
    private void scheduleRelightOnLoad(CallbackInfo ci) {
        LightingHooks.scheduleRelightChecksForChunkBoundaries(worldObj, thiz());
    }

    @Redirect(method = "setLightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"),
              require = 1)
    private void initSubChunkSkyLightOnSet(Chunk thiz,
                                           EnumSkyBlock baseLightType,
                                           int subChunkPosX,
                                           int posY,
                                           int subChunkPosZ,
                                           int value) {
        val chunkPosY = posY / 16;
        LightingHooks.initSkyLightForSubChunk(worldObj, thiz, chunkPosY);
    }

    /**
     * @author FalsePattern
     * @reason Blanking, this is not called anymore
     */
    @Overwrite
    private void relightBlock(int subChunkPosX, int posY, int subChunkPosZ) {
    }

    /**
     * @author FalsePattern
     * @reason Fix
     */
    @Overwrite
    public void generateSkylightMap() {
        LightingHooks.initLightingForChunk(worldObj, thiz());
    }

    /**
     * @reason Hook for calculating light updates only as needed.
     * @author Angeline
     */
    @Overwrite
    public int getSavedLightValue(EnumSkyBlock baseLightType, int subChunkPosX, int posY, int subChunkPosZ) {
        return LightingHooks.getMaxLightValue(worldObj, thiz(), baseLightType, subChunkPosX, posY, subChunkPosZ);
    }

    /**
     * @reason Hooks into checkLight() to check chunk lighting and returns immediately after, voiding the rest of the function.
     * @author Angeline
     */
    @Overwrite
    public void func_150809_p() {
        isTerrainPopulated = true;
        isLightPopulated = LightingHooks.doesChunkHaveLighting(worldObj, thiz());
    }

    /**
     * @reason No longer used
     * @author Ven
     */
    @Overwrite
    private void recheckGaps(boolean onlyOne) {
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
    private void skipSkyLightPropagation(Chunk thiz, int posX, int posZ) {
    }

    @Redirect(method = "func_150807_a(IIILnet/minecraft/block/Block;I)Z",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/Chunk;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"),
              require = 2)
    private int alwaysZeroLightValue(Chunk thiz, EnumSkyBlock baseLightType, int posX, int posY, int posZ) {
        return 0;
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;getLightOpacity(Lnet/minecraft/world/IBlockAccess;III)I"),
              require = 2)
    private int alwaysZeroLightOpacity(Block block, IBlockAccess baseWorld, int posX, int posY, int posZ) {
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
                                                int basePosY,
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
        LightingHooks.updateSkyLightForBlock(worldObj, thiz(), subChunkPosX, basePosY, subChunkPosZ);
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "NEW",
                       args = "class=net/minecraft/world/chunk/storage/ExtendedBlockStorage"),
              require = 1)
    private ExtendedBlockStorage createSubChunkWithInitializedSkyLight(int posY, boolean hasSky) {
        val baseSubChunk = new ExtendedBlockStorage(posY, hasSky);
        LightingHooks.initSkyLightForSubChunk(worldObj, thiz(), baseSubChunk);
        return baseSubChunk;
    }

    /**
     * @author embeddedt
     * @reason optimize random light checks so they complete faster
     */
    @Overwrite
    public void enqueueRelightChecks() {
        LightingHooks.randomLightUpdates(worldObj, thiz());
    }

    private Chunk thiz() {
        return (Chunk) (Object) this;
    }
}
