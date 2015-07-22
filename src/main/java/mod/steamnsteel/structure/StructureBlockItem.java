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
import mod.steamnsteel.structure.coordinates.TripleIterator;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.bindLocalToGlobal;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;

public class StructureBlockItem extends ItemBlock
{
    public StructureBlockItem(Block block)
    {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
        final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) field_150939_a;

        if (player == null)
        {
            return false;
        }
        final Orientation o = Orientation.getdecodedOrientation(BlockDirectional.getDirection(MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5)));
        final boolean isMirrored = false; //player.isSneaking(); Disabled until fix :p todo fix structure mirroring

        //find master block location
        final ImmutableTriple<Integer, Integer, Integer> hSize = block.getPattern().getHalfBlockBounds();
        final ImmutableTriple<Integer, Integer, Integer> ml = block.getPattern().getMasterLocation();

        ImmutableTriple<Integer, Integer, Integer> mLoc
                = localToGlobal(
                -hSize.getLeft() - ml.getLeft(), ml.getMiddle(), -hSize.getRight() - ml.getRight(),
                x, y, z,
                o, isMirrored, block.getPattern().getBlockBounds());

        //check block locations
        final TripleIterator itr = block.getPattern().getFormItr();

        while (itr.hasNext())
        {
            final WorldBlockCoord coord = bindLocalToGlobal(mLoc, itr.next(), o, isMirrored, block.getPattern().getBlockBounds());

            if (!coord.isReplaceable(world))
            {
                return false;
            }
        }

        world.setBlock(mLoc.getLeft(), mLoc.getMiddle(), mLoc.getRight(), block, metadata, 0x3);
        block.onBlockPlacedBy(world, mLoc.getLeft(), mLoc.getMiddle(), mLoc.getRight(), player, stack);
        block.onPostBlockPlaced(world, mLoc.getLeft(), mLoc.getMiddle(), mLoc.getRight(), world.getBlockMetadata(x,y,z));

        return true;
    }


}
