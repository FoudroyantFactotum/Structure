/*
 * Copyright (c) 2016 Foudroyant Factotum
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
package com.foudroyantfactotum.tool.structure.IStructure;

import com.foudroyantfactotum.tool.structure.StructureRegistry;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import static com.foudroyantfactotum.tool.structure.block.StructureBlock.getMirror;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobal;

public interface IStructureTE extends ITileEntity
{
    default IBlockState getTransmutedBlock()
    {
        final StructureBlock sb = StructureRegistry.getStructureBlock(getRegHash());

        if (sb != null)
        {
            final IBlockState state = getWorld().getBlockState(getPos());

            if (state != null && state.getBlock() instanceof IStructureTE)
            {
                final IBlockState block = sb.getPattern().getBlock(getLocal()).getBlockState();

                return block == null ?
                        Blocks.air.getDefaultState() :
                        localToGlobal(
                                block,
                                state.getValue(BlockDirectional.FACING),
                                getMirror(state)
                        );
            }
        }

        return Blocks.air.getDefaultState();
    }

    int getRegHash();
    StructureBlock getMasterBlockInstance();
    BlockPos getMasterBlockLocation();

    void configureBlock(BlockPos local, int registerHash);
    BlockPos getLocal();
}
