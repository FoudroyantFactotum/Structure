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
package mod.steamnsteel.tileentity;

import com.google.common.base.Optional;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.IStructureTE;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.registry.StructureBlockSideAccess;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureNeighbours;
import mod.steamnsteel.structure.registry.StructureRegistry;
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
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.isMirrored;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.registry.StructureDefinition.dehashLoc;
import static mod.steamnsteel.structure.registry.StructureDefinition.hashLoc;
import static mod.steamnsteel.tileentity.SteamNSteelStructureTE.*;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public final class StructureShapeTE extends SteamNSteelTE implements IStructureTE, ISidedInventory
{
    private ImmutableTriple<Byte, Byte, Byte> blockID = ImmutableTriple.of((byte)0,(byte)0,(byte)0);
    private StructureNeighbours neighbours = StructureNeighbours.MISSING_NEIGHBOURS;
    private int patternHash = -1;

    private Optional<ImmutableTriple<Integer,Integer,Integer>> masterLocation = Optional.absent();
    private Optional<SteamNSteelStructureTE> originTE = Optional.absent();
    private boolean hasNotAttempedAqusitionOfOriginTE = true;

    private SteamNSteelStructureTE getOriginTE()
    {
        return originTE.get();
    }

    private boolean hasOriginTE()
    {
        if (originTE.isPresent())
            if (!originTE.get().isInvalid())
                return true;

        //get te and test
        final ImmutableTriple<Integer,Integer,Integer> mloc = getMasterBlockLocation();
        final TileEntity te = getWorldObj()
                .getTileEntity(mloc.getLeft(), mloc.getMiddle(), mloc.getRight());

        if (hasNotAttempedAqusitionOfOriginTE && te instanceof SteamNSteelStructureTE)
            if (!te.isInvalid())
            {
                originTE = Optional.of((SteamNSteelStructureTE) te);
                return true;
            }

        hasNotAttempedAqusitionOfOriginTE = false;
        return false;
    }

    //================================================================
    //                 S T R U C T U R E   C O N F I G
    //================================================================

    @Override
    public int getRegHash()
    {
        return patternHash;
    }

    @Override
    public ImmutableTriple<Integer, Integer, Integer> getMasterLocation(int meta)
    {
        return localToGlobal(
                blockID.getLeft(), blockID.getMiddle(), blockID.getRight(),
                xCoord, yCoord, zCoord,
                getdecodedOrientation(meta), isMirrored(meta),
                getPattern()
        );
    }

    @Override
    public SteamNSteelStructureBlock getMasterBlockInstance()
    {
        return StructureRegistry.getBlock(patternHash);
    }

    public ImmutableTriple<Integer,Integer,Integer> getMasterBlockLocation()
    {
        if (!masterLocation.isPresent())
        {
            final int meta = getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord);

            masterLocation = Optional.of(localToGlobal(
                    blockID.getLeft(), blockID.getMiddle(), blockID.getRight(),
                    xCoord, yCoord, zCoord,
                    getdecodedOrientation(meta), isMirrored(meta),
                    getPattern()));
        }

        return masterLocation.get();
    }

    @Override
    public StructureDefinition getPattern()
    {
        final SteamNSteelStructureBlock block = StructureRegistry.getBlock(patternHash);

        return block == null? StructureDefinition.MISSING_STRUCTURE : block.getPattern();
    }

    @Override
    public StructureNeighbours getNeighbours()
    {
        return neighbours;
    }

    @Override
    public Block getTransmutedBlock()
    {
        final Block block = getPattern().getBlock(blockID.getLeft(), blockID.getMiddle(), blockID.getRight());
        return block == null ?
                Blocks.air :
                block;
    }

    @Override
    public int getTransmutedMeta()
    {
        return localToGlobal(
                getPattern().getBlockMetadata(blockID.getLeft(), blockID.getMiddle(), blockID.getRight()),
                getTransmutedBlock(),
                getdecodedOrientation(getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord)),
                false// :/ mirroring
        );
    }

    @Override
    public void configureBlock(StructureBlockCoord sBlock, int patternHash)
    {
        neighbours = new StructureNeighbours(sBlock);
        blockID = sBlock.getLocal();
        this.patternHash = patternHash;
    }

    @Override
    public ImmutableTriple<Byte, Byte, Byte> getBlockID()
    {
        return blockID;
    }


    //================================================================
    //                     I T E M   I N P U T
    //================================================================

    private StructureBlockSideAccess getSideAccess(int side)
    {//todo cache?
        final int meta = getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord);

        return getPattern().getSideAccess(
                blockID.getLeft(),
                blockID.getMiddle(),
                blockID.getRight(),
                localToGlobal(
                        ForgeDirection.values()[side],
                        getdecodedOrientation(meta),
                        isMirrored(meta)
                )
        );
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return getSideAccess(side).getAccessibleSlotsFromSide();
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side)
    {
        return hasOriginTE() && getOriginTE()
                .canStructreInsertItem(slotIndex, itemStack, side, blockID);
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side)
    {
        return hasOriginTE() && getOriginTE()
                .canStructreExtractItem(slotIndex, itemStack, side, blockID);
    }

    @Override
    public int getSizeInventory()
    {
        if (hasOriginTE())
            return getOriginTE().getSizeInventory();

        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        if (hasOriginTE())
            return getOriginTE().getStackInSlot(slotIndex);

        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int decrAmount)
    {
        if (hasOriginTE())
            return getOriginTE().decrStackSize(slotIndex, decrAmount);

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        if (hasOriginTE())
            return getOriginTE().getStackInSlotOnClosing(slotIndex);

        return null;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack)
    {
        if (hasOriginTE())
            getOriginTE().setInventorySlotContents(slotIndex, itemStack);
    }

    @Override
    public String getInventoryName()
    {
        if (hasOriginTE())
            return getOriginTE().getInventoryName();

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
            return getOriginTE().getInventoryStackLimit();

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
            getOriginTE().openInventory();
    }

    @Override
    public void closeInventory()
    {
        if (hasOriginTE())
            getOriginTE().closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return hasOriginTE() && getOriginTE().isItemValidForSlot(slotIndex, itemStack);
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
        patternHash = nbt.getInteger(BLOCK_PATTERN_NAME);

        blockID = dehashLoc(blockInfo & maskBlockID);
        neighbours = new StructureNeighbours ((byte)(blockInfo >>> shiftNeighbourBlocks));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_INFO, hashLoc(blockID.getLeft(),blockID.getMiddle(),blockID.getRight()) | neighbours.hashCode() << shiftNeighbourBlocks);
        nbt.setInteger(BLOCK_PATTERN_NAME, patternHash);
    }
}
