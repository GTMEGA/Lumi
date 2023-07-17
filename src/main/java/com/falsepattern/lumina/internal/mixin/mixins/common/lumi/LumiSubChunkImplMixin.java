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

import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunkRoot;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.internal.mixin.mixins.common.init.LumiSubChunkBaseInitImplMixin;
import lombok.val;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

import static com.falsepattern.lumina.api.init.LumiSubChunkBaseInit.LUMI_SUB_CHUNK_BASE_INIT_METHOD_REFERENCE;

@Unique
@Mixin(ExtendedBlockStorage.class)
public abstract class LumiSubChunkImplMixin implements LumiSubChunk {
    @Shadow
    private NibbleArray blocklightArray;
    @Shadow
    @Nullable
    private NibbleArray skylightArray;

    private LumiSubChunkRoot lumi$root;


    @Inject(method = LUMI_SUB_CHUNK_BASE_INIT_METHOD_REFERENCE,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @SuppressWarnings({"ReferenceToMixin", "CastToIncompatibleInterface"})
    @Dynamic(mixin = LumiSubChunkBaseInitImplMixin.class)
    private void lumiSubChunkInit(CallbackInfo ci) {
        this.lumi$root = (LumiSubChunkRoot) this;
    }

    @Override
    public @NotNull LumiSubChunkRoot lumi$root() {
        return lumi$root;
    }

    @Override
    public @NotNull String lumi$subChunkID() {
        return "lumi_sub_chunk";
    }

    @Override
    public void lumi$writeToNBT(@NotNull NBTTagCompound output) {
        output.setByteArray(BLOCK_LIGHT_NBT_TAG_NAME, blocklightArray.data);
        if (skylightArray != null)
            output.setByteArray(SKY_LIGHT_NBT_TAG_NAME, skylightArray.data);
    }

    @Override
    public void lumi$readFromNBT(@NotNull NBTTagCompound input) {
        if (input.hasKey(BLOCK_LIGHT_NBT_TAG_NAME, 7)) {
            val blockLightBytes = input.getByteArray(BLOCK_LIGHT_NBT_TAG_NAME);
            if (blockLightBytes.length == 2048)
                System.arraycopy(blockLightBytes, 0, blocklightArray.data, 0, 2048);
        }

        if (skylightArray != null && input.hasKey(SKY_LIGHT_NBT_TAG_NAME, 7)) {
            val skyLightBytes = input.getByteArray(SKY_LIGHT_NBT_TAG_NAME);
            if (skyLightBytes.length == 2048)
                System.arraycopy(skyLightBytes, 0, skylightArray.data, 0, 2048);
        }
    }

    @Override
    public void lumi$writeToPacket(@NotNull ByteBuffer output) {
        output.put(blocklightArray.data);
        if (skylightArray != null)
            output.put(skylightArray.data);
    }

    @Override
    public void lumi$readFromPacket(@NotNull ByteBuffer input) {
        input.get(blocklightArray.data);
        if (skylightArray != null)
            input.get(skylightArray.data);
    }

    @Override
    public void lumi$setLightValue(@NotNull LightType lightType,
                                   int subChunkPosX,
                                   int subChunkPosY,
                                   int subChunkPosZ,
                                   int lightValue) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                break;
            case SKY_LIGHT_TYPE:
                lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                break;
            default:
                break;
        }
    }

    @Override
    public int lumi$getLightValue(@NotNull LightType lightType,
                                  int subChunkPosX,
                                  int subChunkPosY,
                                  int subChunkPosZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
            default:
                return lightType.defaultLightValue();
        }
    }

    @Override
    public void lumi$setBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue) {
        blocklightArray.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
    }

    @Override
    public int lumi$getBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        return blocklightArray.get(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void lumi$setSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue) {
        if (skylightArray == null)
            return;
        skylightArray.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
    }

    @Override
    public int lumi$getSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        if (skylightArray != null)
            return skylightArray.get(subChunkPosX, subChunkPosY, subChunkPosZ);
        return 0;
    }
}
