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

package com.falsepattern.lumina.internal.cache;


import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * ChunkCache, but small and efficient
 */
final class ChunkCacheCompact implements IBlockAccess {
    private int chunkX;
    private int chunkZ;
    private int dataSize;
    private Chunk[] chunkArray;

    public void init(LumiChunkRoot[] chunkArray, int dataSize, int chunkX, int chunkZ) {
        if (this.chunkArray == null || this.chunkArray.length != chunkArray.length)
            this.chunkArray = new Chunk[chunkArray.length];
        for (int i = 0; i < chunkArray.length; i++)
            this.chunkArray[i] = (Chunk) chunkArray[i];
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.dataSize = dataSize;
    }

    public Block getBlock(int posX, int posY, int posZ) {
        var block = Blocks.air;

        fetchBlock:
        {
            if (posY < 0 || posY >= 256) {
                break fetchBlock;
            }
            val cX = (posX >> 4) - this.chunkX;
            val cZ = (posZ >> 4) - this.chunkZ;

            if (cX < 0 || cX >= dataSize || cZ < 0 || cZ >= dataSize)
                break fetchBlock;

            val chunk = this.chunkArray[cZ * dataSize + cX];
            if (chunk == null)
                break fetchBlock;

            block = chunk.getBlock(posX & 15, posY, posZ & 15);
        }

        return block;
    }

    public TileEntity getTileEntity(int posX, int posY, int posZ) {
        val cX = (posX >> 4) - this.chunkX;
        val cZ = (posZ >> 4) - this.chunkZ;
        if (cX < 0 || cX >= dataSize || cZ < 0 || cZ >= dataSize)
            return null;

        val chunk = chunkArray[cZ * dataSize + cX];
        if (chunk == null)
            return null;
        return chunk.func_150806_e(posX & 15, posY, posZ & 15);
    }


    /**
     * Returns the block metadata at coords x,y,z
     */
    public int getBlockMetadata(int posX, int posY, int posZ) {
        if (posY < 0) {
            return 0;
        } else if (posY >= 256) {
            return 0;
        } else {
            int cX = (posX >> 4) - this.chunkX;
            int cZ = (posZ >> 4) - this.chunkZ;
            if (cX < 0 || cX >= dataSize || cZ < 0 || cZ >= dataSize)
                return 0;
            val chunk = chunkArray[cZ * dataSize + cX];
            if (chunk == null) return 0;
            return chunk.getBlockMetadata(posX & 15, posY, posZ & 15);
        }
    }

    /**
     * Is this block powering in the specified direction Args: x, y, z, direction
     */
    public int isBlockProvidingPowerTo(int x, int y, int z, int directionIn) {
        return this.getBlock(x, y, z).isProvidingStrongPower(this, x, y, z, directionIn);
    }

    /**
     * Returns true if the block at the specified coordinates is empty
     */
    public boolean isAirBlock(int x, int y, int z) {
        return this.getBlock(x, y, z).isAir(this, x, y, z);
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000)
            return _default;
        return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
    }

    @SideOnly(Side.CLIENT)
    public int getHeight() {
        throw new UnsupportedOperationException();
    }

    @SideOnly(Side.CLIENT)
    public boolean extendedLevelsInChunkCache() {
        throw new UnsupportedOperationException();
    }

    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int posX, int posY, int posZ, int minBlock) {
        throw new UnsupportedOperationException();
    }

    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        throw new UnsupportedOperationException();
    }
}
