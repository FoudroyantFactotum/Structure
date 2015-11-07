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
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.SteamNSteelTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobalDirection;
import static mod.steamnsteel.structure.coordinates.TransformLAG.transformFromDefinitionToMaster;

public class BallMillTE extends SteamNSteelStructureTE
{
    private static final TripleCoord LOCATION_STEAM_INPUT = TripleCoord.of(0,0,1);
    private static final int DIRECTIONS_STEAM_INPUT = ForgeDirection.SOUTH.flag;

    private static final TripleCoord LOCATION_WATER_INPUT = TripleCoord.of(4,1,1);
    private static final int DIRECTIONS_WATER_INPUT = ForgeDirection.EAST.flag;

    private static final TripleCoord LOCATION_MATERIAL_INPUT = TripleCoord.of(0,0,0);
    private static final int DIRECTIONS_MATERIAL_INPUT = ForgeDirection.NORTH.flag;

    private static final TripleCoord LOCATION_MATERIAL_OUTPUT = TripleCoord.of(4,0,0);
    private static final int DIRECTIONS_MATERIAL_OUTPUT = ForgeDirection.NORTH.flag;

    //Global Directions
    private int globalDirectionsSteamInput;
    private int globalDirectionsWaterInput;
    private int globalDirectionsMaterialInput;
    private int globalDirectionsMaterialOutput;

    private TripleCoord globalLocationSteamInput;
    private TripleCoord globalLocationWaterInput;
    private TripleCoord globalLocationMaterialInput;
    private TripleCoord globalLocationMaterialOutput;

    private final Inventory inventory = new Inventory(1);
    private static final int INPUT = 0;
    private static final int[] slotsDefault = {};
    private static final int[] slotsMaterialInput = {INPUT};

    public BallMillTE()
    {
        //noop
    }

    public BallMillTE(int meta, StructureDefinition sd)
    {
        super(meta, sd);
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
        return slotIndex == INPUT;
    }

    @Override
    public boolean canStructureInsertItem(int slot, ItemStack item, int side, TripleCoord blockID)
    {
        return isSide(globalDirectionsMaterialInput, side) &&
                blockID.equals(globalLocationMaterialInput) &&
                isItemValidForSlot(slot, item);
    }

    @Override
    public boolean canStructureExtractItem(int slot, ItemStack item, int side, TripleCoord blockID)
    {
        return slot == INPUT && isSide(globalDirectionsMaterialOutput, side);
    }

    @Override
    public int[] getAccessibleSlotsFromStructureSide(int side, TripleCoord blockID)
    {
        return globalLocationMaterialInput.equals(blockID) || globalLocationMaterialOutput.equals(blockID) ?
                slotsMaterialInput :
                slotsDefault;
    }

    //================================================================
    //                  F L U I D   H A N D L E R
    //================================================================

    @Override
    public boolean canStructureFill(ForgeDirection from, Fluid fluid, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public boolean canStructureDrain(ForgeDirection from, Fluid fluid, TripleCoord blockID)
    {
        return false;
    }

    @Override
    public int structureFill(ForgeDirection from, FluidStack resource, boolean doFill, TripleCoord blockID)
    {
        return 0;
    }

    @Override
    public FluidStack structureDrain(ForgeDirection from, FluidStack resource, boolean doDrain, TripleCoord blockID)
    {
        return null;
    }

    @Override
    public FluidStack structureDrain(ForgeDirection from, int maxDrain, boolean doDrain, TripleCoord blockID)
    {
        return null;
    }

    @Override
    public FluidTankInfo[] getStructureTankInfo(ForgeDirection from, TripleCoord blockID)
    {
        return emptyFluidTankInfo;
    }

    //================================================================
    //                 P I P E   C O N E C T I O N
    //================================================================

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
        return isSide(globalDirectionsSteamInput, opposite) && globalLocationSteamInput.equals(blockID) ||
                isSide(globalDirectionsWaterInput, opposite) && globalLocationWaterInput.equals(blockID);
    }

    @Override
    public void disconnectStructure(ForgeDirection opposite, TripleCoord blockID)
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
        globalDirectionsSteamInput     = localToGlobalDirection(DIRECTIONS_STEAM_INPUT,     getBlockMetadata());
        globalDirectionsWaterInput     = localToGlobalDirection(DIRECTIONS_WATER_INPUT,     getBlockMetadata());
        globalDirectionsMaterialInput  = localToGlobalDirection(DIRECTIONS_MATERIAL_INPUT,  getBlockMetadata());
        globalDirectionsMaterialOutput = localToGlobalDirection(DIRECTIONS_MATERIAL_OUTPUT, getBlockMetadata());

        globalLocationSteamInput     = transformFromDefinitionToMaster(sd, LOCATION_STEAM_INPUT);
        globalLocationWaterInput     = transformFromDefinitionToMaster(sd, LOCATION_WATER_INPUT);
        globalLocationMaterialInput  = transformFromDefinitionToMaster(sd, LOCATION_MATERIAL_INPUT);
        globalLocationMaterialOutput = transformFromDefinitionToMaster(sd, LOCATION_MATERIAL_OUTPUT);
    }
}
