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
import mod.steamnsteel.structure.registry.StructurePattern;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.utility.Orientation;
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

public abstract class SteamNSteelStructureTE extends SteamNSteelTE implements IStructureTE, ISidedInventory, IPatternHolder
{
    private static final byte blockIDShiftX = Byte.SIZE * 2;
    private static final byte blockIDShiftY = Byte.SIZE * 1;
    private static final byte blockIDShiftZ = Byte.SIZE * 0;

    private static final String BLOCK_ID = "blockID";
    private static final String NEIGHBOUR_BLOCKS = "neighbourBlocks";
    private static final String BLOCK_PATTERN_NAME = "blockPatternHash";

    private byte neighbourBlocks = 0x0;
    private int blockID = -1;
    private int blockPatternHash = "".hashCode();

    private Optional<AxisAlignedBB> renderBounds = Optional.absent();
    private boolean checkForMasterBlockType = true;
    private SteamNSteelStructureBlock structurePatternBlock = null;

    protected Optional<Orientation> orientation = Optional.absent();
    protected Optional<Vec3> masterLocation = Optional.absent();


    protected abstract boolean hasSharedInventory();
    protected abstract Inventory getSharedInventory();

    //todo marked for Liquid/Steam management later on.
    //protected abstract boolean hasSharedLiquidStorage();
    //protected abstract ------- getSharedLiquidStorage();

    @Override
    public StructurePattern getPattern()
    {
        return getMasterBlockInstance() != null ?
                structurePatternBlock.getPattern() :
                StructurePattern.MISSING_STRUCTURE;
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

        blockID = nbt.getInteger(BLOCK_ID);
        neighbourBlocks = nbt.getByte(NEIGHBOUR_BLOCKS);
        blockPatternHash = nbt.getInteger(BLOCK_PATTERN_NAME);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_ID, blockID);
        nbt.setByte(NEIGHBOUR_BLOCKS, neighbourBlocks);
        nbt.setInteger(BLOCK_PATTERN_NAME, blockPatternHash);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {//todo redo so not base off of getSelectedBoundingBoxFromPool
     if (!renderBounds.isPresent())
        {
            final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) getBlockType();
            final StructurePattern pattern = block.getPattern();

            renderBounds = pattern == StructurePattern.MISSING_STRUCTURE ?
                    Optional.of(INFINITE_EXTENT_AABB) :
                    Optional.of(block.getSelectedBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord));
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
        return getPattern().getSideAccess(
                getBlockIDX(),
                getBlockIDY(),
                getBlockIDZ(),
                ForgeDirection.values()[side],
                Orientation.getdecodedOrientation(getWorldObj().getBlockMetadata(xCoord,yCoord,zCoord)));
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
        return getPattern().getBlock(getBlockIDX(),getBlockIDY(),getBlockIDZ());
    }

    @Override
    public int getTransmutedMeta()
    {
        final int meta = getWorldObj().getBlockMetadata(xCoord,yCoord,zCoord);
        return getPattern().getBlockMetadata(
                getBlockIDX(),
                getBlockIDY(),
                getBlockIDZ(),
                Orientation.getdecodedOrientation(meta),
                SteamNSteelStructureBlock.isMirrored(meta)
        );
    }

    public Vec3 getMasterLocation()
    {
        if (!masterLocation.isPresent())
            return getMasterLocation(getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord));

        return masterLocation.get();
    }

    public Vec3 getMasterLocation(int meta)
    {
        if (!masterLocation.isPresent())
        {
            final Orientation o = Orientation.getdecodedOrientation(meta);
            final boolean isMirrored = SteamNSteelStructureBlock.isMirrored(meta);

            return getMasterLocation(o,isMirrored);
        }

        return masterLocation.get();
    }

    public Vec3 getMasterLocation(Orientation o, boolean isMirrored)
    {
        if (!masterLocation.isPresent())
        {
            Vec3 loc = Vec3.createVectorHelper(getBlockIDX(),getBlockIDY(),getBlockIDZ());

            if (isMirrored)
            {
                final int z = getPattern().getSizeZ();
                final int hlfZ = z / 2;
                loc.zCoord -= hlfZ;
                loc.zCoord *= -1;
                loc.zCoord += hlfZ;
                if (z % 2 == 0) loc.zCoord -= 1;
            }

            loc.rotateAroundY((float) o.getRotationValue());

            loc.xCoord = xCoord - loc.xCoord;
            loc.yCoord = yCoord - loc.yCoord;
            loc.zCoord = zCoord - loc.zCoord;

           masterLocation = Optional.of(loc);
        }

        return masterLocation.get();
    }

    private void setBlockID(int x, int y, int z)
    {
        blockID = (((byte) x) << blockIDShiftX) +
                  (((byte) y) << blockIDShiftY) +
                  (((byte) z) << blockIDShiftZ);
    }

    private byte getBlockIDX()
    {
        return (byte)(blockID >> blockIDShiftX);
    }

    private byte getBlockIDY()
    {
        return (byte)(blockID >> blockIDShiftY);
    }

    private byte getBlockIDZ()
    {
        return (byte)(blockID >> blockIDShiftZ);
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

    private String toStringNeighbour()
    {
        final StringBuilder builder = new StringBuilder(6);

        builder.append(hasNeighbour(ForgeDirection.UP)   ?'U':' ');
        builder.append(hasNeighbour(ForgeDirection.DOWN) ?'D':' ');
        builder.append(hasNeighbour(ForgeDirection.NORTH)?'N':' ');
        builder.append(hasNeighbour(ForgeDirection.SOUTH)?'S':' ');
        builder.append(hasNeighbour(ForgeDirection.EAST) ?'E':' ');
        builder.append(hasNeighbour(ForgeDirection.WEST) ?'W':' ');

        return builder.toString();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("neighbourBlocks", toStringNeighbour())
                .add("blockID", Vec3.createVectorHelper(getBlockIDX(),getBlockIDY(),getBlockIDZ()))
                .add("renderBounds", renderBounds)
                .add("blockPatternHash", blockPatternHash)
                .add("masterLocation", masterLocation)
                .add("\n\tstructurePattern", structurePatternBlock)
                .toString();
    }
}
