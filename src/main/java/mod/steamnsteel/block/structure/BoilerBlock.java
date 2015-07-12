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

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.StructureDefinitionBuilder;
import mod.steamnsteel.tileentity.structure.BoilerTE;
import mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public class BoilerBlock extends SteamNSteelStructureBlock implements ITileEntityProvider
{
    public static final String NAME = "boiler";

    @SideOnly(Side.CLIENT)
    private static float rndRC()
    {
        return ((float)Math.random())*1.0f-0.5f;
    }

    public BoilerBlock()
    {
        setBlockName(NAME);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnBreakParticle(World world, SteamNSteelStructureTE te, ImmutableTriple<Integer, Integer, Integer> coord, float sx, float sy, float sz)
    {
        /*final int x = coord.getX();
        final int y = coord.getY();
        final int z = coord.getZ();

        final Block block = coord.getStructureDefinition().getBlock(coord.getLX(), coord.getLY(), coord.getLZ());

        if (block != null)
        {
            for (int i = 0; i < 5; ++i)
            {
                world.spawnParticle("explode", x + rndRC(), y + 1, z + rndRC(), sx, sy, sz);
                world.spawnParticle("explode", x, y + 0.5, z, sx, sy, sz);
                world.spawnParticle("explode", x + rndRC(), y, z + rndRC(), sx, sy, sz);
            }
        }*/
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new BoilerTE();
    }

    @Override
    public boolean onStructureBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz, ImmutableTriple<Integer, Integer, Integer> sbID, int sbx, int sby, int sbz)
    {
        print("Master TE: ", world.getTileEntity(x,y,z));
        return super.onStructureBlockActivated(world, x, y, z, player, side, sx, sy, sz, sbID, sbx, sby, sbz);
    }

    @Override
    public StructureDefinitionBuilder getStructureBuild()
    {
        StructureDefinitionBuilder builder = new StructureDefinitionBuilder();

        builder.assignBlockDefinitions(ImmutableMap.of(
                'p', "steamnsteel:blockPlotonium",
                's', "steamnsteel:blockSteel",
                'g', "minecraft:glass_pane",
                'f', "minecraft:fire",
                'w', "minecraft:planks"
        ));

        builder.assignConstructionBlocks(
                new String[]{
                        "ppp",
                        "sss",
                        "ppp"
                },
                new String[]{
                        "ppp",
                        "sws",
                        "pgp"
                },
                new String[]{
                        "ppp",
                        "sfs",
                        "pgp"
                },
                new String[]{
                        "ppp",
                        "sss",
                        "ppp"
                }
        );

        builder.assignToolFormPosition(ImmutableTriple.of(1,2,2));

        builder.setConfiguration(ImmutableTriple.of(0,0,0),
                new String[]{
                        "M--",
                        "---",
                        "---"
                },
                new String[]{
                        "---",
                        "---",
                        "---"
                },
                new String[]{
                        "---",
                        "---",
                        "---"
                },
                new String[]{
                        "---",
                        "---",
                        "---"
                }
        );

        builder.setCollisionBoxes(
                new float[]{0.7f,3.5f,0.7f, 2.3f,4.0f,2.3f},
                new float[]{0.3f,3.0f,0.3f, 2.7f,3.5f,2.7f},
                new float[]{0.0f,1.0f,0.0f, 3.0f,3.0f,3.0f},
                new float[]{0.3f,0.5f,0.3f, 2.7f,1.0f,2.7f},
                new float[]{0.7f,0.0f,0.7f, 2.3f,0.5f,2.3f}
        );

        return builder;
    }
}
