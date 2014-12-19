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
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import mod.steamnsteel.tileentity.StructureShapeTE;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IStructurePatternBlock
{
    private static String STRUCTURE_LOCATION = "structure/";
    private static String STRUCTURE_FILE_EXTENSION = ".structure.json";

    private Optional<StructurePattern> blockPattern = Optional.absent();

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

        while (itr.hasNext())
        {
            final StructureBlockCoord block = itr.next();

            if (block.getX() != x || block.getY() != y || block.getZ() != z)
            {
                block.setBlock(world, ModBlock.structureShape, meta, 2);
                final StructureShapeTE ssBlock = (StructureShapeTE) block.getTileEntity(world);
                ssBlock.setMaster(x, y, z);
                ssBlock.configureBlock(block);
            } else {
                final TileEntity te = block.getTileEntity(world);

                if(te instanceof IStructureTE){
                    final IStructureTE ssBlock = (IStructureTE) te;
                    ssBlock.configureBlock(block);
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oBlock, int meta)
    {
        super.breakBlock(world, x, y, z, oBlock, meta);
        /*final Orientation o = Orientation.getdecodedOrientation(meta);

        StructureBlockIterator itr = new StructureBlockIterator(getPattern(), Vec3.createVectorHelper(x, y, z), o, false);

        while (itr.hasNext())
        {
            final StructureBlockCoord block = itr.next();

            block.setBlock(world, Blocks.air);
            world.removeTileEntity(block.getX(),block.getY(),block.getZ());
        }*/
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rng)
    {
        //world.spawnParticle("smoke", x+0.5*rng.nextFloat(), y + 0.5, z+0.5*rng.nextFloat(), 0.0d, 0.05d, 0.0d);
        super.randomDisplayTick(world, x, y, z, rng);
    }

    protected Vec3 getMasterBlock(World world, int x, int y, int z)
    {
        return Vec3.createVectorHelper(x,y,z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
        {
            if (te.hasNeighbour(d))
            {
                final int nx = x + d.offsetX;
                final int ny = y + d.offsetY;
                final int nz = z + d.offsetZ;

                final Block nBlock = world.getBlock(nx,ny,nz);

                if (!(nBlock instanceof SteamNSteelStructureBlock))
                {
                    if (StructureShapeBlock.class.equals(getClass())) //TODO Remove This check
                    {
                        //is structure
                        final StructureShapeTE cte = (StructureShapeTE) te;
                        final Block mblock = cte.getMasterBlock();

                        if (mblock instanceof SteamNSteelStructureBlock)
                        {
                            if (!world.isRemote) Logger.info("structure: " + Vec3.createVectorHelper(x,y,z));
                            placePatternBlocks(world, cte.getMasterLocation(), (SteamNSteelStructureBlock) mblock);
                            return;
                        }
                        //no master block
                    } else if (this instanceof SteamNSteelStructureBlock) { //TODO Remove This check
                        //Master Block broken
                        if (!world.isRemote) Logger.info("master: " + Vec3.createVectorHelper(x,y,z));
                        placePatternBlocks(world, Vec3.createVectorHelper(x, y, z), this);
                        return;
                    }

                    //cascade
                    if (!world.isRemote) Logger.info("cascade: " + Vec3.createVectorHelper(x,y,z));
                    world.setBlockToAir(x, y, z);
                    world.removeTileEntity(x, y, z);
                    return;

                }
            }
        }
    }

    private static void placePatternBlocks(World world, Vec3 mLoc, SteamNSteelStructureBlock block)
    {
        final StructurePattern pattern = block.getPattern();
        final int meta = world.getBlockMetadata((int)mLoc.xCoord,(int)mLoc.yCoord,(int)mLoc.zCoord);
        final StructureBlockIterator itr = new StructureBlockIterator(pattern, mLoc, Orientation.getdecodedOrientation(meta), isMirrored(meta));

        //set Blocks to pattern
        while (itr.hasNext())
        {
            final StructureBlockCoord coord = itr.next();

            final Block patternReplacementBlock = pattern.getBlock(coord);
            final byte patternMeta = pattern.getBlockMetadata(coord);
            final Block coordBlock = coord.getBlock(world);

            if (coordBlock == Blocks.air || coordBlock.getClass().equals(StructureShapeBlock.class) || block.getPattern() == pattern) //TODO fix issue with transported crossover master blocks
            {
                //is structure block or air, can replace.
                coord.setBlock(world,patternReplacementBlock, patternMeta, 2);
            } else {
                //spawn block in the world
                if (patternReplacementBlock != Blocks.air){
                    //make sure not to spawn air block
                    patternReplacementBlock.dropBlockAsItem(world,(int)mLoc.xCoord,(int)mLoc.yCoord,(int)mLoc.zCoord,8,0);
                }
            }
        }
    }

    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
        return true; //No Digging Effects
    }

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
        final Vec3 mLoc = getMasterBlock(world, x, y, z);
        final float sAjt = 0.2f;

        final StructureBlockIterator itr = new StructureBlockIterator(
                ((IStructurePatternBlock) world.getBlock((int) mLoc.xCoord, (int) mLoc.yCoord, (int) mLoc.zCoord)).getPattern(),
                mLoc,
                Orientation.getdecodedOrientation(meta),
                isMirrored(meta)
        );

        while (itr.hasNext())
        {
            final StructureBlockCoord coord = itr.next();

            float xSpeed = 0.0f;
            float ySpeed = 0.0f;
            float zSpeed = 0.0f;
            int count = 0;

            final IStructureTE te = (IStructureTE) coord.getTileEntity(world);
            for(ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            {
                if (!te.hasNeighbour(d)) {
                    xSpeed += d.offsetX;
                    ySpeed += d.offsetY;
                    zSpeed += d.offsetZ;
                }
            }

            if (++count > 5) continue;

            world.spawnParticle("largeexplode", coord.getX()+0.5, coord.getY() + 0.5, coord.getZ()+0.5, xSpeed*sAjt, ySpeed*sAjt, zSpeed*sAjt);
        }

        return true;
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
        final float rot = (float) Orientation.
                getdecodedOrientation(world.getBlockMetadata(x, y, z)).
                getRotationValue();

        size.rotateAroundY(rot);
        b.rotateAroundY(rot);

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
    {//TODO cache mby?
        final int meta = world.getBlockMetadata(x, y, z);
        final Orientation o = Orientation.getdecodedOrientation(meta);
        final float[][] collB = getPattern().getCollisionBoxes();

        final Vec3 trans = getPattern().getHalfSize().addVector(-0.5, 0, -0.5);
        final boolean isMirrored = isMirrored(meta);
        final float rot = (float) o.getRotationValue();

        trans.rotateAroundY(rot);
        for (float[] f: collB)
        {
            final Vec3 lower = Vec3.createVectorHelper(f[0], f[1], f[2] * (isMirrored?-1:1));
            final Vec3 upper = Vec3.createVectorHelper(f[3], f[4], f[5] * (isMirrored?-1:1));

            lower.rotateAroundY(rot);
            upper.rotateAroundY(rot);

            final AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
                    x + min(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + lower.yCoord, z + min(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5,
                    x + max(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + upper.yCoord, z + max(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5);

            if (aabb.intersectsWith(bb))
            {
                boundingBoxList.add(bb);
            }
        }
    }

    private static boolean isMirrored(int meta)
    {
        return (meta & flagMirrored) != 0;
    }

    //TODO remove!!!
    public void cleanPattern()
    {
        blockPattern = Optional.absent();
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }
}
