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
import mod.steamnsteel.structure.IStructure.IStructureSidedInventory;
import mod.steamnsteel.structure.IStructureTE;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.registry.StructureBlockSideAccess;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureNeighbours;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.isMirrored;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.registry.StructureDefinition.dehashLoc;
import static mod.steamnsteel.structure.registry.StructureDefinition.hashLoc;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public abstract class SteamNSteelStructureTE extends SteamNSteelTE implements IStructureTE, IStructureSidedInventory
{
    public static final ImmutableTriple<Byte,Byte,Byte> ORIGIN_ZERO = ImmutableTriple.of((byte)0,(byte)0,(byte)0);
    static final int maskBlockID = 0xFFFFFF;
    static final byte shiftNeighbourBlocks = Byte.SIZE * 3;

    static final String BLOCK_INFO = "blockINFO";
    static final String BLOCK_PATTERN_NAME = "blockPatternHash";

    private ImmutableTriple<Byte, Byte, Byte> blockID = ORIGIN_ZERO;
    private int patternHash = -1;

    private Optional<AxisAlignedBB> renderBounds = Optional.absent();
    private StructureNeighbours neighbours = StructureNeighbours.MISSING_NEIGHBOURS;


    //================================================================
    //                 S T R U C T U R E   C O N F I G
    //================================================================

    @Override
    public SteamNSteelStructureBlock getMasterBlockInstance()
    {
        return StructureRegistry.getBlock(patternHash);
    }

    @Override
    public ImmutableTriple<Integer,Integer,Integer> getMasterLocation(int meta)
    {
        return ImmutableTriple.of(xCoord, yCoord, zCoord);
    }

    @Override
    public int getRegHash()
    {
        return patternHash;
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
    public void configureBlock(StructureBlockCoord sBlock, int patternHash)
    {
        neighbours = new StructureNeighbours(sBlock);
        this.patternHash = patternHash;
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

    @Override
    public ImmutableTriple<Byte, Byte, Byte> getBlockID()
    {
        return blockID;
    }


    public int getHashedBlockID()
    {
        return hashLoc(
                blockID.getLeft(),
                blockID.getMiddle(),
                blockID.getRight()
        );
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
        return canStructreInsertItem(slotIndex, itemStack, side, ORIGIN_ZERO);
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side)
    {
        return canStructreExtractItem(slotIndex, itemStack, side, ORIGIN_ZERO);
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return slotIndex >= 0 && slotIndex < 4;
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
        neighbours =  new StructureNeighbours((byte)(blockInfo >>> shiftNeighbourBlocks));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_INFO, getHashedBlockID() | neighbours.hashCode() << shiftNeighbourBlocks);
        nbt.setInteger(BLOCK_PATTERN_NAME, patternHash);
    }

    //================================================================
    //                    C L I E N T   S I D E
    //================================================================

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
                    Optional.of(localToGlobal(xCoord, yCoord,zCoord, blockID, getPattern(), o, false));
        }

        return renderBounds.get();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("neighbourBlocks", neighbours)
                .add("blockID", blockID)
                .add("renderBounds", renderBounds)
                .add("blockPatternHash", patternHash)
                .toString();
    }
}
