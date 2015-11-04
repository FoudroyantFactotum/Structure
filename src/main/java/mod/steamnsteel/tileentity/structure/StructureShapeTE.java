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
import com.google.common.base.Optional;
import mod.steamnsteel.api.plumbing.IPipeTileEntity;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.IStructure.IStructureTE;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.tileentity.SteamNSteelTE;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.isMirrored;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE.*;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public final class StructureShapeTE extends SteamNSteelTE implements IStructureTE, ISidedInventory, IFluidHandler, IPipeTileEntity
{
    private TripleCoord local = TripleCoord.of(0,0,0);
    private int definitionHash = -1;

    private Optional<TripleCoord> masterLocation = Optional.absent();
    private Optional<SteamNSteelStructureTE> originTE = Optional.absent();
    private boolean hasNotAttemptedAcquisitionOfOriginTE = true;

    private SteamNSteelStructureTE getOriginTE()
    {
        return originTE.get();
    }

    private boolean hasOriginTE()
    {
        if (originTE.isPresent())
        {
            if (!originTE.get().isInvalid())
            {
                return true;
            }
        }

        //get te and test
        final TripleCoord mloc = getMasterBlockLocation();
        final TileEntity te = getWorldObj()
                .getTileEntity(mloc.x, mloc.y, mloc.z);

        if (hasNotAttemptedAcquisitionOfOriginTE && te instanceof SteamNSteelStructureTE)
        {
            if (!te.isInvalid())
            {
                originTE = Optional.of((SteamNSteelStructureTE) te);
                return true;
            }
        }

        hasNotAttemptedAcquisitionOfOriginTE = false;
        return false;
    }

    //================================================================
    //                 S T R U C T U R E   C O N F I G
    //================================================================

    @Override
    public int getRegHash()
    {
        return definitionHash;
    }

    @Override
    public SteamNSteelStructureBlock getMasterBlockInstance()
    {
        return StructureRegistry.getStructureBlock(definitionHash);
    }

    public TripleCoord getMasterBlockLocation()
    {
        if (!masterLocation.isPresent())
        {
            final int meta = getBlockMetadata();
            final SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(definitionHash);

            if (sb == null)
            {
                return TripleCoord.of(xCoord, yCoord, zCoord);
            }

            masterLocation = Optional.of(localToGlobal(
                    -local.x, -local.y, -local.z,
                    xCoord, yCoord, zCoord,
                    getdecodedOrientation(meta), isMirrored(meta),
                    sb.getPattern().getBlockBounds()));
        }

        return masterLocation.get();
    }

    @Override
    public Block getTransmutedBlock()
    {
        SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(definitionHash);

        if (sb != null)
        {
            Block block = sb.getPattern().getBlock(local);
            return block == null ?
                    Blocks.air :
                    block;
        }

        return Blocks.air;
    }

    @Override
    public int getTransmutedMeta()
    {
        final SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(definitionHash);

        if (sb != null)
        {
            final int meta = getBlockMetadata();

            return localToGlobal(
                    sb.getPattern().getBlockMetadata(local),
                    getTransmutedBlock(),
                    getdecodedOrientation(meta),
                    isMirrored(meta)
            );
        }

        return 0;
    }

    @Override
    public void configureBlock(TripleCoord local, int patternHash)
    {
        this.local = local;
        this.definitionHash = patternHash;
    }

    @Override
    public TripleCoord getLocal()
    {
        return local;
    }


    //================================================================
    //                     I T E M   I N P U T
    //================================================================

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return hasOriginTE()?
                getOriginTE().getAccessibleSlotsFromStructureSide(side, local):
                new int[0];
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side)
    {
        return hasOriginTE() && getOriginTE()
                .canStructureInsertItem(slotIndex, itemStack, side, local);
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side)
    {
        return hasOriginTE() && getOriginTE()
                .canStructureExtractItem(slotIndex, itemStack, side, local);
    }

    @Override
    public int getSizeInventory()
    {
        if (hasOriginTE())
        {
            return getOriginTE().getSizeInventory();
        }

        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        if (hasOriginTE())
        {
            return getOriginTE().getStackInSlot(slotIndex);
        }

        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int decrAmount)
    {
        if (hasOriginTE())
        {
            return getOriginTE().decrStackSize(slotIndex, decrAmount);
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        if (hasOriginTE())
        {
            return getOriginTE().getStackInSlotOnClosing(slotIndex);
        }

        return null;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack)
    {
        if (hasOriginTE())
        {
            getOriginTE().setInventorySlotContents(slotIndex, itemStack);
        }
    }

    @Override
    public String getInventoryName()
    {
        if (hasOriginTE())
        {
            return getOriginTE().getInventoryName();
        }

        return "";
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return hasOriginTE() && getOriginTE().hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (hasOriginTE())
        {
            return getOriginTE().getInventoryStackLimit();
        }

        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return hasOriginTE() && getOriginTE().isUseableByPlayer(player);
    }

    @Override
    public void openInventory()
    {
        if (hasOriginTE())
        {
            getOriginTE().openInventory();
        }
    }

    @Override
    public void closeInventory()
    {
        if (hasOriginTE())
        {
            getOriginTE().closeInventory();
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return hasOriginTE() && getOriginTE().isItemValidForSlot(slotIndex, itemStack);
    }

    //================================================================
    //                  F L U I D   H A N D L E R
    //================================================================

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (hasOriginTE())
        {
            return getOriginTE().structureFill(from, resource, doFill, local);
        }

        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (hasOriginTE())
        {
            return getOriginTE().structureDrain(from, resource, doDrain, local);
        }

        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        if (hasOriginTE())
        {
            return getOriginTE().structureDrain(from, maxDrain, doDrain, local);
        }

        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return hasOriginTE() && getOriginTE().canStructureFill(from, fluid, local);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return hasOriginTE() && getOriginTE().canStructureDrain(from, fluid, local);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        if (hasOriginTE())
        {
            return getOriginTE().getStructureTankInfo(from, local);
        }

        return emptyFluidTankInfo;
    }

    //================================================================
    //                 P I P E   C O N E C T I O N
    //================================================================

    @Override
    public boolean isSideConnected(ForgeDirection opposite)
    {
        return hasOriginTE() && getOriginTE().isStructureSideConnected(opposite, local);
    }

    @Override
    public boolean tryConnect(ForgeDirection opposite)
    {
        return hasOriginTE() && getOriginTE().tryStructureConnect(opposite, local);
    }

    @Override
    public boolean canConnect(ForgeDirection opposite)
    {
        return hasOriginTE() && getOriginTE().canStructureConnect(opposite, local);
    }

    @Override
    public void recalculateVisuals()
    {
        //noop
    }

    @Override
    public void disconnect(ForgeDirection opposite)
    {
        if (hasOriginTE())
        {
            getOriginTE().disconnectStructure(opposite, local);
        }
    }

    //================================================================
    //                            N B T
    //================================================================

    @Override
    public Packet getDescriptionPacket()
    {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        readFromNBT(packet.func_148857_g());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        final int blockInfo = nbt.getInteger(BLOCK_INFO);
        definitionHash = nbt.getInteger(BLOCK_PATTERN_NAME);

        local = TripleCoord.dehashLoc(blockInfo & maskBlockID);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_INFO, local.hashCode());
        nbt.setInteger(BLOCK_PATTERN_NAME, definitionHash);
    }

    //================================================================
    //                          C l a s s
    //================================================================

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("local", local)
                .add("definitionHash", definitionHash)
                .add("masterLocation", masterLocation)
                .add("originTE", originTE)
                .add("hasNotAttemptedAcquisitionOfOriginTE", hasNotAttemptedAcquisitionOfOriginTE)
                .toString();
    }
}
