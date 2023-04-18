/*
 * Copyright (C) 2023 FalsePattern
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

package com.falsepattern.lumina.api;

import com.falsepattern.lumina.api.phosphor.ILightingEngineProvider;

import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;

/**
 * Chunk placeholder for implementation.
 */
public interface ILumiChunk extends ILightingEngineProvider {

    /**
     * Sets the light value at the coordinate. If enumskyblock is set to sky it sets it in the skylightmap and if its a
     * block then into the blocklightmap. Args enumSkyBlock, x, y, z, lightValue
     */
    void setLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z, int lightValue);
    int getSavedLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z);

    boolean canBlockSeeTheSky(int x, int y, int z);

    ILumiEBS getLumiEBS(int arrayIndex);

    ILumiWorld world();


    //IChunkLighting
    default int getCachedLightFor(EnumSkyBlock type, int xIn, int yIn, int zIn) {
        int i = xIn & 15;
        int j = yIn;
        int k = zIn & 15;

        ILumiEBS extendedblockstorage = this.getLumiEBS(j >> 4);

        if (extendedblockstorage == null) {
            if (this.canBlockSeeTheSky(i, j, k)) {
                return type.defaultLightValue;
            } else {
                return 0;
            }
        } else if (type == EnumSkyBlock.Sky) {
            if (this.world().hasNoSky()) {
                return 0;
            } else {
                return extendedblockstorage.getExtSkylightValue(i, j & 15, k);
            }
        } else {
            if (type == EnumSkyBlock.Block) {
                return extendedblockstorage.getExtBlocklightValue(i, j & 15, k);
            } else {
                return type.defaultLightValue;
            }
        }
    }

    //IChunkLightingData
    short[] getNeighborLightChecks();

    void setNeighborLightChecks(short[] data);

    boolean isLightInitialized();

    void setLightInitialized(boolean val);

    void setSkylightUpdatedPublic();

    void isLightPopulated(boolean state);

    void isGapLightingUpdated(boolean b);

    //Proxy these to carrier chunk
    void setHeightValue(int x, int z, int val);
    int getHeightValue(int x, int z);
    int x();
    int z();
    void setChunkModified();
    Block getBlock(int x, int y, int z);

    int heightMapMinimum();

    void heightMapMinimum(int min);

    boolean[] updateSkylightColumns();

    void isModified(boolean b);
}
