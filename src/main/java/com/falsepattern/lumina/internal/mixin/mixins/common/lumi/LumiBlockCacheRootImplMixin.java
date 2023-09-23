/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.mixin.mixins.common.lumi;

import com.falsepattern.lumina.api.storage.LumiBlockCacheRoot;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.falsepattern.lumina.internal.mixin.plugin.MixinPlugin.LUMI_ROOT_IMPL_MIXIN_PRIORITY;

@Unique
@Mixin(value = ChunkCache.class, priority = LUMI_ROOT_IMPL_MIXIN_PRIORITY)
public abstract class LumiBlockCacheRootImplMixin implements IBlockAccess, LumiBlockCacheRoot {
    // region Shadow
    @Shadow
    private World worldObj;

    @Shadow
    public abstract Block getBlock(int posX, int posY, int posZ);

    @Shadow
    public abstract boolean isAirBlock(int posX, int posY, int posZ);

    @Shadow
    public abstract int getBlockMetadata(int posX, int posY, int posZ);

    @Shadow
    public abstract TileEntity getTileEntity(int posX, int posY, int posZ);
    // endregion

    // region Block Cache Root
    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "lumi_block_cache_root";
    }
    // endregion

    // region Block Storage Root
    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "lumi_block_cache_root";
    }

    @Override
    public boolean lumi$isClientSide() {
        return worldObj.isRemote;
    }

    @Override
    public boolean lumi$hasSky() {
        return !worldObj.provider.hasNoSky;
    }

    @Override
    public @NotNull Block lumi$getBlock(int posX, int posY, int posZ) {
        return getBlock(posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        return getBlockMetadata(posX, posY, posZ);
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        return isAirBlock(posX, posY, posZ);
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        return getTileEntity(posX, posY, posZ);
    }
    // endregion
}
