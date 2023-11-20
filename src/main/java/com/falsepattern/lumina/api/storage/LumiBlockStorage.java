/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

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
