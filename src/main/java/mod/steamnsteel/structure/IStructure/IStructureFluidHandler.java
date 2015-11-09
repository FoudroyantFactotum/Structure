package mod.steamnsteel.structure.IStructure;

import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public interface IStructureFluidHandler extends IFluidHandler
{
    boolean canStructureFill(EnumFacing from, Fluid fluid, TripleCoord blockID);
    boolean canStructureDrain(EnumFacing from, Fluid fluid, TripleCoord blockID);

    int structureFill(EnumFacing from, FluidStack resource, boolean doFill, TripleCoord blockID);
    FluidStack structureDrain(EnumFacing from, FluidStack resource, boolean doDrain, TripleCoord blockID);
    FluidStack structureDrain(EnumFacing from, int maxDrain, boolean doDrain, TripleCoord blockID);

    FluidTankInfo[] getStructureTankInfo(EnumFacing from, TripleCoord blockID);
}
