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

import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class StructureBlockItem extends ItemBlock
{
    public StructureBlockItem(Block block)
    {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
        if (player == null) return false;

        final int orientation = BlockDirectional.getDirection(MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5));
        final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) field_150939_a;

        StructureBlockIterator itr = new StructureBlockIterator(
                block.getPattern(), Vec3.createVectorHelper(x, y, z), Orientation.getdecodedOrientation(orientation), player.isSneaking());

        while (itr.hasNext())
        {
            final WorldBlockCoord wBlock = itr.next();

            if (!wBlock.isAirBlock(world)) return false;
        }

        world.setBlock(x, y, z, block, metadata, 3);
        block.onBlockPlacedBy(world, x, y, z, player, stack);
        block.onPostBlockPlaced(world, x, y, z, metadata);

        return true;
    }
}
