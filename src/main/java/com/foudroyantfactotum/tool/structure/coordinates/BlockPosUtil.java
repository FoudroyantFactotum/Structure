/*
 * Copyright (c) 2016 Foudroyant Factotum
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.foudroyantfactotum.tool.structure.coordinates;

import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;

public final class BlockPosUtil
{
    public static final int BLOCKPOS_MASK = 0x00FFFFFF;
    public static final int BLOCKPOS_BITLEN = 24;

    public static BlockPos of(BlockPos pos, EnumFacing f) { return new BlockPos(pos.getX() + f.getFrontOffsetX(), pos.getY() + f.getFrontOffsetY(), pos.getZ() + f.getFrontOffsetZ()); }
    public static BlockPos of(int x, int y, int z)
    {
        return new BlockPos(x, y, z);
    }

    public static int toInt(BlockPos pos) { return toInt(pos.getX(), pos.getY(), pos.getZ()); }
    public static int toInt(int x, int y, int z)
    {
        return  (((byte) x) << 16) +
                (((byte) y) << 8)  +
                 ((byte) z);
    }

    public static BlockPos fromInt(int val)
    {
        return new BlockPos(
                (byte) (val >> 16),
                (byte) (val >> 8),
                (byte)  val
        );
    }

    public static void mutSetX(MutableBlockPos pos, int x)
    {
        pos.set(x, pos.getY(), pos.getZ());
    }

    public static void mutSetY(MutableBlockPos pos, int y)
    {
        pos.set(pos.getX(), y, pos.getZ());
    }

    public static void mutSetZ(MutableBlockPos pos, int z)
    {
        pos.set(pos.getX(), pos.getY(), z);
    }

    public static MutableBlockPos newMutBlockPos(BlockPos pos)
    {
        return new MutableBlockPos().set(pos.getX(), pos.getY(), pos.getZ());
    }

    public static MutableBlockPos mutOffset(MutableBlockPos pos, EnumFacing facing)
    {
        return pos.set(
                facing.getFrontOffsetX() + pos.getX(),
                facing.getFrontOffsetY() + pos.getY(),
                facing.getFrontOffsetZ() + pos.getZ()
        );
    }
}

