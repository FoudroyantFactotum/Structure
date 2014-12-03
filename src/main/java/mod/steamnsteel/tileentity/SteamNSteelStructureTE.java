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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.IStructureTE;
import mod.steamnsteel.utility.structure.StructurePattern;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public abstract class SteamNSteelStructureTE extends SteamNSteelTE implements IStructureTE
{
    private int blockID = -1;
    private Optional<AxisAlignedBB> renderBounds = Optional.absent();

    private static final String BLOCK_ID = "blockID";

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
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger(BLOCK_ID,blockID);
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
                final Vec3 size = block.getPattern().getSize();

                renderBounds = Optional.of(AxisAlignedBB.getBoundingBox(
                        xCoord - (int)size.xCoord/2,
                        yCoord,
                        zCoord - (int)size.zCoord/2,

                        xCoord + Math.ceil(size.xCoord/2),
                        yCoord + size.yCoord,
                        zCoord + Math.ceil(size.zCoord/2)));
            }
        }

        return renderBounds.get();
    }

    @Override
    public int getBlockID()
    {
        return blockID;
    }

    @Override
    public void setBlockID(int blkID)
    {
        blockID = blkID < 0 ? -1:blkID;
    }
}
