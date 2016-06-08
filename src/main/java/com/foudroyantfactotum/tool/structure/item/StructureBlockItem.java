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
package com.foudroyantfactotum.tool.structure.item;

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobal;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.mutLocalToGlobal;

public class StructureBlockItem extends ItemBlock
{
    public StructureBlockItem(Block block)
    {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        final StructureBlock block = (StructureBlock) this.block;

        if (player == null)
        {
            return false;
        }

        final EnumFacing orientation = EnumFacing.getHorizontal(MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5) & 3);
        final boolean mirror = block.canMirror() && player.isSneaking();

        newState = newState.withProperty(BlockHorizontal.FACING, orientation);

        if (block.canMirror())
        {
            newState = newState.withProperty(StructureBlock.MIRROR, mirror);
        }

        //find master block location
        final BlockPos hSize = block.getPattern().getHalfBlockBounds();
        final BlockPos ml = block.getPattern().getMasterLocation();

        BlockPos origin
                = localToGlobal(
                -hSize.getX() + ml.getX(), ml.getY(), -hSize.getZ() + ml.getZ(),
                pos.getX(), pos.getY(), pos.getZ(),
                orientation, mirror, block.getPattern().getBlockBounds());

        //check block locations
        for (final MutableBlockPos local : block.getPattern().getStructureItr())
        {
            if (!block.getPattern().hasBlockAt(local))
            {
                continue;
            }

            mutLocalToGlobal(local, origin, orientation, mirror, block.getPattern().getBlockBounds());

            if (!world.getBlockState(local).getBlock().isReplaceable(world, local))
            {
                return false;
            }
        }

        world.setBlockState(origin, newState, 0x2);
        block.onBlockPlacedBy(world, origin, newState, player, stack);

        return true;
    }
}
