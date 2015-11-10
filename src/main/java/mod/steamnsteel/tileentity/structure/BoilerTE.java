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

public class BoilerTE extends SteamNSteelStructureTE
{
    private static final TripleCoord LOCATION_WATER_INPUT = TripleCoord.of(1,0,1);
    private static final int DIRECTIONS_WATER_INPUT = flagEnumFacing(EnumFacing.DOWN);

    private static final TripleCoord LOCATION_STEAM_OUTPUT = TripleCoord.of(1,3,1);
    private static final int DIRECTIONS_STEAM_OUTPUT = flagEnumFacing(EnumFacing.UP);

    private static final TripleCoord LOCATION_MATERIAL_INPUT = TripleCoord.of(1,0,2);
    private static final int DIRECTIONS_MATERIAL_INPUT = flagEnumFacing(EnumFacing.SOUTH);

    //Global Directions
    private int globalDirectionsSteamOutput;
    private int globalDirectionsWaterInput;
    private int globalDirectionsMaterialInput;

    private TripleCoord globalLocationSteamOutput;
    private TripleCoord globalLocationWaterInput;
    private TripleCoord globalLocationMaterialInput;

    private final Inventory inventory = new Inventory(1);
    private static final int INPUT = 0;
    private static final int[] slotsDefault = {};
    private static final int[] slotsMaterialInput = {INPUT};

    public BoilerTE()
    {
        //noop
    }

    public BoilerTE(StructureDefinition sd, EnumFacing orientation, boolean mirror)
    {
        super(sd, orientation, mirror);
    }

    //================================================================
    //                     I T E M   I N P U T
    //================================================================

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

    /*@Override
    public String getInventoryName()
    {
        return SteamNSteelTE.containerName(BoilerBlock.NAME);
    }*/

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
        return LOCATION_MATERIAL_INPUT.equals(blockID) ?
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
        return (isSide(globalDirectionsSteamOutput, opposite) && LOCATION_STEAM_OUTPUT.equals(blockID)) ||
                (isSide(globalDirectionsWaterInput, opposite) && LOCATION_WATER_INPUT.equals(blockID));
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
        globalDirectionsSteamOutput    = localToGlobalDirection(DIRECTIONS_STEAM_OUTPUT,    orientation, mirror);
        globalDirectionsWaterInput     = localToGlobalDirection(DIRECTIONS_WATER_INPUT,     orientation, mirror);
        globalDirectionsMaterialInput  = localToGlobalDirection(DIRECTIONS_MATERIAL_INPUT,  orientation, mirror);

        globalLocationSteamOutput   = transformFromDefinitionToMaster(sd, LOCATION_STEAM_OUTPUT);
        globalLocationWaterInput    = transformFromDefinitionToMaster(sd, LOCATION_WATER_INPUT);
        globalLocationMaterialInput = transformFromDefinitionToMaster(sd, LOCATION_MATERIAL_INPUT);
    }
}
