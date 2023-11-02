/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.chunk.LumiSubChunkRoot;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.falsepattern.lumina.internal.mixin.plugin.MixinPlugin.LUMI_ROOT_IMPL_MIXIN_PRIORITY;

@Unique
@Mixin(value = ExtendedBlockStorage.class, priority = LUMI_ROOT_IMPL_MIXIN_PRIORITY)
public abstract class LumiSubChunkRootImplMixin implements LumiSubChunkRoot {
    @Shadow
    private int yBase;

    @Override
    public @NotNull String lumi$subChunkRootID() {
        return "lumi_sub_chunk_root";
    }

    @Shadow
    public abstract Block getBlockByExtId(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    @Shadow
    public abstract int getExtBlockMetadata(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    @Override
    public @NotNull Block lumi$getBlock(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        return getBlockByExtId(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockMeta(int subChunkPosX, int subChunkPosY, int subChunkPosZ) {
        return getExtBlockMetadata(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$posY() {
        return yBase;
    }
}
