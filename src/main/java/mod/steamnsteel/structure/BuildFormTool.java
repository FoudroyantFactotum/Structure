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

import mod.steamnsteel.item.tool.SSToolShovel;
import mod.steamnsteel.library.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BuildFormTool extends SSToolShovel
{
    public BuildFormTool()
    {
        super(Material.STEEL);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        //todo fix
        /*if (!world.isRemote)
        {
            final Pair<StructureBlockIterator, SteamNSteelStructureBlock> res = uberStructureSearch(world, x, y, z);

            if (res != null)
            {
                final ImmutableTriple<Integer, Integer, Integer> blkLoc = res.getLeft().getWorldLocation();
                //print("Uber-Structure-Search found matching Structure : ", res.getRight(), " : ", blkLoc, " : ", res.getLeft().getOrientation());

                final int meta = res.getLeft().getOrientation().encode();
                final SteamNSteelStructureBlock block = res.getRight();

                world.setBlock(blkLoc.getLeft(), blkLoc.getMiddle(), blkLoc.getRight(), block, meta, 0x2);
                block.formStructure(world, res.getLeft(), meta, 0x2);
                block.onPostBlockPlaced(world, blkLoc.getLeft(), blkLoc.getMiddle(), blkLoc.getRight(), meta);

               ModNetwork.network.sendToAllAround(
                        new StructurePacket(blkLoc.getLeft(), blkLoc.getMiddle(), blkLoc.getRight(), block.getRegHash(), getdecodedOrientation(meta), isMirrored(meta), StructurePacketOption.BLOCK_PARTICLE_BUILD),
                        new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 30)
                );
            } //else
                //print("No Structure Found");
        }*/

        return false;
    }

    /*private Pair<StructureBlockIterator, SteamNSteelStructureBlock> uberStructureSearch(World world, int x, int y, int z)
    {
        //do uber search and build structure todo Threaded? Reduce search space? Reduce memory usage?
        final Vec3 loc = Vec3.createVectorHelper(x,y,z);

        for (SteamNSteelStructureBlock ssBlock : StructureRegistry.getStructureList())
        {
            final StructureDefinition sd = ssBlock.getPattern();

            final ImmutableTriple<Integer, Integer, Integer> tl = sd.getToolFormLocation();
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
    }*/
}