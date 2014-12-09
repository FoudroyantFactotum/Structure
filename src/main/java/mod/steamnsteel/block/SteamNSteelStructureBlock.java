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
package mod.steamnsteel.block;

import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.TheMod;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.tileentity.StructureShapeTE;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import mod.steamnsteel.utility.structure.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.lang.Math.*;

public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IStructurePatternBlock
{
    private static String STRUCTURE_LOCATION = "structure/";
    private static String STRUCTURE_FILE_EXTENSION = ".structure.json";

    protected Optional<StructurePattern> blockPattern = Optional.absent();

    public static final int flagMirrored = 1<<2;

    @Override
    public StructurePattern getPattern()
    {
        if (!blockPattern.isPresent())
        {
            final ResourceLocation jsonStructure = getResourceLocation(getStructurePath(getUnwrappedUnlocalizedName(
                    getBlockName(getUnlocalizedName()))));

            try
            {
                final IResource res = Minecraft.getMinecraft().getResourceManager().getResource(jsonStructure);
                final InputStreamReader inpStream = new InputStreamReader(res.getInputStream());
                final BufferedReader buffRead = new BufferedReader(inpStream);

                blockPattern = Optional.of(JSONStructurePattern.gson.fromJson(buffRead, StructurePattern.class));

                buffRead.close();
                inpStream.close();
            } catch (IOException e)
            {
                Logger.info("file does not exist : " + e.getMessage());
                blockPattern = Optional.of(StructurePattern.MISSING_STRUCTURE);
            }
        }

        return blockPattern.get();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack)
    {
        super.onBlockPlacedBy(world, x, y, z, entity, itemStack);

        int meta = world.getBlockMetadata(x, y, z);
        final boolean mirror = entity.isSneaking();
        final Orientation o = Orientation.getdecodedOrientation(meta);

        if (mirror) {
            meta |= flagMirrored;
            world.setBlockMetadataWithNotify(x,y,z,meta,0);
        }

        StructureBlockIterator itr = new StructureBlockIterator(getPattern(), Vec3.createVectorHelper(x, y, z), o, mirror);

        int blkID = 0;

        while (itr.hasNext())
        {
            final WorldBlockCoord block = itr.next();

            if (block.getX() != x || block.getY() != y || block.getZ() != z)
            {
                block.setBlock(world, ModBlock.structureShape, meta, 2);
                final StructureShapeTE ssBlock = (StructureShapeTE) block.getTileEntity(world);
                ssBlock.setMaster(x, y, z);
                ssBlock.setBlockID(blkID);
                ssBlock.setNeighbours(getPattern().getSize(), Orientation.NORTH);


            } else {
                final TileEntity te = block.getTileEntity(world);

                if(te instanceof IStructureTE)
                    ((IStructureTE) te).setBlockID(blkID);
            }
            ++blkID;
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oBlock, int meta)
    {
        final Orientation o = Orientation.getdecodedOrientation(meta);

        StructureBlockIterator itr = new StructureBlockIterator(getPattern(), Vec3.createVectorHelper(x, y, z), o, false);

        while (itr.hasNext())
        {
            final WorldBlockCoord block = itr.next();

            block.setBlock(world, Blocks.air);
            world.removeTileEntity(block.getX(),block.getY(),block.getZ());
        }
    }

    private static ResourceLocation getResourceLocation(String path)
    {
        return new ResourceLocation(TheMod.MOD_ID.toLowerCase(), path);
    }

    private static String getStructurePath(String name)
    {
        return STRUCTURE_LOCATION + name + STRUCTURE_FILE_EXTENSION;
    }

    private static String getBlockName(String s)
    {
        final int p = s.indexOf(":");
        return s.substring(p+1, s.length());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {//TODO consider caching AABB?
        final Vec3 size = getPattern().getSize().addVector(-0.5, 0, -0.5);
        final Vec3 b = Vec3.createVectorHelper(-0.5,0,-0.5);
        final Orientation o = Orientation.getdecodedOrientation(world.getBlockMetadata(x, y, z));

        size.rotateAroundY((float) (Math.PI * (1.0-o.ordinal()/2.0)));
        b.rotateAroundY((float) (Math.PI * (1.0-o.ordinal()/2.0)));

        return AxisAlignedBB.getBoundingBox(
                x + 0.5 + min(b.xCoord, size.xCoord),
                y,
                z + 0.5 + min(b.zCoord, size.zCoord),

                x + 0.5 + max(b.xCoord, size.xCoord),
                y + size.yCoord,
                z + 0.5 + max(b.zCoord, size.zCoord));
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {//TODO cache
        final TileEntity te = world.getTileEntity(x,y,z);

        if (te instanceof StructureShapeTE){
            final Block block = ((StructureShapeTE) te).getMasterBlock();

            if (block instanceof SteamNSteelStructureBlock)
            {
                final Vec3 ml = ((StructureShapeTE) te).getMasterLocation();

                block.addCollisionBoxesToList(world, (int)ml.xCoord, (int)ml.yCoord, (int)ml.zCoord, aabb, boundingBoxList, entityColliding);
            }

            return;
        }

        final int meta = world.getBlockMetadata(x, y, z);
        final Orientation o = Orientation.getdecodedOrientation(meta);
        final float[][] collB = getPattern().getCollisionBoxes();
        final Vec3 trans = getPattern().getHalfSize().addVector(-0.5,0,-0.5);

        final boolean isMirrored = (meta & flagMirrored) != 0;

        for (float[] f: collB)
        {
            final Vec3 lower = Vec3.createVectorHelper(f[0], f[1], f[2] * (isMirrored?-1:1));
            final Vec3 upper = Vec3.createVectorHelper(f[3], f[4], f[5] * (isMirrored?-1:1));
            final float rot = (float) (PI * (1.0 - o.ordinal() / 2.0));

            lower.rotateAroundY(rot);
            upper.rotateAroundY(rot);
            trans.rotateAroundY(rot);

            final AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
                    x + min(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + lower.yCoord, z + min(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5,
                    x + max(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + upper.yCoord, z + max(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5);

            if (aabb.intersectsWith(bb))
            {
                boundingBoxList.add(bb);
            }
        }
    }

    //TODO remove!!!
    public void cleanPattern()
    {
        blockPattern = Optional.absent();
    }
}
