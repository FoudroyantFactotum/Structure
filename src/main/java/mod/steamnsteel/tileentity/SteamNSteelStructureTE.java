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
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.IStructureTE;
import mod.steamnsteel.utility.structure.StructureBlockCoord;
import mod.steamnsteel.utility.structure.StructureBlockSideAccess;
import mod.steamnsteel.utility.structure.StructurePattern;
import net.minecraft.block.Block;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public abstract class SteamNSteelStructureTE extends SteamNSteelTE implements IStructureTE, ISidedInventory
{
    //todo private
    public ImmutableTriple<Integer,Integer, Integer> blockID = ImmutableTriple.of(-1,-1,-1);
    private Optional<AxisAlignedBB> renderBounds = Optional.absent();
    private int blockPatternHash = "".hashCode();
    private Optional<StructurePattern> structurePattern = Optional.absent();

    private static final String BLOCK_ID = "blockID";
    private static final String NEIGHBOUR_BLOCKS = "neighbourBlocks";
    private static final String BLOCK_PATTERN_NAME = "blockPatternHash";

    private byte neighbourBlocks = 0x0;

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
        final int[] blockIDArray = nbt.getIntArray(BLOCK_ID);
        blockID = ImmutableTriple.of(blockIDArray[0],blockIDArray[1],blockIDArray[2]);
        neighbourBlocks = nbt.getByte(NEIGHBOUR_BLOCKS);
        blockPatternHash = nbt.getInteger(BLOCK_PATTERN_NAME);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setIntArray(BLOCK_ID, new int[]{blockID.getLeft(),blockID.getMiddle(),blockID.getRight()});
        nbt.setByte(NEIGHBOUR_BLOCKS, neighbourBlocks);
        nbt.setInteger(BLOCK_PATTERN_NAME, blockPatternHash);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {//todo redo
     if (!renderBounds.isPresent())
        {
            final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) getBlockType();
            final StructurePattern pattern = block.getPattern();

            if (pattern == StructurePattern.MISSING_STRUCTURE)
            {//todo Logger.error or equiv?
                Logger.info("Missing Pattern for : " + block.getUnlocalizedName());
                renderBounds = Optional.of(INFINITE_EXTENT_AABB);
            } else
            {
                renderBounds = Optional.of(block.getSelectedBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord));
            }
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

    public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side)
    {
        return getSideAccess(side).canExtractItem();
    }

    private StructureBlockSideAccess getSideAccess(int side)
    {
        return getPattern().getSideAccess(
                blockID.getLeft(),
                blockID.getMiddle(),
                blockID.getRight(),
                ForgeDirection.values()[side],
                Orientation.getdecodedOrientation(getWorldObj().getBlockMetadata(xCoord,yCoord,zCoord)));
    }

    public StructurePattern getPattern()
    {
        if (!structurePattern.isPresent())
        {
            structurePattern = Optional.of(StructurePattern.getPattern(blockPatternHash));
        }

        return structurePattern.get();
    }

    @Override
    public void configureBlock(StructureBlockCoord sBlock)
    {
        setBlockID(sBlock.getLX(),sBlock.getLY(),sBlock.getLZ());

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS) if (sBlock.hasGlobalNeighbour(d)) setNeighbour(d);
    }

    public void setBlockPattern(String name)
    {
        blockPatternHash = name.hashCode();
    }

    @Override
    public Block getTransmutedBlock()
    {
        return getPattern().getBlock(blockID.getLeft(),blockID.getMiddle(),blockID.getRight());
    }

    @Override
    public int getTransmutedMeta()
    {
        final int meta = getWorldObj().getBlockMetadata(xCoord,yCoord,zCoord);
        return getPattern().getBlockMetadata(
                blockID.getLeft(),
                blockID.getMiddle(),
                blockID.getRight(),
                Orientation.getdecodedOrientation(meta),
                SteamNSteelStructureBlock.isMirrored(meta));
    }

    public Vec3 getMasterLocation()
    {//todo world rotation....
        return Vec3.createVectorHelper(
                xCoord - blockID.getLeft(),
                yCoord - blockID.getMiddle(),
                zCoord - blockID.getRight()
        );
    }

    public Block getMasterBlock()
    {
        final Vec3 mLoc = getMasterLocation();
        return getWorldObj().getBlock((int) mLoc.xCoord,(int) mLoc.yCoord, (int) mLoc.zCoord);
    }

    private void setBlockID(int x, int y, int z)
    {
        blockID = ImmutableTriple.of(x,y,z);
    }

    private void setNeighbour(ForgeDirection d)
    {
        neighbourBlocks |= d.flag;
    }

    public boolean hasNeighbour(ForgeDirection d)
    {
        return (neighbourBlocks & d.flag) != 0;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("neighbourBlocks", neighbourBlocks)
                .add("blockID", blockID)
                .add("renderBounds", renderBounds)
                .add("blockPatternHash", blockPatternHash)
                .add("structurePattern", structurePattern)
                .toString();
    }
}
