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


import mod.steamnsteel.inventory.Inventory;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.registry.StructureDefinition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import static mod.steamnsteel.structure.coordinates.TransformLAG.*;

public class BlastFurnaceTE extends SteamNSteelStructureTE
{
    private static final TripleCoord LOCATION_STEAM_INPUT = TripleCoord.of(1,1,0);
    private static final int DIRECTIONS_STEAM_INPUT = flagEnumFacing(EnumFacing.NORTH);

    private static final TripleCoord LOCATION_MATERIAL_INPUT = TripleCoord.of(1,2,1);
    private static final int DIRECTIONS_MATERIAL_INPUT = flagEnumFacing(EnumFacing.UP);

    private static final TripleCoord LOCATION_METAL_OUTPUT = TripleCoord.of(1,2,1);
    private static final int DIRECTIONS_METAL_OUTPUT = flagEnumFacing(EnumFacing.UP);

    private static final TripleCoord LOCATION_SLAG_OUTPUT = TripleCoord.of(1,2,1);
    private static final int DIRECTIONS_SLAG_OUTPUT = flagEnumFacing(EnumFacing.UP);

    //Global Directions
    private int globalDirectionsSteamInput;
    private int globalDirectionsMaterialInput;
    private int globalDirectionsMetalOutput;
    private int globalDirectionsSlagOutput;

    private TripleCoord globalLocationSteamInput;
    private TripleCoord globalLocationMaterialInput;
    private TripleCoord globalLocationMetalOutput;
    private TripleCoord globalLocationSlagOutput;

    private final Inventory inventory = new Inventory(1);
    private static final int INPUT = 0;
    private static final int[] slotsDefault = {};
    private static final int[] slotsMaterialInput = {INPUT};

    public BlastFurnaceTE()
    {
        //noop
    }

    public BlastFurnaceTE(StructureDefinition sd, EnumFacing orientation, boolean mirror)
    {
        super(sd, orientation, mirror);
    }

    //================================================================
    //                     I T E M   I N P U T
    //================================================================

    @Override
    public String getCommandSenderName()
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
    public void openInventory(EntityPlayer player)
    {

    }

    @Override
    public void closeInventory(EntityPlayer player)
    {

    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return slotIndex == INPUT;
    }

    @Override
    public boolean canStructureInsertItem(int slot, ItemStack item, EnumFacing side, TripleCoord blockID)
    {
        return isSide(globalDirectionsMaterialInput, side) &&
                blockID.equals(LOCATION_MATERIAL_INPUT) &&
                isItemValidForSlot(slot, item);
    }

    @Override
    public boolean canStructureExtractItem(int slot, ItemStack item, EnumFacing side, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public int[] getSlotsForStructureFace(EnumFacing side, TripleCoord blockID)
    {
        return LOCATION_MATERIAL_INPUT.equals(blockID)?
                slotsMaterialInput :
                slotsDefault;
    }

    //================================================================
    //                  F L U I D   H A N D L E R
    //================================================================

    @Override
    public boolean canStructureFill(EnumFacing from, Fluid fluid, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public boolean canStructureDrain(EnumFacing from, Fluid fluid, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public int structureFill(EnumFacing from, FluidStack resource, boolean doFill, TripleCoord blockID)
    {
        return 0;
    }

    @Override
    public FluidStack structureDrain(EnumFacing from, FluidStack resource, boolean doDrain, TripleCoord blockID)
    {
        return null;
    }

    @Override
    public FluidStack structureDrain(EnumFacing from, int maxDrain, boolean doDrain, TripleCoord blockID)
    {
        return null;
    }

    @Override
    public FluidTankInfo[] getStructureTankInfo(EnumFacing from, TripleCoord blockID)
    {
        return emptyFluidTankInfo;
    }

    //================================================================
    //                 P I P E   C O N E C T I O N
    //================================================================

    @Override
    public boolean isStructureSideConnected(EnumFacing opposite, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public boolean tryStructureConnect(EnumFacing opposite, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public boolean canStructureConnect(EnumFacing opposite, TripleCoord blockID)
    {
        return isSide(globalDirectionsSteamInput, opposite) && LOCATION_STEAM_INPUT.equals(blockID);
    }

    @Override
    public void disconnectStructure(EnumFacing opposite, TripleCoord blockID)
    {

    }

    //================================================================
    //                            N B T
    //================================================================
    @Override
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
    protected void transformDirectionsOnLoad(StructureDefinition sd)
    {
        globalDirectionsSteamInput    = localToGlobalDirection(DIRECTIONS_STEAM_INPUT,    orientation, mirror);
        globalDirectionsMaterialInput = localToGlobalDirection(DIRECTIONS_MATERIAL_INPUT, orientation, mirror);
        globalDirectionsMetalOutput   = localToGlobalDirection(DIRECTIONS_METAL_OUTPUT,   orientation, mirror);
        globalDirectionsSlagOutput    = localToGlobalDirection(DIRECTIONS_SLAG_OUTPUT,    orientation, mirror);

        globalLocationSteamInput    = transformFromDefinitionToMaster(sd, LOCATION_STEAM_INPUT);
        globalLocationMaterialInput = transformFromDefinitionToMaster(sd, LOCATION_MATERIAL_INPUT);
        globalLocationMetalOutput   = transformFromDefinitionToMaster(sd, LOCATION_METAL_OUTPUT);
        globalLocationSlagOutput    = transformFromDefinitionToMaster(sd, LOCATION_SLAG_OUTPUT);
    }


}
