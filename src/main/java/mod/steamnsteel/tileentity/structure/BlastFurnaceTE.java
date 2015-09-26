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
package mod.steamnsteel.tileentity.structure;

import com.google.common.base.Objects;
import mod.steamnsteel.block.structure.BlastFurnaceBlock;
import mod.steamnsteel.inventory.Inventory;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.tileentity.SteamNSteelTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public class BlastFurnaceTE extends SteamNSteelStructureTE
{//TODO complete class

    private final TripleCoord pipeConnectLocation = TripleCoord.of(1,1,0);

    public static final int INPUT = 0;
    public static final int INPUT_FUEL = 1;
    public static final int OUTPUT_TOP = 2;
    public static final int OUTPUT_BOTTOM = 3;
    public static final int INVENTORY_SIZE = 4;

    private final Inventory inventory = new Inventory(INVENTORY_SIZE);

    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        inventory.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        inventory.writeToNBT(nbt);
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.getSize();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return inventory.getStack(slotIndex);
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int decrAmount)
    {
        return inventory.decrStackSize(slotIndex, decrAmount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        return inventory.getStackOnClosing(slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack)
    {
        inventory.setSlot(slotIndex, itemStack);
    }


    @Override
    public String getInventoryName()
    {
        return SteamNSteelTE.containerName(BlastFurnaceBlock.NAME);
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return inventory.getStackSizeMax();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void openInventory()
    {
        //no op
    }

    @Override
    public void closeInventory()
    {
        //no op
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return slotIndex != OUTPUT_TOP &&
                slotIndex != OUTPUT_BOTTOM &&
                (slotIndex != INPUT_FUEL || TileEntityFurnace.isItemFuel(itemStack));
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("position", Vec3.createVectorHelper(xCoord,yCoord,zCoord))
                .add("inventory", inventory)
                .toString();
    }

    @Override
    public boolean canStructureInsertItem(int slot, ItemStack item, int side, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public boolean canStructureExtractItem(int slot, ItemStack item, int side, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromStructureSide(int side, TripleCoord blockID)
    {
        return new int[0];
    }

    @Override
    public boolean isStructureSideConnected(ForgeDirection opposite, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public boolean tryStructureConnect(ForgeDirection opposite, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public boolean canStructureConnect(ForgeDirection opposite, TripleCoord blockID)
    {
        return opposite == localToGlobal(ForgeDirection.NORTH, getdecodedOrientation(getBlockMetadata()), false) && pipeConnectLocation.equals(blockID);
    }

    @Override
    public void disconnectStructure(ForgeDirection opposite, TripleCoord blockID)
    {

    }
    //================================================================
    //                  F L U I D   H A N D L E R
    //================================================================

    public boolean canStructureFill(ForgeDirection from, Fluid fluid, TripleCoord blockID)
    {
        return false;
    }

    public boolean canStructureDrain(ForgeDirection from, Fluid fluid, TripleCoord blockID)
    {
        return false;
    }

    public int structureFill(ForgeDirection from, FluidStack resource, boolean doFill, TripleCoord blockID)
    {
        return 0;
    }

    public FluidStack structureDrain(ForgeDirection from, FluidStack resource, boolean doDrain, TripleCoord blockID)
    {
        return null;
    }

    public FluidStack structureDrain(ForgeDirection from, int maxDrain, boolean doDrain, TripleCoord blockID)
    {
        return null;
    }

    public FluidTankInfo[] getStructureTankInfo(ForgeDirection from, TripleCoord blockID)
    {
        return emptyFluidTankInfo;
    }
}
