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
package mod.steamnsteel.structure;

import mod.steamnsteel.block.SteamNSteelStructureBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.*;
import static mod.steamnsteel.block.SteamNSteelStructureBlock.bindLocalToGlobal;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static net.minecraft.block.BlockDirectional.FACING;

public class StructureBlockItem extends ItemBlock
{
    public StructureBlockItem(Block block)
    {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        /*if (world.isRemote)
            return false;*/

        final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) this.block;

        if (player == null)
        {
            return false;
        }

        final int orientation = (MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5)) & 3;
        final EnumFacing horizontal = EnumFacing.getHorizontal(orientation);
        final boolean isMirrored = false; //player.isSneaking(); Disabled until fix :p todo fix structure mirroring

        newState = newState.withProperty(FACING, horizontal).withProperty(MIRROR, isMirrored);

        //find master block location
        final BlockPos hSize = block.getPattern().getHalfBlockBounds();
        final BlockPos ml = block.getPattern().getMasterLocation();

        BlockPos mLoc
                = localToGlobal(
                -hSize.getX() + ml.getX(), ml.getY(), -hSize.getZ() + ml.getZ(),
                pos.getX(), pos.getY(), pos.getZ(),
                horizontal, isMirrored, block.getPattern().getBlockBounds());

        //check block locations
        for (final MutableBlockPos local : block.getPattern().getStructureItr())
        {
            final BlockPos coord = bindLocalToGlobal(mLoc, local, horizontal, isMirrored, block.getPattern().getBlockBounds());

            if (!block.getPattern().hasBlockAt(local))
            {
                continue;
            }

            if (!world.getBlockState(coord).getBlock().isReplaceable(world, coord))
            {
                return false;
            }
        }

        world.setBlockState(mLoc, newState, 0x2);
        block.onBlockPlacedBy(world, mLoc, newState, player, stack);

        /*ModNetwork.network.sendToAllAround(
                        new StructurePacket(mLoc, block.getRegHash(), horizontal, isMirrored, StructurePacketOption.BUILD),
                        new NetworkRegistry.TargetPoint(world.provider.getDimensionId(), mLoc.getX(), mLoc.getY(), mLoc.getZ(), 30)
                );*/
        return true;
    }


}
