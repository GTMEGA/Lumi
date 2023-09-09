/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.internal.mixin.hook.LightingHooks;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Unique
@Mixin(Chunk.class)
public abstract class LumiChunkRootImplMixin implements LumiChunkRoot {
    @Shadow
    private ExtendedBlockStorage[] storageArrays;
    @Shadow
    public World worldObj;
    @Shadow
    public boolean isModified;

    @Shadow
    public abstract int getTopFilledSegment();

    @Shadow
    public abstract Block getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract int getBlockMetadata(int subChunkPosX, int posY, int subChunkPosZ);

    @Shadow
    public abstract void setChunkModified();

    @Override
    public @NotNull String lumi$chunkRootID() {
        return "lumi_sub_chunk_root";
    }

    @Override
    public boolean lumi$isUpdating() {
        return worldObj.activeChunkSet.contains(thiz());
    }

    @Override
    public void lumi$markDirty() {
        setChunkModified();
    }

    @Override
    public boolean lumi$isDirty() {
        return isModified;
    }

    @Override
    public void lumi$prepareSubChunk(int chunkPosY) {
        chunkPosY &= 15;
        var subChunkBase = storageArrays[chunkPosY];

        if (subChunkBase == null) {
            val posY = chunkPosY << 4;
            subChunkBase = new ExtendedBlockStorage(posY, !worldObj.provider.hasNoSky);
            storageArrays[chunkPosY] = subChunkBase;
            LightingHooks.handleSubChunkInit(thiz(), subChunkBase);
        }

        lumi$markDirty();
    }

    @Override
    public boolean lumi$isSubChunkPrepared(int chunkPosY) {
        chunkPosY &= 15;
        val subChunk = storageArrays[chunkPosY];
        return subChunk != null;
    }

    @Override
    public int lumi$topPreparedSubChunkBasePosY() {
        return getTopFilledSegment();
    }

    @Override
    public @NotNull Block lumi$getBlock(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlock(subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ) {
        return getBlockMetadata(subChunkPosX, posY, subChunkPosZ);
    }

    private Chunk thiz() {
        return (Chunk) (Object) this;
    }
}
