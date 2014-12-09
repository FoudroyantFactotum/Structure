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
package mod.steamnsteel.utility.structure;

import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class StructureBlockIterator implements Iterator<WorldBlockCoord>
{
    private final Vec3 worldLocation;
    private Vec3 block;

    private final ImmutableTriple<Integer,Integer,Integer> maxSize;
    private final ImmutableTriple<Integer,Integer,Integer> minSize;

    private final Orientation orienetation;
    private final boolean mirrored;

    private static final int rotationMatrix[][][] = {
            {{-1, 0}, {0, -1}}, //south
            {{0, 1}, {-1, 0}}, //west
            {{1, 0}, {0, 1}}, // north
            {{0, -1}, {1, 0}}, //east
    };

    public StructureBlockIterator(StructurePattern sp, Vec3 worldLocation, Orientation orientation, Boolean mirrored)
    {
        this.worldLocation = worldLocation;
        final Vec3 spSize = sp.getSize();
        this.orienetation = orientation;
        this.mirrored = mirrored;

        maxSize = ImmutableTriple.of(
                (int)spSize.xCoord-1,
                (int)spSize.yCoord-1,
                (int)spSize.zCoord-1
        );

        minSize = ImmutableTriple.of(
                (int)0,
                (int)0,
                (int)0
        );

        this.block = Vec3.createVectorHelper(
                (int)maxSize.getLeft(),
                (int)maxSize.getMiddle(),
                (int)maxSize.getRight());

        if (mirrored) block.zCoord = minSize.getRight();
    }

    public void cleanIterator()
    {
        block = Vec3.createVectorHelper(
                (int)maxSize.getLeft(),
                (int)maxSize.getMiddle(),
                (int)maxSize.getRight());
    }

    @Override
    public boolean hasNext()
    {
        return 0 <= block.yCoord;
    }

    @Override
    public WorldBlockCoord next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        final int d = orienetation.ordinal();

        final double xF = rotationMatrix[d][0][0] * block.xCoord + rotationMatrix[d][0][1] * block.zCoord;
        final double zF = rotationMatrix[d][1][0] * block.xCoord + rotationMatrix[d][1][1] * block.zCoord;
        final double yF = block.yCoord;

        final int[] locPos = {(int)block.xCoord, (int)block.yCoord, (int)(mirrored ? maxSize.getRight()-block.zCoord : block.zCoord)};

        --block.xCoord;
        if (block.xCoord < minSize.getLeft()) {
            block.xCoord = maxSize.getLeft();
            block.zCoord += (mirrored)?1:-1;
        }

        if (block.zCoord < minSize.getRight() || block.zCoord > maxSize.getRight()){
            block.zCoord = mirrored ? minSize.getRight() : maxSize.getRight();
            --block.yCoord;
        }

        return WorldBlockCoord.of(
                (int)(xF + worldLocation.xCoord),
                (int)(yF + worldLocation.yCoord),
                (int)(zF + worldLocation.zCoord));
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
