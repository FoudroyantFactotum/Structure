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

import com.foudroyantfactotum.tool.structure.IStructure.structure.IStructureSidedInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;

public interface IStructureShapeSidedInventory<E extends IStructureSidedInventory> extends ISidedInventory, IStructureShapeTE<E>
{
    @Override
    default int[] getSlotsForFace(EnumFacing side)
    {
        return hasOriginTE() ?
                getOriginTE().getSlotsForStructureFace(side, getLocal()) :
                new int[0];
    }

    @Override
    default boolean canInsertItem(int slotIndex, ItemStack itemStack, EnumFacing side)
    {
        return hasOriginTE() && getOriginTE()
                .canStructureInsertItem(slotIndex, itemStack, side, getLocal());
    }

    @Override
    default boolean canExtractItem(int slotIndex, ItemStack itemStack, EnumFacing side)
    {
        return hasOriginTE() && getOriginTE()
                .canStructureExtractItem(slotIndex, itemStack, side, getLocal());
    }

    @Override
    default int getSizeInventory()
    {
        if (hasOriginTE())
        {
            return getOriginTE().getSizeInventory();
        }

        return 0;
    }

    @Override
    default ItemStack getStackInSlot(int slotIndex)
    {
        if (hasOriginTE())
        {
            return getOriginTE().getStackInSlot(slotIndex);
        }

        return null;
    }

    @Override
    default ItemStack decrStackSize(int slotIndex, int decrAmount)
    {
        if (hasOriginTE())
        {
            return getOriginTE().decrStackSize(slotIndex, decrAmount);
        }

        return null;
    }

    @Override
    default ItemStack removeStackFromSlot(int slotIndex)
    {
        if (hasOriginTE())
        {
            return getOriginTE().removeStackFromSlot(slotIndex);
        }

        return null;
    }

    @Override
    default void setInventorySlotContents(int slotIndex, ItemStack itemStack)
    {
        if (hasOriginTE())
        {
            getOriginTE().setInventorySlotContents(slotIndex, itemStack);
        }
    }

    @Override
    default IChatComponent getDisplayName()
    {
        if (hasOriginTE())
        {
            return getOriginTE().getDisplayName();
        }

        return new ChatComponentText("");
    }

    @Override
    default String getName()
    {
        return null;
    }

    @Override
    default boolean hasCustomName()
    {
        return hasOriginTE() && getOriginTE().hasCustomName();
    }

    @Override
    default int getInventoryStackLimit()
    {
        if (hasOriginTE())
        {
            return getOriginTE().getInventoryStackLimit();
        }

        return 0;
    }

    @Override
    default boolean isUseableByPlayer(EntityPlayer player)
    {
        return hasOriginTE() && getOriginTE().isUseableByPlayer(player);
    }

    @Override
    default void openInventory(EntityPlayer player)
    {
        if (hasOriginTE())
        {
            getOriginTE().openInventory(player);
        }
    }

    @Override
    default void closeInventory(EntityPlayer player)
    {
        if (hasOriginTE())
        {
            getOriginTE().closeInventory(player);
        }
    }

    @Override
    default boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return hasOriginTE() && getOriginTE().isItemValidForSlot(slotIndex, itemStack);
    }

    @Override
    default int getField(int id)
    {
        return 0;
    }

    @Override
    default void setField(int id, int value)
    {

    }

    @Override
    default int getFieldCount()
    {
        return 0;
    }

    @Override
    default void clear()
    {

    }
}
