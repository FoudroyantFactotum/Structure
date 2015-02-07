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
import mod.steamnsteel.structure.coordinates.StructureBlockIterator;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import static java.lang.Math.PI;

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

        if (player == null) return false;
        //if (!world.getEntitiesWithinAABBExcludingEntity(null, block.getSelectedBoundingBoxFromPool(world, x, y, z)).isEmpty()) return false;//todo fix directional check
        //todo deal with entity colisions
        final Orientation o = Orientation.getdecodedOrientation(BlockDirectional.getDirection(MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5)));

        final Vec3 hlfSz = block.getPattern().getHalfSize();
        hlfSz.xCoord *= -1;
        hlfSz.zCoord *= -1;
        hlfSz.rotateAroundY((float) (PI * (1.0-o.ordinal()/2.0)));

        final Vec3 mLoc = Vec3.createVectorHelper(x+(int)hlfSz.xCoord,y, z+(int)hlfSz.zCoord);
        final StructureBlockIterator itr = new StructureBlockIterator(block.getPattern(), mLoc, o, player.isSneaking());

        while (itr.hasNext())
            if (!itr.next().isAirBlock(world)) return false;

        world.setBlock((int)mLoc.xCoord, (int)mLoc.yCoord,(int)mLoc.zCoord, block, metadata, 3);
        block.onBlockPlacedBy(world, (int)mLoc.xCoord, (int)mLoc.yCoord,(int)mLoc.zCoord, player, stack);
        block.onPostBlockPlaced(world, (int)mLoc.xCoord, (int)mLoc.yCoord,(int)mLoc.zCoord, metadata);

        return true;
    }
}
