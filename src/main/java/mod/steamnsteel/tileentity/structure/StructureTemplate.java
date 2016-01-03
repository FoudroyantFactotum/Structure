package mod.steamnsteel.tileentity.structure;

import mod.steamnsteel.structure.registry.StructureDefinition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public abstract class StructureTemplate extends SteamNSteelStructureTE
{

    public StructureTemplate()
    {
        //noop
    }

    public StructureTemplate(StructureDefinition sd, EnumFacing orientation, boolean mirror)
    {
        super(sd, orientation, mirror);
    }

    //================================================================
    //                     I T E M   I N P U T
    //================================================================

    @Override
    public int getSizeInventory()
    {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int decrAmount)
    {
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int slotIndex)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack)
    {
        //noop
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
       //noop
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        //noop
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return false;
    }

    @Override
    public boolean canStructureInsertItem(int slot, ItemStack item, EnumFacing side, BlockPos local)
    {
        return false;
    }

    @Override
    public boolean canStructureExtractItem(int slot, ItemStack item, EnumFacing side, BlockPos local)
    {
        return false;
    }

    @Override
    public int[] getSlotsForStructureFace(EnumFacing side, BlockPos local)
    {
        return new int[0];
    }

    //================================================================
    //                  F L U I D   H A N D L E R
    //================================================================

    @Override
    public boolean canStructureFill(EnumFacing from, Fluid fluid, BlockPos local)
    {
        return false;
    }

    @Override
    public boolean canStructureDrain(EnumFacing from, Fluid fluid, BlockPos local)
    {
        return false;
    }

    @Override
    public int structureFill(EnumFacing from, FluidStack resource, boolean doFill, BlockPos local)
    {
        return 0;
    }

    @Override
    public FluidStack structureDrain(EnumFacing from, FluidStack resource, boolean doDrain, BlockPos local)
    {
        return null;
    }

    @Override
    public FluidStack structureDrain(EnumFacing from, int maxDrain, boolean doDrain, BlockPos local)
    {
        return null;
    }

    @Override
    public FluidTankInfo[] getStructureTankInfo(EnumFacing from, BlockPos local)
    {
        return emptyFluidTankInfo;
    }
    //================================================================
    //                 P I P E   C O N E C T I O N
    //================================================================

    @Override
    public boolean isStructureSideConnected(EnumFacing opposite, BlockPos local)
    {
        return false;
    }

    @Override
    public boolean tryStructureConnect(EnumFacing opposite, BlockPos local)
    {
        return false;
    }

    @Override
    public boolean canStructureConnect(EnumFacing opposite, BlockPos local)
    {
        return false;
    }

    @Override
    public void disconnectStructure(EnumFacing opposite, BlockPos local)
    {
        //noop
    }

    //================================================================
    //                            N B T
    //================================================================

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
    }

    @Override
    protected void transformDirectionsOnLoad(StructureDefinition sd)
    {
        //noop
    }
}
