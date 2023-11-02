package com.falsepattern.lumina.api.storage;

import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

public interface LumiBlockStorage {
    @NotNull LumiBlockStorageRoot lumi$root();

    @NotNull String lumi$blockStorageID();

    @NotNull LumiWorld lumi$world();

    int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ);

    int lumi$getBrightness(int posX, int posY, int posZ);

    int lumi$getLightValue(int posX, int posY, int posZ);

    int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ);

    int lumi$getBlockLightValue(int posX, int posY, int posZ);

    int lumi$getSkyLightValue(int posX, int posY, int posZ);

    int lumi$getBlockBrightness(int posX, int posY, int posZ);

    int lumi$getBlockOpacity(int posX, int posY, int posZ);

    int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);

    int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ);
}
