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

import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
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
    public boolean isTerrainPopulated;
    @Shadow
    public boolean isLightPopulated;


    @Inject(method = "onChunkLoad",
            at = @At("RETURN"),
            require = 1)
    private void handleChunkLoad(CallbackInfo ci) {
        LightingHooks.handleChunkLoad(thiz());
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
        LightingHooks.handleSubChunkInit(thiz, chunkPosY);
    }

    /**
     * @author FalsePattern
     * @reason Blanking, this is not called anymore
     */
    @Overwrite
    public void relightBlock(int subChunkPosX, int posY, int subChunkPosZ) {
    }

    /**
     * @author FalsePattern
     * @reason Fix
     */
    @Overwrite
    public void generateSkylightMap() {
        LightingHooks.handleChunkInit(thiz());
    }

    /**
     * @reason Hook for calculating light updates only as needed.
     * @author Angeline
     */
    @Overwrite
    public int getSavedLightValue(EnumSkyBlock baseLightType, int subChunkPosX, int posY, int subChunkPosZ) {
        return LightingHooks.getCurrentLightValue(thiz(), baseLightType, subChunkPosX, posY, subChunkPosZ);
    }

    /**
     * @reason Hooks into checkLight() to check chunk lighting and returns immediately after, voiding the rest of the function.
     * @author Angeline
     */
    @Overwrite
    public void func_150809_p() {
        isTerrainPopulated = true;
        isLightPopulated = LightingHooks.isChunkFullyLit(thiz());
    }

    /**
     * @reason No longer used
     * @author Ven
     */
    @Overwrite
    private void recheckGaps(boolean onlyOne) {
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
    private int alwaysZeroLightOpacity(Block block, IBlockAccess worldBase, int posX, int posY, int posZ) {
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
                                                int posX,
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
        LightingHooks.updateLightingForBlock(thiz(), subChunkPosX, posY, subChunkPosZ);
    }

    @Redirect(method = "func_150807_a",
              at = @At(value = "NEW",
                       args = "class=net/minecraft/world/chunk/storage/ExtendedBlockStorage"),
              require = 1)
    private ExtendedBlockStorage createSubChunkWithInitializedSkyLight(int posY, boolean hasSky) {
        val subChunkBase = new ExtendedBlockStorage(posY, hasSky);
        LightingHooks.handleSubChunkInit(thiz(), subChunkBase);
        return subChunkBase;
    }

    @Inject(method = "resetRelightChecks",
            at = @At("RETURN"),
            require = 1)
    private void resetQueuedRandomLightUpdates(CallbackInfo cir) {
        LightingHooks.resetQueuedRandomLightUpdates(thiz());
    }

    /**
     * @author embeddedt
     * @reason optimize random light checks so they complete faster
     */
    @Overwrite
    public void enqueueRelightChecks() {
        LightingHooks.doRandomChunkLightingUpdates(thiz());
    }

    private Chunk thiz() {
        return (Chunk) (Object) this;
    }
}
