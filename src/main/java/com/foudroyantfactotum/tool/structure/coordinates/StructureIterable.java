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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is used as the basis of all iteration through out the structure. It allows the iteration over all the
 * values (designed in this case for (x,y,z))
 */
public class StructureIterable implements Iterator<MutableBlockPos>
{
    private int layerNo, depthNo, rowNo;
    private int layerNoU, depthNoU, rowNoU;
    private int layerNoL, depthNoL, rowNoL;

    //because private constructor
    private MutableBlockPos pos = (MutableBlockPos) BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, BlockPos.ORIGIN).iterator().next();

    private boolean hasNext;

    private StructureIterable()
    {
        this(0,0,0);
    }

    /**
     * (0,0,0) - (x,y,z)
     * @param x upper x-coord
     * @param y upper y-coord
     * @param z upper z-coord
     */
    public StructureIterable(int x, int y, int z)
    {
        this(0,0,0, x,y,z);
    }

    /**
     * (xl,yl,zl) - (xu,yu,zu)
     * @param xl lower x-coord
     * @param yl lower y-coord
     * @param zl lower z-coord
     * @param xu upper x-coord
     * @param yu upper y-coord
     * @param zu upper z-coord
     */
    public StructureIterable(int xl, int yl, int zl, int xu, int yu, int zu)
    {
        rowNoL = xl-1; layerNoL = yl; depthNoL = zl;
        rowNoU = xu; layerNoU = yu; depthNoU = zu;

        rowNo = rowNoL; depthNo = depthNoL; layerNo = layerNoL;

        hasNext = true;

        shiftReadHead();
    }

    private void shiftReadHead()
    {
        while (layerNo < layerNoU)
        {
            while (depthNo < depthNoU)
            {
                if (++rowNo < rowNoU)
                {
                    return;
                }

                rowNo = rowNoL;
                depthNo++;
            }

            depthNo = depthNoL;
            layerNo++;
        }

        hasNext = false;
    }

    @Override
    public boolean hasNext()
    {
        return hasNext;
    }

    @Override
    public MutableBlockPos next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }

        pos.set(rowNo, layerNo, depthNo);

        shiftReadHead();

        return pos;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
