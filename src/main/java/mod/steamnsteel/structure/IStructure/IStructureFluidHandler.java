package mod.steamnsteel.structure.IStructure;

import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public interface IStructureFluidHandler extends IFluidHandler
{
    boolean canStructureFill(ForgeDirection from, Fluid fluid, TripleCoord blockID);
    boolean canStructureDrain(ForgeDirection from, Fluid fluid, TripleCoord blockID);

    int structureFill(ForgeDirection from, FluidStack resource, boolean doFill, TripleCoord blockID);
    FluidStack structureDrain(ForgeDirection from, FluidStack resource, boolean doDrain, TripleCoord blockID);
    FluidStack structureDrain(ForgeDirection from, int maxDrain, boolean doDrain, TripleCoord blockID);

    FluidTankInfo[] getStructureTankInfo(ForgeDirection from, TripleCoord blockID);
}
