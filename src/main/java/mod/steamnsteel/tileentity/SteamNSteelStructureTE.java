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
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.IStructureTE;
import mod.steamnsteel.utility.structure.StructureBlockCoord;
import mod.steamnsteel.utility.structure.StructurePattern;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class SteamNSteelStructureTE extends SteamNSteelTE implements IStructureTE
{
    private int blockID = -1;
    private Optional<AxisAlignedBB> renderBounds = Optional.absent();

    private static final String BLOCK_ID = "blockID";
    private static final String NEIGHBOUR_BLOCKS = "neighbourBlocks";
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
        blockID = nbt.getInteger(BLOCK_ID);
        neighbourBlocks = nbt.getByte(NEIGHBOUR_BLOCKS);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger(BLOCK_ID,blockID);
        nbt.setByte(NEIGHBOUR_BLOCKS,neighbourBlocks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
     if (!renderBounds.isPresent())
        {
            final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) getBlockType();
            final StructurePattern pattern = block.getPattern();

            if (pattern == StructurePattern.MISSING_STRUCTURE)
            {
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
    public int getBlockID()
    {
        return blockID;
    }

    private void setBlockID(int blkID)
    {
        blockID = blkID < 0 ? -1:blkID;
    }

    @Override
    public void configureBlock(StructureBlockCoord sBlock)
    {
        setBlockID(sBlock.getBlockID());

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS) if (sBlock.hasGlobalNeighbour(d)) setNeighbour(d);
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
                .toString();
    }
}
