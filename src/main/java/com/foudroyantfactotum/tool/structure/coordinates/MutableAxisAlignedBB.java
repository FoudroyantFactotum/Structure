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

import net.minecraft.util.AxisAlignedBB;

public class MutableAxisAlignedBB
{
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public MutableAxisAlignedBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static MutableAxisAlignedBB fromBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        return new MutableAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean intersectsWith(AxisAlignedBB other)
    {
        return other.maxX > minX && other.minX < maxX &&
                other.maxY > minY && other.minY < maxY &&
                other.maxZ > minZ && other.minZ < maxZ;
    }

    public AxisAlignedBB getAxisAlignedBB()
    {
        return AxisAlignedBB.fromBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString()
    {
        return "(" + minX +','+minY+','+minZ+"->"+maxX+','+maxY+','+maxZ+')';
    }
}
