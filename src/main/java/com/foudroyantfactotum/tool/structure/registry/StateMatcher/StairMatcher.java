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
package com.foudroyantfactotum.tool.structure.registry.StateMatcher;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.BlockStairs.EnumShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import static net.minecraft.block.BlockStairs.HALF;
import static net.minecraft.block.BlockStairs.SHAPE;

/*
Because some states match each other when you decide not to use meta data
*/
public class StairMatcher implements IStateMatcher
{
    @Override
    public boolean matchBlockState(IBlockState b1, IBlockState b2)
    {
        final EnumFacing b1Facing = b1.getValue(BlockDirectional.FACING);
        final EnumFacing b2Facing = b2.getValue(BlockDirectional.FACING);

        final EnumHalf b1Half = b1.getValue(HALF);
        final EnumHalf b2Half = b2.getValue(HALF);

        final EnumShape b1Shape = b1.getValue(SHAPE);
        final EnumShape b2Shape = b2.getValue(SHAPE);

        if (b1Half != b2Half)
        {
            return false;
        }

        if (b1Facing == b2Facing)
        {
            return b1Shape == b2Shape;
        }

        if (b1Facing.getOpposite() == b2Facing)
        {
            return false;
        }

        if ((b1Shape == EnumShape.OUTER_RIGHT && b2Shape == EnumShape.OUTER_LEFT) ||
                (b1Shape == EnumShape.INNER_RIGHT && b2Shape == EnumShape.INNER_LEFT) ||
                (b1Shape == EnumShape.OUTER_LEFT && b2Shape == EnumShape.OUTER_RIGHT) ||
                (b1Shape == EnumShape.INNER_LEFT && b2Shape == EnumShape.INNER_RIGHT))
        {
            return true;
        }

        return false;
    }
}
