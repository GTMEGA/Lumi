package com.falsepattern.lumina.api.storage;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LumiBlockStorageRoot {
    @NotNull String lumi$blockStorageRootID();

    boolean lumi$isClientSide();

    boolean lumi$hasSky();

    @NotNull Block lumi$getBlock(int posX, int posY, int posZ);

    int lumi$getBlockMeta(int posX, int posY, int posZ);

    boolean lumi$isAirBlock(int posX, int posY, int posZ);

    @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ);
}
