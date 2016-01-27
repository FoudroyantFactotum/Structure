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
package com.foudroyantfactotum.tool.structure.registry.StateCorrecter;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockStairs.EnumShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobal;
import static net.minecraft.block.BlockStairs.*;
import static net.minecraft.block.BlockStairs.EnumShape.*;

public class StairsMinecraftRotation implements IStructurePatternStateCorrecter
{
    private static EnumShape[] opp = {
            STRAIGHT,
            INNER_RIGHT,
            INNER_LEFT,
            OUTER_RIGHT,
            OUTER_LEFT
    };

    @Override
    public IBlockState alterBlockState(IBlockState state, EnumFacing orientation, boolean mirror)
    {
        final EnumFacing facing = state.getValue(BlockDirectional.FACING);
        final EnumShape shape = state.getValue(SHAPE);

        return state
                .withProperty(BlockDirectional.FACING, localToGlobal(facing, orientation, mirror))
                .withProperty(SHAPE, mirror ? opp[shape.ordinal()] : shape);
    }
}
