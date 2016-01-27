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
package com.foudroyantfactotum.tool.structure.registry;

import com.google.common.base.Objects;
import com.foudroyantfactotum.tool.structure.IStructure.IPartBlockState;
import com.foudroyantfactotum.tool.structure.coordinates.BlockPosUtil;
import com.foudroyantfactotum.tool.structure.coordinates.StructureIterable;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

/**
 * Structures contain two states. Construction & Form state C->F, F->C.
 *
 * 2D Example: (Ref grid CG-coord)
 *              Construction           |              Form
 * b -> block, '-' -> null, ' ' -> air | none, S -> Structure, H -> Shape
 *
 *   State(Construction)      State(Form)
 *        b,b,b,b               -,-,H,-
 *        b,b,b,b     ====>     -,S,H,-
 *        b,b,b,b               -,-,H,-
 *
 * Var belongs to state:
 *
 * S(C) {
 *     blocks           = (as above)
 *     metadata         = metadata of the blocks
 *     toolFormPosition = eg. (2,2) Note: player needs to be able to hit location with the tool else you'll never be
 *                            able to form the structure
 * }
 *
 * S(F) {
 *     sbLayout{Size, SizeHlf} = specify the location of shape blocks within State(Form)
 *     masterPosition          = translation of origin from S(C).origin -> S(F).origin eg. (0,0) -> (1,1)
 *                               Note: location must exist in sbLayout
 *
 *     collisionBoxes          = #imagination
 * }
 *
 * Also watch out. The "getBlock(...)" can return blocks that are unregistered with minecraft (and null (no block)). Double check with an
 * (result instanceOf IGeneralBlock) for safety.
 */
public class StructureDefinition
{
    private BitSet sbLayout;
    private BlockPos sbLayoutSize;
    private BlockPos sbLayoutSizeHlf;

    private BlockPos masterPosition;
    private BlockPos toolFormPosition;

    private IPartBlockState[][][] blocks;
    private float[][] collisionBoxes;

    private StructureDefinition()
    {
        //noop
    }

    public StructureDefinition(BitSet sbLayout,
                               BlockPos sbLayoutSize,
                               BlockPos masterPosition,
                               BlockPos toolFormPosition,

                               IPartBlockState[][][] blocks,
                               float[][] collisionBoxes)
    {
        this.sbLayout = sbLayout;
        this.sbLayoutSize = sbLayoutSize;

        this.masterPosition = masterPosition;
        this.toolFormPosition = toolFormPosition;

        this.blocks = blocks;
        this.collisionBoxes = collisionBoxes;

        sbLayoutSizeHlf = BlockPosUtil.of(
                sbLayoutSize.getX()/2,
                sbLayoutSize.getY()/2,
                sbLayoutSize.getZ()/2);
    }

    public boolean hasBlockAt(BlockPos loc, EnumFacing d) { return hasBlockAt(loc.getX() + d.getFrontOffsetX(), loc.getY() + d.getFrontOffsetY(), loc.getZ() + d.getFrontOffsetZ()); }
    public boolean hasBlockAt(BlockPos loc) { return hasBlockAt(loc.getX(), loc.getY(), loc.getZ()); }
    public boolean hasBlockAt(int x, int y, int z)
    {
        x += masterPosition.getX();
        y += masterPosition.getY();
        z += masterPosition.getZ();

        return  x < sbLayoutSize.getX() && x > -1 &&
                y < sbLayoutSize.getY() && y > -1 &&
                z < sbLayoutSize.getZ() && z > -1 &&
                sbLayout.get(x + y * sbLayoutSize.getX() * sbLayoutSize.getZ() + z * sbLayoutSize.getX());
    }

    public IPartBlockState getBlock(BlockPos loc) { return getBlock(loc.getX(), loc.getY(), loc.getZ()); }
    public IPartBlockState getBlock(int x, int y, int z)
    {
        x += masterPosition.getX();
        y += masterPosition.getY();
        z += masterPosition.getZ();

        if (blocks.length > x       && x > -1 &&
            blocks[x].length > y    && y > -1 &&
            blocks[x][y].length > z && z > -1)
            return blocks[x][y][z];

        return null;
    }

    public BlockPos getBlockBounds()
    {
        return sbLayoutSize;
    }

    public BlockPos getHalfBlockBounds()
    {
        return sbLayoutSizeHlf;
    }

    public BlockPos getMasterLocation()
    {
        return masterPosition;
    }

    public BlockPos getToolFormLocation()
    {
        return toolFormPosition;
    }

    public Iterable<MutableBlockPos> getStructureItr()
    {
        return new Iterable<MutableBlockPos>()
        {
            @Override
            public Iterator<MutableBlockPos> iterator()
            {
                return new StructureIterable(
                        -masterPosition.getX(),                -masterPosition.getY(),                   -masterPosition.getZ(),
                        blocks.length - masterPosition.getX(), blocks[0].length - masterPosition.getY(), blocks[0][0].length - masterPosition.getZ());
            }
        };
    }

    public float[][] getCollisionBoxes()
    {
        return collisionBoxes;
    }

    public String toString(){
        return Objects.toStringHelper(this)
                .add("blocks", Arrays.toString(blocks))
                .add("collisionBoxes", Arrays.toString(collisionBoxes))
                .add("masterPosition", masterPosition)
                .add("toolFormPosition", toolFormPosition)
                .add("sbLayoutSize", sbLayoutSize)
                .add("sbLayout", sbLayout)
                .toString();
    }
}
