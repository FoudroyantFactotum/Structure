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
package mod.steamnsteel.structure.coordinates;

import com.google.common.base.Objects;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;

public class StructureBlockCoord
{
    private final int localCoordX;
    private final int localCoordY;
    private final int localCoordZ;

    private final int localNeighbors;
    private final boolean isMasterBlock;

    private final Vec3 worldLocation;
    private final Orientation orientation;
    private final boolean isMirrored;

    private final WorldBlockCoord worldCoord;

    public StructureBlockCoord(int localCoordX, int localCoordY, int localCoordZ, boolean isMasterBlock,
                               int localNeighbors, Vec3 worldLocation,
                               WorldBlockCoord worldCoord, Orientation orientation, boolean isMirrored)
    {
        this.localCoordX = localCoordX;
        this.localCoordY = localCoordY;
        this.localCoordZ = localCoordZ;

        this.localNeighbors = localNeighbors;
        this.isMasterBlock = isMasterBlock;

        this.worldLocation = worldLocation;
        this.orientation = orientation;
        this.isMirrored = isMirrored;
        this.worldCoord = worldCoord;
    }

    public Block getBlock(IBlockAccess world)
    {
        return worldCoord.getBlock(world);
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

    public void setMeta(World world, int meta, int flag)
    {
        //todo redo
        world.setBlockMetadataWithNotify(worldCoord.getX(), worldCoord.getY(), worldCoord.getZ(), meta, flag);
    }

    public void updateNeighbors(World world)
    {
        world.notifyBlocksOfNeighborChange(worldCoord.getX(), worldCoord.getY(), worldCoord.getZ(), ModBlock.structureShape);
    }

    public boolean isAirBlock(World world)
    {
        return worldCoord.isAirBlock(world);
    }

    public boolean isReplaceable(World world)
    {
        return worldCoord.getBlock(world).canReplace(world, worldCoord.getX(),worldCoord.getY(),worldCoord.getZ(),0,null);
    }

    public TileEntity getTileEntity(World world)
    {
        return worldCoord.getTileEntity(world);
    }

    public void removeTileEntity(World world)
    {
        world.removeTileEntity(worldCoord.getX(),worldCoord.getY(),worldCoord.getZ());
    }

    public int getLX()
    {
        return localCoordX;
    }

    public int getLY()
    {
        return localCoordY;
    }

    public int getLZ()
    {
        return localCoordZ;
    }

    public Vec3 getLocal()
    {
        return Vec3.createVectorHelper(localCoordX, localCoordY, localCoordZ);
    }

    public boolean isMasterBlock()
    {
        return isMasterBlock;
    }

    public boolean hasLocalNeighbour(ForgeDirection d)
    {
        return (localNeighbors & d.flag) != 0;
    }

    public String getLocalNeighbour()
    {
        return " \"" + toStringNeighbour(localNeighbors) + "\"";
    }

    public boolean isEdge()
    {
        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            if (hasGlobalNeighbour(d))
                return true;

        return false;
    }

    public boolean hasGlobalNeighbour(ForgeDirection d)
    {
        return hasLocalNeighbour(localToGlobal(d, orientation, isMirrored));
    }

    public static String toStringNeighbour(int n)
    {
        final StringBuilder builder = new StringBuilder(ForgeDirection.VALID_DIRECTIONS.length);

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            builder.append((n & d.flag) != 0 ? d.name().charAt(0):' ');

        return builder.toString();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("localCoord", ImmutableTriple.of(localCoordX,localCoordY,localCoordZ))
                .add("localNeighbors", toStringNeighbour(localNeighbors))
                .add("worldCoord", worldCoord)
                .add("isMasterBlock",isMasterBlock)
                .add("worldLocation", worldLocation)
                .add("orientation", orientation)
                .add("isMirrored", isMirrored)
                .add("worldCoord", worldCoord)
                .toString();
    }
}
