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
import mod.steamnsteel.tileentity.BoilerTE;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import java.util.Random;

public class BoilerBlock extends SteamNSteelStructureBlock implements ITileEntityProvider
{
    public static final String NAME = "boiler";

    @SideOnly(Side.CLIENT)
    private static final Random rnd = new Random(System.currentTimeMillis());

    @SideOnly(Side.CLIENT)
    private static float rndRC()
    {
        return rnd.nextFloat()*4.0f-2.0f;
    }

    public BoilerBlock()
    {
        setBlockName(NAME);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void spawnBreakParticle(World world, SteamNSteelStructureTE te, float x, float y, float z, float sx, float sy, float sz)
    {
        for (int i=0; i<10; ++i)
        {
            world.spawnParticle("explode", x + rndRC(), y + 2, z + rndRC(), sx, sy, sz);
            world.spawnParticle("explode", x, y + 1, z, sx, sy, sz);
            world.spawnParticle("explode", x + rndRC(), y, z + rndRC(), sx, sy, sz);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new BoilerTE();
    }

}
