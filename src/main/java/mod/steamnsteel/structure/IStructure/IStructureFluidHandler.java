package mod.steamnsteel.structure.IStructure;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public interface IStructureFluidHandler extends IFluidHandler
{
    boolean canStructureFill(EnumFacing from, Fluid fluid, BlockPos local);
    boolean canStructureDrain(EnumFacing from, Fluid fluid, BlockPos local);

    int structureFill(EnumFacing from, FluidStack resource, boolean doFill, BlockPos local);
    FluidStack structureDrain(EnumFacing from, FluidStack resource, boolean doDrain, BlockPos local);
    FluidStack structureDrain(EnumFacing from, int maxDrain, boolean doDrain, BlockPos local);

    FluidTankInfo[] getStructureTankInfo(EnumFacing from, BlockPos local);
}
