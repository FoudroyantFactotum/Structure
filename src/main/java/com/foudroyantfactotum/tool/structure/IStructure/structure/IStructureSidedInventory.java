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
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

public interface IStructureSidedInventory extends ISidedInventory, IStructureTE
{
    boolean canStructureInsertItem(int slot, ItemStack item, EnumFacing side, BlockPos local);
    boolean canStructureExtractItem(int slot, ItemStack item, EnumFacing side, BlockPos local);

    int[] getSlotsForStructureFace(EnumFacing side, BlockPos local);

    //================================================================
    //         D e f a u l t   S i d e d   I n v e n t o r y
    //================================================================

    @Override
    default boolean canInsertItem(int slotIndex, ItemStack itemStack, EnumFacing side)
    {
        return canStructureInsertItem(slotIndex, itemStack, side, getLocal());
    }

    @Override
    default boolean canExtractItem(int slotIndex, ItemStack itemStack, EnumFacing side)
    {
        return canStructureExtractItem(slotIndex, itemStack, side, getLocal());
    }

    @Override
    default int[] getSlotsForFace(EnumFacing side)
    {
        return getSlotsForStructureFace(side, getLocal());
    }
}
