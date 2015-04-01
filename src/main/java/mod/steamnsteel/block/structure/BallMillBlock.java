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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.BallMillTE;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;

public class BallMillBlock extends SteamNSteelStructureBlock implements ITileEntityProvider
{
    public static final String NAME = "ballMill";

    @SideOnly(Side.CLIENT)
    private static final Random rnd = new Random(System.currentTimeMillis());

    public BallMillBlock()
    {
        setBlockName(NAME);
    }

    private static float rndRC()
    {
        return rnd.nextFloat()*2.0f-1.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void spawnBreakParticle(World world, SteamNSteelStructureTE te, StructureBlockCoord coord, float sx, float sy, float sz)
    {
        final int x = coord.getX();
        final int y = coord.getY();
        final int z = coord.getZ();

        StructureDefinition pattern = te.getPattern();


        if (pattern != null) {
            final Block block = pattern.getBlock(coord.getLX(), coord.getLY(), coord.getLZ());

            if (block != null)
            {
                for (int i = 0; i < 5; ++i)
                {
                    world.spawnParticle("explode", x + rndRC(), y + 1, z + rndRC(), sx, sy, sz);
                    world.spawnParticle("explode", x, y + 0.5, z, sx, sy, sz);
                    world.spawnParticle("explode", x + rndRC(), y, z + rndRC(), sx, sy, sz);
                }
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new BallMillTE();
    }

}
