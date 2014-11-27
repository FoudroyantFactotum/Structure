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
package mod.steamnsteel.block.structure;

import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.tileentity.BallMillTE;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import mod.steamnsteel.utility.structure.StructureBlockIterator;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BallMillBlock extends SteamNSteelStructureBlock implements ITileEntityProvider
{
    public static final String NAME = "ballMill";

    public BallMillBlock()
    {
        setBlockName(NAME);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new BallMillTE();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p_149727_5_, int meta, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {
        final Orientation orientation = Orientation.getdecodedOrientation(meta);
        StructureBlockIterator itr = new StructureBlockIterator(getPattern(), Vec3.createVectorHelper(x, y, z), orientation, false);

        while (itr.hasNext())
        {
            final WorldBlockCoord block = itr.next();
            block.setBlock(world, ModBlock.blockPlotonium);
        }
        return false;
    }
}
