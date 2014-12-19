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
package mod.steamnsteel.utility.structure;

import com.google.common.base.Objects;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class StructureBlockCoord
{
    private final WorldBlockCoord worldCoord;
    private final Vec3 localCoord;
    private final Orientation orienetation;
    private final Vec3 pSize;
    private final boolean isMirrored;
    private final int blockID;

    public StructureBlockCoord(WorldBlockCoord worldCoord, Vec3 localCoord, Orientation orienetation, Vec3 pSize, int blockID, boolean isMirrored)
    {
        this.worldCoord = worldCoord;
        this.localCoord = localCoord;
        this.orienetation = orienetation;
        this.pSize = pSize;
        this.blockID = blockID;

        this.isMirrored = isMirrored;
    }

    public int getBlockID()
    {
        return blockID;
    }

    public Block getBlock(IBlockAccess world)
    {
        return worldCoord.getBlock(world);
    }

    public Vec3 getSize()
    {
        return Vec3.createVectorHelper(pSize.xCoord,pSize.yCoord,pSize.zCoord);
    }

    public int getX()
    {
        return worldCoord.getX();
    }

    public int getY()
    {
        return worldCoord.getY();
    }

    public int getZ()
    {
        return worldCoord.getZ();
    }

    public void setBlock(World world, Block block, int meta, int flag)
    {
        worldCoord.setBlock(world,block,meta,flag);
    }

    public void setBlock(World world, Block block)
    {
        worldCoord.setBlock(world, block);
    }

    public boolean isAirBlock(World world)
    {
        return worldCoord.isAirBlock(world);
    }

    public TileEntity getTileEntity(World world)
    {
        return worldCoord.getTileEntity(world);
    }

    public int getLX()
    {
        return (int)localCoord.xCoord;
    }

    public int getLY()
    {
        return (int)localCoord.yCoord;
    }

    public int getLZ()
    {
        return (int)(pSize.zCoord - localCoord.zCoord);
    }

    public Vec3 getLocal()
    {
        return Vec3.createVectorHelper(localCoord.xCoord, localCoord.yCoord, localCoord.zCoord);
    }

    public Orientation getOrienetation()
    {
        return orienetation;
    }

    public boolean isMasterBlock()
    {
        return (int)localCoord.xCoord == 0 && (int)localCoord.yCoord == 0 && (int) localCoord.zCoord == 0;
    }

    public boolean isMirrored()
    {
        return isMirrored;
    }

    public boolean hasLocalNeighbour(ForgeDirection d)
    {
        if (d == ForgeDirection.NORTH || d == ForgeDirection.SOUTH) d = isMirrored?d.getOpposite():d;
        switch (d)
        {
            case DOWN:
                return localCoord.yCoord > 0;
            case UP:
                return pSize.yCoord > localCoord.yCoord+1;
            case NORTH:
                return localCoord.zCoord > 0;
            case SOUTH:
                return pSize.zCoord > localCoord.zCoord+1;
            case WEST:
                return localCoord.xCoord > 0;
            case EAST:
                return pSize.xCoord > localCoord.xCoord+1;
            default:
                return false;
        }
    }

    public boolean hasGlobalNeighbour(ForgeDirection d)
    {
        //switch from local direction to global
        switch (orienetation)
        {
            case SOUTH:
                d = d.getRotation(ForgeDirection.DOWN).getRotation(ForgeDirection.DOWN);
                break;
            case WEST:
                d = d.getRotation(ForgeDirection.UP);
                break;
            case EAST:
                d = d.getRotation(ForgeDirection.DOWN);
                break;
            default://North
        }

        return hasLocalNeighbour(d);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("worldCoord",worldCoord)
                .add("localCoord",localCoord)
                .add("orienetation",orienetation)
                .add("pSize",pSize)
                .add("blockID",blockID)
                .toString();
    }
}
