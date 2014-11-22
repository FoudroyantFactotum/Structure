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
package mod.steamnsteel.utility.crafting;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.util.Vec3;

import static com.google.common.base.Preconditions.*;

public class StructurePattern
{
    ImmutableMap<Character, Block> blocks;
    final int rowsPerLayer;
    final ImmutableList<String> pattern;

    public StructurePattern(ImmutableMap<Character, Block> blocks, int rowsPerLayer, String... recRows){
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
        this.rowsPerLayer = rowsPerLayer;
        pattern = builder.build();
    }

    public Block getBlock(int x, int y, int z)
    {
        //TODO check Bounds?
        final Character c = pattern.get(z + y*rowsPerLayer).charAt(x);
        return blocks.get(c);
    }

    public Block getBlock(Vec3 v)
    {
        return getBlock((int)v.xCoord, (int)v.yCoord, (int)v.zCoord);
    }

    public Vec3 getSize()
    {
        return Vec3.createVectorHelper(
                pattern.get(0).length(),
                pattern.size()/rowsPerLayer,
                rowsPerLayer);
    }

    public String toString(){
        return Objects.toStringHelper(this)
                .add("pattern", pattern)
                .add("rowsPerLayer", rowsPerLayer)
                .add("blocks", blocks)
                .add("Size", getSize())
                .toString();
    }

    public ImmutableMap<Character, Block> getBlockMap()
    {
        return blocks;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(pattern) + Objects.hashCode(rowsPerLayer) + Objects.hashCode(blocks);
    }


}
