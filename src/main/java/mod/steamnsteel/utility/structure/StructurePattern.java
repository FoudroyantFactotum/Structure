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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.Vec3;

import static com.google.common.base.Preconditions.*;

public class StructurePattern
{
    public static final StructurePattern MISSING_STRUCTURE = new StructurePattern(1,1,1);

    float[][] collisionBoxes;
    ImmutableMap<Character, Block> blocks;
    ImmutableList<String> pattern;
    final Vec3 size;

    public StructurePattern(ImmutableMap<Character, Block> blocks, int rowsPerLayer, String... recRows)
    {
        Builder<String> builder = ImmutableList.builder();

        int recRowLength = recRows[0].length();
        int count = 0;
        for (String recRow: recRows) {
            checkState(recRowLength == recRow.length(), "Recipe row must be of the same length");
            builder.add(recRow);
            ++count;
        }

        checkState(count % rowsPerLayer == 0, "Recipe must fill defined box " + count);

        this.blocks = blocks;
        pattern = builder.build();

        size = Vec3.createVectorHelper(
                recRowLength,
                pattern.size() / rowsPerLayer,
                rowsPerLayer);

        collisionBoxes = new float[][]{{
                -recRowLength/2,
                0,
                -rowsPerLayer/2,
                (float)Math.ceil(recRowLength/2),
                (float)pattern.size() / rowsPerLayer,
                (float)Math.ceil(rowsPerLayer/2)}};
    }

    public StructurePattern(int xSize, int ySize, int zSize)
    {
        blocks = null;
        pattern = null;

        size = Vec3.createVectorHelper(xSize,ySize,zSize);

        collisionBoxes = new float[][]{{
                -xSize/2,
                0,
                -zSize/2,
                (float)Math.ceil(xSize/2),
                ySize,
                (float)Math.ceil(zSize/2)}};
    }


    public Block getBlock(int x, int y, int z)
    {
        if (blocks == null) return Blocks.air;

        //TODO check Bounds?
        final Character c = pattern.get((int) (z + y*size.zCoord)).charAt(x);
        return blocks.get(c);
    }

    public Block getBlock(Vec3 v)
    {
        return getBlock((int)v.xCoord, (int)v.yCoord, (int)v.zCoord);
    }

    public Vec3 getSize()
    {
        return Vec3.createVectorHelper(
                size.xCoord,
                size.yCoord,
                size.zCoord);
    }

    public Vec3 getHalfSize()
    {
        return Vec3.createVectorHelper(
                size.xCoord*0.5,
                0,
                size.zCoord*0.5);
    }

    public ImmutableMap<Character, Block> getBlockMap()
    {
        return blocks;
    }

    public float[][] getCollisionBoxes()
    {
        return collisionBoxes.clone();
    }

    public String toString(){
        return Objects.toStringHelper(this)
                .add("pattern", pattern)
                .add("blocks", blocks)
                .add("size", size)
                .add("collisionBoxes", collisionBoxes)
                .toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(pattern) + Objects.hashCode(size) + Objects.hashCode(blocks)
                + Objects.hashCode(collisionBoxes);
    }


}
