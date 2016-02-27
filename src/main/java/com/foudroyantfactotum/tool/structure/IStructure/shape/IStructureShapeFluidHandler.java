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
package com.foudroyantfactotum.tool.structure.IStructure.shape;

import com.foudroyantfactotum.tool.structure.IStructure.structure.IStructureFluidHandler;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public interface IStructureShapeFluidHandler<E extends IStructureFluidHandler> extends IFluidHandler, IStructureShapeTE<E>
{
    FluidTankInfo[] emptyFluidTankInfo = {};

    @Override
    default int fill(EnumFacing from, FluidStack resource, boolean doFill)
    {
        if (hasOriginTE())
        {
            return getOriginTE().structureFill(from, resource, doFill, getLocal());
        }

        return 0;
    }

    @Override
    default FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
    {
        if (hasOriginTE())
        {
            return getOriginTE().structureDrain(from, resource, doDrain, getLocal());
        }

        return null;
    }

    @Override
    default FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
    {
        if (hasOriginTE())
        {
            return getOriginTE().structureDrain(from, maxDrain, doDrain, getLocal());
        }

        return null;
    }

    @Override
    default boolean canFill(EnumFacing from, Fluid fluid)
    {
        return hasOriginTE() && getOriginTE().canStructureFill(from, fluid, getLocal());
    }

    @Override
    default boolean canDrain(EnumFacing from, Fluid fluid)
    {
        return hasOriginTE() && getOriginTE().canStructureDrain(from, fluid, getLocal());
    }

    @Override
    default FluidTankInfo[] getTankInfo(EnumFacing from)
    {
        if (hasOriginTE())
        {
            return getOriginTE().getStructureTankInfo(from, getLocal());
        }

        return emptyFluidTankInfo;
    }
}
