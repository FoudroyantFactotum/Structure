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

package mod.steamnsteel.utility;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.Map;

import static java.lang.Math.PI;

public enum Orientation
{
    SOUTH(0x0),
    WEST(0x1),
    NORTH(0x2),
    EAST(0x3);

    // Reverse-lookup map for getting an orientation from the metadata code
    private static final ImmutableMap<Integer, Orientation> LOOKUP;

    static
    {
        final Map<Integer, Orientation> lookup = Maps.newHashMapWithExpectedSize(Orientation.values().length);
        for (final Orientation o : Orientation.values())
            lookup.put(o.flag, o);
        LOOKUP = ImmutableMap.copyOf(lookup);
    }

    private final int flag;
    private static final PropertyOrientation propertyOrientation = new PropertyOrientation("orientation", Arrays.asList(values()));

    Orientation(int flag)
    {
        this.flag = flag;
    }

    public static Orientation getdecodedOrientation(int encoded)
    {
        return NORTH; //todo fix north bug
        //return LOOKUP.get(BlockDirectional.getDirection(encoded));
    }

    public IBlockState setBlockState(IBlockState state)
    {
        return state.withProperty(propertyOrientation, this);
    }

    public static Orientation getdecodedOrientation(IBlockState state)
    {
        Comparable comp = state.getValue(propertyOrientation);

        if (comp.compareTo(SOUTH) == 0)
            return SOUTH;
        else if (comp.compareTo(WEST) == 0)
            return WEST;
        else if (comp.compareTo(NORTH) == 0)
            return NORTH;
        else
            return NORTH;
    }

    public EnumFacing getEnumFacing()
    {
        switch (this)
        {
            case SOUTH:
                return EnumFacing.SOUTH;
            case EAST:
                return EnumFacing.EAST;
            case WEST:
                return EnumFacing.WEST;
            default:
                return EnumFacing.NORTH;
        }
    }

    public int encode()
    {
        return flag;
    }

    public double getRotationValue()
    {
        return PI * (1.0 - ordinal() / 2.0);
    }
}
