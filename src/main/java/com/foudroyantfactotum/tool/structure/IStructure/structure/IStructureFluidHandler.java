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
package com.foudroyantfactotum.tool.structure.IStructure.structure;

import com.foudroyantfactotum.tool.structure.IStructure.IStructureTE;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public interface IStructureFluidHandler extends IFluidHandler, IStructureTE
{
    FluidTankInfo[] emptyFluidTankInfo = {};

    boolean canStructureFill(EnumFacing from, Fluid fluid, BlockPos local);
    boolean canStructureDrain(EnumFacing from, Fluid fluid, BlockPos local);

    int structureFill(EnumFacing from, FluidStack resource, boolean doFill, BlockPos local);
    FluidStack structureDrain(EnumFacing from, FluidStack resource, boolean doDrain, BlockPos local);
    FluidStack structureDrain(EnumFacing from, int maxDrain, boolean doDrain, BlockPos local);

    FluidTankInfo[] getStructureTankInfo(EnumFacing from, BlockPos local);

    //================================================================
    //           D e f a u l t   F l u i d   H a n d l e r
    //================================================================

    @Override
    default int fill(EnumFacing from, FluidStack resource, boolean doFill)
    {
        return structureFill(from, resource, doFill, getLocal());
    }

    @Override
    default FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
    {
        return structureDrain(from, resource, doDrain, getLocal());
    }

    @Override
    default FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
    {
        return structureDrain(from, maxDrain, doDrain, getLocal());
    }

    @Override
    default boolean canFill(EnumFacing from, Fluid fluid)
    {
        return canStructureFill(from, fluid, getLocal());
    }

    @Override
    default boolean canDrain(EnumFacing from, Fluid fluid)
    {
        return canStructureDrain(from, fluid, getLocal());
    }

    @Override
    default FluidTankInfo[] getTankInfo(EnumFacing from)
    {
        return getStructureTankInfo(from, getLocal());
    }
}
