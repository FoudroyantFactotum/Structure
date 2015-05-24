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

import cpw.mods.fml.common.network.NetworkRegistry;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.item.tool.SSToolShovel;
import mod.steamnsteel.library.Material;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.coordinates.StructureBlockIterator;
import mod.steamnsteel.structure.net.StructureParticleChoice;
import mod.steamnsteel.structure.net.StructureParticlePacket;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.utility.ModNetwork;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.isMirrored;
import static mod.steamnsteel.block.SteamNSteelStructureBlock.print;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public class BuildFormTool extends SSToolShovel
{
    public BuildFormTool()
    {
        super(Material.STEEL);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        //if (world.isRemote)
        {
            final Pair<StructureBlockIterator, SteamNSteelStructureBlock> res = uberStructureSearch(world, x, y, z);

            if (res != null)
            {
                final ImmutableTriple<Integer, Integer, Integer> blkLoc = res.getLeft().getWorldLocation();
                print("Uber-Structure-Search found matching Structure : ", res.getRight(), " : ", blkLoc, " : ", res.getLeft().getOrientation());

                final int meta = res.getLeft().getOrientation().encode();
                final SteamNSteelStructureBlock block = res.getRight();

                world.setBlock(blkLoc.getLeft(), blkLoc.getMiddle(), blkLoc.getRight(), block, meta, 0x3);
                block.formStructure(world, res.getLeft(), meta);
                block.onPostBlockPlaced(world, blkLoc.getLeft(), blkLoc.getMiddle(), blkLoc.getRight(), meta);

               ModNetwork.network.sendToAllAround(
                        new StructureParticlePacket(blkLoc.getLeft(), blkLoc.getMiddle(), blkLoc.getRight(), block.getRegHash(), getdecodedOrientation(meta), isMirrored(meta), StructureParticleChoice.BUILD),
                        new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 30)
                );
            }
            else
                print("Uber-Structure-Search failed!");
        }

        return false;
    }

    private Pair<StructureBlockIterator, SteamNSteelStructureBlock> uberStructureSearch(World world, int x, int y, int z)
    {
        //do uber search and build structure todo Threaded? Reduce search space? Reduce memory usage?
        final Vec3 loc = Vec3.createVectorHelper(x,y,z);

        for (SteamNSteelStructureBlock ssBlock : StructureRegistry.getStructureList())
        {
            final StructureDefinition sd = ssBlock.getPattern();

            final ImmutableTriple<Integer, Integer, Integer> tl = sd.getToolBuildLocation();
            final ImmutableTriple<Integer, Integer, Integer> t2 = sd.getMasterLocation();

            //every Direction nsew
            nextOrientation:
            for (Orientation o : Orientation.values())
            {
                final ImmutableTriple<Integer,Integer,Integer> pml =
                        localToGlobal(
                                tl.getLeft() - t2.getLeft(), tl.getMiddle() - t2.getMiddle(), tl.getRight() - t2 .getRight(),
                                (int)loc.xCoord, (int)loc.yCoord, (int)loc.zCoord,
                                o, false, sd
                        );

                final StructureBlockIterator itr = new StructureBlockIterator(
                        sd,
                        pml,
                        o,
                        false//mirroring :p
                );

                while (itr.hasNext())
                {
                    final StructureBlockCoord coord = itr.next();
                    final Block ptnBlk = sd.getBlock(coord.getLX(),coord.getLY(),coord.getLZ());

                    if (ptnBlk == null || ptnBlk != coord.getBlock(world))
                        continue nextOrientation;
                }

                //found match eeek!
                itr.cleanIterator();
                return Pair.of(itr, ssBlock);
            }
        }

        //nothing matches
        return null;
    }
}
