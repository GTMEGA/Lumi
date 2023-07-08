package com.falsepattern.lumina.internal.mixin.mixins.common;

import lombok.val;
import lombok.var;
import net.minecraft.world.chunk.NibbleArray;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NibbleArray.class)
public abstract class NibbleArrayMixin {
    @Final
    @Shadow
    public byte[] data;
    @Final
    @Shadow
    private int depthBits;
    @Final
    @Shadow
    private int depthBitsPlusFour;

    /**
     * @author Ven
     * @reason Enforced index range
     */
    @Overwrite
    public int get(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        subChunkPosX &= 15;
        subChunkPosY &= 15;
        subChunkPosZ &= 15;

        val key = subChunkPosY << depthBitsPlusFour |
                  subChunkPosZ << depthBits |
                  subChunkPosX;

        val index = key >> 1;
        val parity = key & 1;

        var value = data[index];
        if (parity == 1)
            value >>= 4;

        return value & 0xF;
    }

    /**
     * @author Ven
     * @reason Enforced index range
     */
    @Overwrite
    public void set(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int value) {
        subChunkPosX &= 15;
        subChunkPosY &= 15;
        subChunkPosZ &= 15;

        value &= 0x0F;

        val key = subChunkPosY << depthBitsPlusFour |
                  subChunkPosZ << depthBits |
                  subChunkPosX;

        val index = key >> 1;
        val parity = key & 1;

        var currentValue = data[index];
        if (parity == 0) {
            currentValue &= 0xF0;
            value |= currentValue;
        } else {
            value <<= 4;
            currentValue &= 0x0F;
            value |= currentValue;
        }

        data[index] = (byte) value;
    }
}