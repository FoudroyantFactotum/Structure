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


import mod.steamnsteel.block.structure.BallMillBlock;
import mod.steamnsteel.inventory.Inventory;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.tileentity.SteamNSteelTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BallMillTE extends SteamNSteelStructureTE
{//TODO complete class
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
        return SteamNSteelTE.containerName(BallMillBlock.NAME);
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
        return slotIndex >= 0 && slotIndex < 4;
    }

    @Override
    public String toString()
    {
        return inventory.toString();
    }

    @Override
    public boolean canStructureInsertItem(int slot, ItemStack item, int side, TripleCoord blockID)
    {
        return true;
    }

    @Override
    public boolean canStructureExtractItem(int slot, ItemStack item, int side, TripleCoord blockID)
    {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromStructureSide(int side, TripleCoord blockID)
    {
        return new int[0];
    }
}
