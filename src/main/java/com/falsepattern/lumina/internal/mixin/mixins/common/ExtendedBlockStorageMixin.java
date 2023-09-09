/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common;

import lombok.val;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExtendedBlockStorage.class)
public abstract class ExtendedBlockStorageMixin {
    @Shadow
    private int blockRefCount;
    @Shadow
    private NibbleArray blocklightArray;
    @Shadow
    private NibbleArray skylightArray;

    @Unique
    private boolean lumi$isDirty;
    @Unique
    private boolean lumi$isTrivial;

    @Inject(method = "<init>*",
            at = @At(value = "RETURN",
                     target = "Ljava/util/Random;nextInt(I)I"),
            require = 1)
    private void lumiSubChunkInit(int posY, boolean hasSky, CallbackInfo ci) {
        this.lumi$isDirty = true;
        this.lumi$isTrivial = false;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtSkylightValue(int posX, int posY, int posZ, int lightValue) {
        skylightArray.set(posX, posY, posZ, lightValue);
        lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setExtBlocklightValue(int posX, int posY, int posZ, int lightValue) {
        blocklightArray.set(posX, posY, posZ, lightValue);
        lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setBlocklightArray(NibbleArray blockLightArray) {
        this.blocklightArray = blockLightArray;
        lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Reset lightRefCount on call
     */
    @Overwrite
    public void setSkylightArray(NibbleArray skyLightArray) {
        this.skylightArray = skyLightArray;
        lumi$isDirty = true;
    }

    /**
     * @author Angeline
     * @reason Send light data to clients when lighting is non-trivial
     */
    @Overwrite
    public boolean isEmpty() {
        if (blockRefCount != 0)
            return false;

        if (lumi$isDirty) {
            val blockLightEqual = lumi$checkLightArrayEqual(blocklightArray, EnumSkyBlock.Block);
            val skyLightEqual = lumi$checkLightArrayEqual(skylightArray, EnumSkyBlock.Sky);
            lumi$isTrivial = blockLightEqual && skyLightEqual;
            lumi$isDirty = false;
        }

        return lumi$isTrivial;
    }

    @Unique
    private boolean lumi$checkLightArrayEqual(NibbleArray storage, EnumSkyBlock baseLightType) {
        if (storage == null)
            return true;

        val expectedValue = (byte) baseLightType.defaultLightValue;
        val data = storage.data;
        for (val value : data)
            if (value != expectedValue)
                return false;

        return true;
    }
}
