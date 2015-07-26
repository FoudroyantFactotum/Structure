/*
 * Copyright (c) 2014 Rosie Alexander and Scott Killen.
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
package mod.steamnsteel.structure.registry.MetaCorrecter;

import mod.steamnsteel.structure.registry.IStructurePatternMetaCorrecter;
import mod.steamnsteel.utility.Orientation;
import net.minecraftforge.common.util.ForgeDirection;

public class SMCStairs implements IStructurePatternMetaCorrecter
{
    private static final byte NORTH = 0x3;
    private static final byte SOUTH = 0x2;
    private static final byte EAST  = 0x0;
    private static final byte WEST  = 0x1;

    private static final byte FLIP  = 0x4;

    @Override
    public int correctMeta(byte meta, Orientation o, boolean isMirrored)
    {
        ForgeDirection d = doMirror(getOrientationFromMeta(meta & 3), isMirrored);
        int result;

        switch (o)
        {
            case SOUTH:
                result = getMetaFromOrientation(d.getRotation(ForgeDirection.DOWN).getRotation(ForgeDirection.DOWN));
                break;
            case WEST:
                result = getMetaFromOrientation(d.getRotation(ForgeDirection.DOWN));
                break;
            case EAST:
                result = getMetaFromOrientation(d.getRotation(ForgeDirection.UP));
                break;
            case NORTH:
            default:
                result = getMetaFromOrientation(d);
        }
        return result | (isFliped(meta)?FLIP:0);
    }

    private static ForgeDirection getOrientationFromMeta(int meta)
    {
        switch (meta)
        {
            case EAST:
                return ForgeDirection.EAST;
            case WEST:
                return ForgeDirection.WEST;
            case SOUTH:
                return ForgeDirection.SOUTH;
            case NORTH:
            default:
                return ForgeDirection.NORTH;
        }
    }

    private static byte getMetaFromOrientation(ForgeDirection d)
    {
        switch (d)
        {
            case SOUTH:
                return SOUTH;
            case WEST:
                return WEST;
            case EAST:
                return EAST;
            case NORTH:
            default:
                return NORTH;
        }
    }

    private static boolean isFliped(byte meta)
    {
        return (meta & FLIP) != 0;
    }

    private static ForgeDirection doMirror(ForgeDirection d, boolean isMirrored)
    {
        if (isMirrored && (d == ForgeDirection.NORTH || d == ForgeDirection.SOUTH)) return d.getOpposite();

        return d;
    }

}
