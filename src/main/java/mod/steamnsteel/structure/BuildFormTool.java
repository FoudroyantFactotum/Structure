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
import mod.steamnsteel.item.tool.SSToolShovel;
import mod.steamnsteel.library.Material;
import mod.steamnsteel.structure.net.StructurePacket;
import mod.steamnsteel.structure.net.StructurePacketOption;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.utility.ModNetwork;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.MIRROR;
import static mod.steamnsteel.block.SteamNSteelStructureBlock.updateExternalNeighbours;
import static mod.steamnsteel.structure.coordinates.TransformLAG.*;

public class BuildFormTool extends SSToolShovel
{
    private static final ExecutorService pool = Executors.newFixedThreadPool(5);
    private static final EnumFacing[][] orientationPriority = {
            {EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.WEST}, //south
            {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH}, //west
            {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST}, //north
            {EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST}, //east
    };

    public BuildFormTool()
    {
        super(Material.STEEL);
        setUnlocalizedName(getUnlocalizedName() + "_form");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote || player == null)
        {
            return false;
        }

        final EnumFacing[] orientation = orientationPriority[MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5) & 3];
        final List<Future<SearchResult>> searchJobFuture = new ArrayList<Future<SearchResult>>(StructureRegistry.getStructureList().size());

        //search currently ignores multiple matches and take the first match available.
        for (final SteamNSteelStructureBlock sb : StructureRegistry.getStructureList())
        {
            searchJobFuture.add(pool.submit(new SearchJob(sb, world, pos, orientation)));
        }

        SearchResult result = null;

        for (final Future<SearchResult> res : searchJobFuture)
        {
            try
            {
                if (result == null)
                {
                    result = res.get();
                } else {
                    res.cancel(true);
                }
            }
            catch (InterruptedException e)
            {
                Logger.severe(e.getMessage());
            }
            catch (ExecutionException e)
            {
                Logger.severe(e.getMessage());
            }
        }

        searchJobFuture.clear();

        if (result != null)
        {
            final IBlockState state = result.block.getDefaultState()
                    .withProperty(BlockDirectional.FACING, result.orientation)
                    .withProperty(MIRROR, result.isMirrored);


            world.setBlockState(result.origin, state, 0x2);
            result.block.formStructure(world, result.origin, state, 0x2);

            updateExternalNeighbours(world, result.origin, result.block.getPattern(), result.orientation, result.isMirrored, true);

            ModNetwork.network.sendToAllAround(
                    new StructurePacket(result.origin, result.block.getRegHash(), result.orientation, result.isMirrored, StructurePacketOption.BUILD),
                    new NetworkRegistry.TargetPoint(world.provider.getDimensionId(), result.origin.getX(), result.origin.getY(), result.origin.getZ(), 30)
            );

            return true;
        }

        return false;
    }

    /**
     * Performs complete search on world at the location
     */
    private static class SearchJob implements Callable<SearchResult>
    {
        final SteamNSteelStructureBlock ssBlock;
        final World world;
        final BlockPos pos;

        final EnumFacing[] orientationOrder;

        SearchJob(SteamNSteelStructureBlock ssBlock, World world, BlockPos pos, EnumFacing[] orientationOrder)
        {
            this.ssBlock = ssBlock;
            this.world = world;
            this.pos = pos;
            this.orientationOrder = orientationOrder;
        }

        @Override
        public SearchResult call() throws Exception
        {
            final StructureDefinition sd = ssBlock.getPattern();
            final BlockPos tl = sd.getToolFormLocation();

            nextOrientation:
            for (final EnumFacing o: orientationOrder)
            {
                final BlockPos origin =
                        localToGlobal(
                                -tl.getX(), -tl.getY(), -tl.getZ(),
                                pos.getX(), pos.getY(), pos.getZ(),
                                o, false, sd.getBlockBounds()
                        );

                for (final MutableBlockPos local : sd.getStructureItr())
                {
                    final IBlockState b = sd.getBlock(local);

                    //alter local coord var and changes it to world coord.
                    mutLocalToGlobal(local, origin, o, false, sd.getBlockBounds());

                    final IBlockState ncwb = world.getBlockState(local);
                    final IBlockState wb = ncwb.getBlock().getActualState(ncwb, world, local);

                    if (b != null && (b.getBlock() != wb.getBlock() || !doBlockStatesMatch(localToGlobal(b, o, false), wb)))
                    {
                        continue nextOrientation;
                    }
                }

                //found match, eeek!
                final SearchResult result = new SearchResult();

                result.block = ssBlock;
                result.origin = origin;
                result.orientation = o;
                result.isMirrored = false; //todo fix mirror state.

                return result;
            }

            //no matches for this structure
            return null;
        }
    }

    /***
     * final result struct, used to return result from the search.
     */
    private static final class SearchResult
    {
        public SteamNSteelStructureBlock block;
        public EnumFacing orientation;
        public boolean isMirrored;
        public BlockPos origin;
    }
}
