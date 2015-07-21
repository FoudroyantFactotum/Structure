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
import mod.steamnsteel.structure.coordinates.TripleIterator;
import mod.steamnsteel.structure.net.StructurePacket;
import mod.steamnsteel.structure.net.StructurePacketOption;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.utility.ModNetwork;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.bindLocalToGlobal;
import static mod.steamnsteel.block.SteamNSteelStructureBlock.updateExternalNeighbours;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;

public class BuildFormTool extends SSToolShovel
{
    public BuildFormTool()
    {
        super(Material.STEEL);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            final StructureSearchResult result = uberStructureSearch(world, x, y, z);

            if (result != null)
            {
                final int meta = result.orientation.encode() | (result.isMirrored ? SteamNSteelStructureBlock.flagMirrored : 0x0);

                world.setBlock(result.origin.getLeft(), result.origin.getMiddle(), result.origin.getRight(), result.block, meta, 0x2);
                result.block.formStructure(world, result.origin, meta, 0x2);

                updateExternalNeighbours(world, result.origin, result.block.getPattern(), result.orientation, result.isMirrored, true);

                ModNetwork.network.sendToAllAround(
                        new StructurePacket(result.origin.getLeft(), result.origin.getMiddle(), result.origin.getRight(),
                                result.block.getRegHash(), result.orientation, result.isMirrored,
                                StructurePacketOption.BUILD),
                        new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 30)
                );
            }
        }

        return false;
    }

    private StructureSearchResult uberStructureSearch(World world, int x, int y, int z)
    {
        //do uber search and build structure todo Threaded? Reduce search space? Reduce memory usage?

        for (SteamNSteelStructureBlock ssBlock : StructureRegistry.getStructureList())
        {
            final StructureDefinition sd = ssBlock.getPattern();

            final ImmutableTriple<Integer, Integer, Integer> tl = sd.getToolFormLocation();
            final ImmutableTriple<Integer, Integer, Integer> t2 = sd.getMasterLocation();

            //todo also search mirrored (currently disabled)
            //every Direction nsew
            nextOrientation:
            for (Orientation o : Orientation.values())
            {
                final ImmutableTriple<Integer,Integer,Integer> origin =
                        localToGlobal(
                                -tl.getLeft() - t2.getLeft(), -tl.getMiddle() - t2.getMiddle(), -tl.getRight() - t2 .getRight(),
                                x, y, z,
                                o, false, sd.getBlockBounds()
                        );

                final TripleIterator itr = sd.getConstructionItr();

                while (itr.hasNext())
                {
                    final ImmutableTriple<Integer, Integer, Integer> local = itr.next();
                    final WorldBlockCoord coord = bindLocalToGlobal(origin, local, o, false, sd.getBlockBounds());

                    if (sd.getBlock(local) == null || sd.getBlock(local) != coord.getBlock(world))
                    {
                        continue nextOrientation;
                    }
                }

                //found match eeek!
                final StructureSearchResult result = new StructureSearchResult();

                result.block = ssBlock;
                result.origin = origin;
                result.orientation = o;
                result.isMirrored = false; //todo fix mirror state.

                return result;
            }
        }

        //no matches
        return null;
    }

    private static final class StructureSearchResult
    {
        public SteamNSteelStructureBlock block;
        public Orientation orientation;
        public boolean isMirrored;
        public ImmutableTriple<Integer, Integer, Integer> origin;
    }
}
