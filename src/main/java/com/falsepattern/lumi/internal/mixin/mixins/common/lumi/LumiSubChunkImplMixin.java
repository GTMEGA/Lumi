/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
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
 *
 */

package com.falsepattern.lumi.internal.mixin.mixins.common.lumi;

import com.falsepattern.chunk.api.ArrayUtil;
import com.falsepattern.lumi.api.chunk.LumiSubChunk;
import com.falsepattern.lumi.api.chunk.LumiSubChunkRoot;
import com.falsepattern.lumi.api.lighting.LightType;
import com.falsepattern.lumi.internal.ArrayHelper;
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

import net.minecraftforge.common.util.Constants;

import java.nio.ByteBuffer;

import static com.falsepattern.lumi.api.init.LumiExtendedBlockStorageInitHook.LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_INFO;
import static com.falsepattern.lumi.api.init.LumiExtendedBlockStorageInitHook.LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_METHOD;

@Unique
@Mixin(ExtendedBlockStorage.class)
public abstract class LumiSubChunkImplMixin implements LumiSubChunk {
    @Nullable
    @Shadow
    private NibbleArray blocklightArray;
    @Shadow
    @Nullable
    private NibbleArray skylightArray;

    private LumiSubChunkRoot lumi$root;


    @Inject(method = LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_METHOD,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @SuppressWarnings("CastToIncompatibleInterface")
    @Dynamic(LUMI_EXTENDED_BLOCK_STORAGE_INIT_HOOK_INFO)
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
        if (blocklightArray != null)
            output.setByteArray(BLOCK_LIGHT_NBT_TAG_NAME, blocklightArray.data);
        if (skylightArray != null)
            output.setByteArray(SKY_LIGHT_NBT_TAG_NAME, skylightArray.data);
    }

    @Override
    public void lumi$readFromNBT(@NotNull NBTTagCompound input) {
        if (input.hasKey(BLOCK_LIGHT_NBT_TAG_NAME, Constants.NBT.TAG_BYTE_ARRAY)) {
            val blockLightBytes = input.getByteArray(BLOCK_LIGHT_NBT_TAG_NAME);
            if (ArrayHelper.isZero(blockLightBytes)) {
                blocklightArray = null;
            } else if (blocklightArray == null) {
                blocklightArray = new NibbleArray(blockLightBytes, 4);
            } else {
                System.arraycopy(blockLightBytes, 0, blocklightArray.data, 0, 2048);
            }
        } else {
            blocklightArray = null;
        }

        if (input.hasKey(SKY_LIGHT_NBT_TAG_NAME, Constants.NBT.TAG_BYTE_ARRAY)) {
            val skyLightBytes = input.getByteArray(SKY_LIGHT_NBT_TAG_NAME);
            if (ArrayHelper.isZero(skyLightBytes)) {
                skylightArray = null;
            } else if (skylightArray == null) {
                skylightArray = new NibbleArray(skyLightBytes, 4);
            } else {
                System.arraycopy(skyLightBytes, 0, skylightArray.data, 0, 2048);
            }
        } else {
            skylightArray = null;
        }
    }

    @Override
    public void lumi$cloneFrom(LumiSubChunk from) {
        blocklightArray = ArrayUtil.copyArray(from.lumi$getBlockLightArray(), lumi$getBlockLightArray());
        skylightArray = ArrayUtil.copyArray(from.lumi$getSkyLightArray(), lumi$getSkyLightArray());
    }

    @Override
    public void lumi$writeToPacket(@NotNull ByteBuffer output) {
        if (blocklightArray != null && ArrayHelper.isZero(blocklightArray.data))
            blocklightArray = null;
        if (skylightArray != null && ArrayHelper.isZero(skylightArray.data))
            skylightArray = null;

        byte flag = (byte) ((blocklightArray != null ? 1 : 0) | (skylightArray != null ? 2 : 0));
        output.put(flag);
        if (blocklightArray != null)
            output.put(blocklightArray.data);
        if (skylightArray != null)
            output.put(skylightArray.data);
    }

    @Override
    public void lumi$readFromPacket(@NotNull ByteBuffer input) {
        byte flag = input.get();
        boolean doBlock = (flag & 1) != 0;
        boolean doSky = (flag & 2) != 0;

        if (doBlock) {
            if (blocklightArray == null) {
                blocklightArray = new NibbleArray(4096, 4);
            }
            input.get(blocklightArray.data);
        } else {
            blocklightArray = null;
        }
        if (doSky) {
            if (skylightArray == null) {
                skylightArray = new NibbleArray(4096, 4);
            }
            input.get(skylightArray.data);
        } else {
            skylightArray = null;
        }
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
        if (blocklightArray == null) {
            if (lightValue == 0)
                return;

            blocklightArray = new NibbleArray(4096, 4);
        }

        blocklightArray.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
    }

    @Override
    public int lumi$getBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        if (blocklightArray == null)
            return 0;

        return blocklightArray.get(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void lumi$setSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue) {
        if (skylightArray == null) {
            if (lightValue == 0)
                return;
            skylightArray = new NibbleArray(4096, 4);
        }

        skylightArray.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
    }

    @Override
    public int lumi$getSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        if (skylightArray == null)
            return 0;

        return skylightArray.get(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public NibbleArray lumi$getBlockLightArray() {
        return blocklightArray;
    }

    @Override
    public NibbleArray lumi$getSkyLightArray() {
        return skylightArray;
    }
}
