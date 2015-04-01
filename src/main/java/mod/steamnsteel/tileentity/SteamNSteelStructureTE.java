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

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.inventory.Inventory;
import mod.steamnsteel.structure.IStructure.IPatternHolder;
import mod.steamnsteel.structure.IStructureTE;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.registry.StructureBlockSideAccess;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.isMirrored;
import static mod.steamnsteel.structure.coordinates.StructureBlockCoord.toStringNeighbour;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.registry.StructureDefinition.dehashLoc;
import static mod.steamnsteel.structure.registry.StructureDefinition.hashLoc;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public abstract class SteamNSteelStructureTE extends SteamNSteelTE implements IStructureTE, ISidedInventory, IPatternHolder
{
    private static final int maskBlockID = 0xFFFFFF;

    private static final byte shiftNeighbourBlocks = Byte.SIZE * 3;

    private static final String BLOCK_INFO = "blockINFO";
    private static final String BLOCK_PATTERN_NAME = "blockPatternHash";

    private byte neighbourBlocks = 0x0;
    private ImmutableTriple<Byte, Byte, Byte> blockID = ImmutableTriple.of((byte)0,(byte)0,(byte)0);
    private int blockPatternHash = "".hashCode();

    private Optional<AxisAlignedBB> renderBounds = Optional.absent();
    private boolean checkForMasterBlockType = true;
    private SteamNSteelStructureBlock structurePatternBlock = null;

    protected Optional<ImmutableTriple<Integer, Integer, Integer>> masterLocation = Optional.absent();


    protected abstract boolean hasSharedInventory();
    protected abstract Inventory getSharedInventory();

    //todo marked for Liquid/Steam management later on.
    //protected abstract boolean hasSharedLiquidStorage();
    //protected abstract ------- getSharedLiquidStorage();

    @Override
    public StructureDefinition getPattern()
    {
        return getMasterBlockInstance() != null ?
                structurePatternBlock.getPattern() :
                StructureDefinition.MISSING_STRUCTURE;
    }

    public SteamNSteelStructureBlock getMasterBlockInstance()
    {
        if (checkForMasterBlockType)
        {
            final SteamNSteelStructureBlock block = StructureRegistry.getBlock(blockPatternHash);

            if (block != null)
                structurePatternBlock = block;

            checkForMasterBlockType = false;
        }

        return structurePatternBlock;
    }

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
        blockPatternHash = nbt.getInteger(BLOCK_PATTERN_NAME);

        blockID = dehashLoc(blockInfo & maskBlockID);
        neighbourBlocks = (byte)(blockInfo >>> shiftNeighbourBlocks);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_INFO, getBlockID() | neighbourBlocks << shiftNeighbourBlocks);
        nbt.setInteger(BLOCK_PATTERN_NAME, blockPatternHash);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        if (!renderBounds.isPresent())
        {
            final Orientation o = getdecodedOrientation(getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord));

            final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) getBlockType();
            final StructureDefinition pattern = block.getPattern();

            renderBounds = pattern == StructureDefinition.MISSING_STRUCTURE ?
                    Optional.of(INFINITE_EXTENT_AABB) :
                    Optional.of(SteamNSteelStructureBlock.getBoundingBoxUsingPattern(xCoord,yCoord,zCoord,pattern,o));
        }

        return renderBounds.get();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return getSideAccess(side).getAccessibleSlotsFromSide();
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side)
    {
        return getSideAccess(side).canInsertItem();
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side)
    {
        return getSideAccess(side).canExtractItem();
    }

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
    public void configureBlock(StructureBlockCoord sBlock)
    {
        setBlockID(sBlock.getLX(),sBlock.getLY(),sBlock.getLZ());

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            if (sBlock.hasGlobalNeighbour(d))
                setNeighbour(d);
    }

    @Override
    public void setBlockPattern(String name)
    {
        blockPatternHash = name.hashCode();
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
        final int meta = getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord);

        return localToGlobal(
                getPattern().getBlockMetadata(
                        blockID.getLeft(),
                        blockID.getMiddle(),
                        blockID.getRight()
                ),
                getTransmutedBlock(),
                getdecodedOrientation(meta),
                isMirrored(meta)
        );
    }

    public ImmutableTriple<Integer, Integer, Integer> getMasterLocation()
    {
        //if (!masterLocation.isPresent())
            return getMasterLocation(getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord));

        //return masterLocation.get();
    }

    public ImmutableTriple<Integer, Integer, Integer> getMasterLocation(int meta)
    {
        //if (!masterLocation.isPresent())
        {
            final Orientation o = getdecodedOrientation(meta);
            final boolean isMirrored = isMirrored(meta);

            return getMasterLocation(o,isMirrored);
        }

        //return masterLocation.get();
    }

    public ImmutableTriple<Integer, Integer, Integer> getMasterLocation(Orientation o, boolean isMirrored)
    {
        //if (!masterLocation.isPresent())
        {
            final ImmutableTriple<Integer, Integer, Integer> size = getPattern().getBlockBounds();
            masterLocation = Optional.of(localToGlobal(
                    blockID.getLeft(), blockID.getMiddle(), blockID.getRight(),
                    xCoord, yCoord, zCoord,
                    o,
                    isMirrored,
                    size.getRight()
            ));
        }

        return masterLocation.get();
    }

    private void setBlockID(int x, int y, int z)
    {
        blockID = ImmutableTriple.of(
                (byte) x,
                (byte) y,
                (byte) z
        );
    }

    public int getBlockID()
    {
        return hashLoc(
                blockID.getLeft(),
                blockID.getMiddle(),
                blockID.getRight()
        );
    }

    private void setNeighbour(ForgeDirection d)
    {
        neighbourBlocks |= d.flag;
    }

    @Override
    public boolean hasNeighbour(ForgeDirection d)
    {
        return (neighbourBlocks & d.flag) != 0;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("neighbourBlocks", toStringNeighbour(neighbourBlocks))
                .add("blockID", blockID)
                .add("renderBounds", renderBounds)
                .add("blockPatternHash", blockPatternHash)
                .add("masterLocation", masterLocation)
                .add("structurePattern", structurePatternBlock)
                .toString();
    }
}
