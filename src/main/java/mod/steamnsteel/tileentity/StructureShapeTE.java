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
import mod.steamnsteel.utility.structure.IStructureTE;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.Vec3;

public class StructureShapeTE extends SteamNSteelTE implements IStructureTE
{
    private int blockID = -1;
    private Optional<Vec3> masterLocation = Optional.absent();

    private static final String BLOCK_NUMBER = "blockNumber";
    private static final String MASTER_LOCATION = "masterLocation";

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

        final int[] mLoc = nbt.getIntArray(MASTER_LOCATION);
        if (mLoc != null && mLoc.length == 3)
        {
            masterLocation = Optional.of(Vec3.createVectorHelper(mLoc[0],mLoc[1],mLoc[2]));
        }

        blockID = nbt.getInteger(BLOCK_NUMBER);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (masterLocation.isPresent())
        {
            final Vec3 mLoc = masterLocation.get();
            nbt.setIntArray(MASTER_LOCATION, new int[]{(int)mLoc.xCoord,(int)mLoc.yCoord,(int)mLoc.zCoord});
        }

        nbt.setInteger(BLOCK_NUMBER,blockID);
    }

    public Block getMasterBlock()
    {
        final Vec3 mLoc = masterLocation.get();
        return worldObj.getBlock((int)mLoc.xCoord, (int)mLoc.yCoord, (int)mLoc.zCoord);
    }

    public Vec3 getMasterLocation()
    {
        final Vec3 mLoc = masterLocation.get();
        return Vec3.createVectorHelper(mLoc.xCoord, mLoc.yCoord, mLoc.zCoord);
    }

    public boolean hasMaster()
    {
        return masterLocation.isPresent();
    }

    public StructureShapeTE setMaster(int x, int y, int z)
    {
        masterLocation = Optional.of(Vec3.createVectorHelper(x,y,z));
        return this;
    }

    public void setBlockID(int blkID)
    {
        blockID = blkID < 0 ? -1:blkID;
    }

    public int getBlockID()
    {
        return blockID;
    }
}
