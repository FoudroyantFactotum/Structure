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
package mod.steamnsteel.structure.coordinates;

import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureNeighbours;
import mod.steamnsteel.utility.Orientation;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static mod.steamnsteel.structure.coordinates.TransformLAG.fromMasterlocalToGlobal;

public class StructureBlockIterator implements Iterator<StructureBlockCoord>
{
    private final ImmutableTriple<Integer, Integer, Integer> worldLocation;

    private final Orientation orientation;
    private final boolean mirrored;

    private final ImmutableTriple<Integer,Integer,Integer> mps;

    private final StructureDefinition sd;
    private final ImmutableTriple<Integer, Integer, Integer> sbLayoutSize;
    private final BitSet sbLayout;

    private int rhx = -1;
    private int rhy = 0;
    private int rhz = 0;

    private boolean readHeadEnd = false;

    private final int xzSize;

    public StructureBlockIterator(StructureDefinition sd, ImmutableTriple<Integer, Integer, Integer> worldLocation, Orientation orientation, Boolean mirrored)
    {
        this.worldLocation = worldLocation;

        this.orientation = orientation;
        this.mirrored = mirrored;

        this.sd = sd;
        sbLayoutSize = sd.getBlockBounds();
        sbLayout = sd.getBlockLayout();
        mps = sd.getMasterLocation();

        xzSize = sbLayoutSize.getLeft()*sbLayoutSize.getRight();

        shiftReadHead();
    }

    public Orientation getOrientation()
    {
        return orientation;
    }

    public StructureDefinition getStructureDefinition()
    {
        return sd;
    }

    public ImmutableTriple<Integer, Integer, Integer> getWorldLocation()
    {
        return worldLocation;
    }

    public void cleanIterator()
    {
        rhx = -1;
        rhy = 0;
        rhz = 0;

        readHeadEnd = false;

        shiftReadHead();
    }

    private void shiftReadHead()
    {
        while (rhy < sbLayoutSize.getMiddle())
        {
            while (rhz < sbLayoutSize.getRight())
            {
                while (++rhx < sbLayoutSize.getLeft())
                    if (sbLayout.get(rhx +
                            (sbLayoutSize.getLeft()*rhz) +
                            (xzSize*rhy)))
                        return;

                rhx = -1;
                ++rhz;
            }

            rhz = 0;
            ++rhy;
        }

        readHeadEnd = true;
    }

    private byte getNeighbors()
    {
        byte neighbours = 0;

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
        {
            final int fdx = rhx+d.offsetX;
            final int fdy = rhy+d.offsetY;
            final int fdz = rhz+d.offsetZ;

            if (fdy > -1 && fdy < sbLayoutSize.getMiddle())
                if (fdz > -1 && fdz < sbLayoutSize.getRight())
                    if (fdx > -1 && fdx < sbLayoutSize.getLeft())
                        if (sbLayout.get(fdx +
                                        (sbLayoutSize.getLeft()*rhz) +
                                        (xzSize*rhy)
                        ))
                            neighbours |= d.flag;
        }

        return neighbours;
    }

    private boolean isReadHeadOnMaster()
    {
        return  rhx == mps.getLeft() &&
                rhy == mps.getMiddle() &&
                rhz == mps.getRight();
    }

    @Override
    public boolean hasNext()
    {
        return !readHeadEnd;
    }

    @Override
    public StructureBlockCoord next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        final int fx = rhx - mps.getLeft();
        final int fy = rhy - mps.getMiddle();
        final int fz = rhz - mps.getRight();

        final StructureBlockCoord sb = new StructureBlockCoord(
                fx, fy, fz,
                isReadHeadOnMaster(),
                new StructureNeighbours(getNeighbors()),
                worldLocation,
                fromMasterlocalToGlobal(fx, fy, fz, worldLocation, orientation, mirrored, sd),
                orientation, mirrored
        );

        shiftReadHead();

        return sb;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
