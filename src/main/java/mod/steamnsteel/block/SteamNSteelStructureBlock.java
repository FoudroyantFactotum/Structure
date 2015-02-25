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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.structure.IStructure.IPatternHolder;
import mod.steamnsteel.structure.IStructure.IStructureAspects;
import mod.steamnsteel.structure.IStructureTE;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.coordinates.StructureBlockIterator;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IPatternHolder, IStructureAspects
{
    public static final int flagMirrored = 1 << 2;
    public static final int maskMeta = 0x7;

    private StructureDefinition structureDefinition = null;

    @Override
    public StructureDefinition getPattern()
    {
        return structureDefinition;
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
            world.setBlockMetadataWithNotify(x,y,z,meta,0x2);
        }

        final StructureBlockIterator itr =
                new StructureBlockIterator(
                        getPattern(),
                        Vec3.createVectorHelper(x, y, z),
                        o,
                        mirror
                );

        //place Blocks
        while (itr.hasNext())
        {
            final StructureBlockCoord block = itr.next();

            if (!block.isMasterBlock())
                block.setBlock(world, ModBlock.structureShape, meta, 0x2);

            final IStructureTE ssBlock = (IStructureTE) block.getTileEntity(world);
            ssBlock.setBlockPattern(getUnlocalizedName());
            ssBlock.configureBlock(block);
        }
    }

    @Override
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta)
    {
        final StructureBlockIterator itr =
                new StructureBlockIterator(
                        getPattern(),
                        Vec3.createVectorHelper(x, y, z),
                        Orientation.getdecodedOrientation(meta),
                        isMirrored(meta)
                );

        while (itr.hasNext())
        {
            final StructureBlockCoord block = itr.next();

            if (block.isEdge())
                block.updateNeighbors(world);
        }

        super.onPostBlockPlaced(world, x, y, z, meta);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);
        onSharedNeighbourBlockChange(world, x, y, z, block);
    }


    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        final int meta = world.getBlockMetadata(x,y,z);
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);
        final boolean isPlayerCreative = player != null && player.capabilities.isCreativeMode;

        if (te != null)
            breakStructure(world, Vec3.createVectorHelper(x, y, z), getPattern(), Orientation.getdecodedOrientation(meta), isMirrored(meta), isPlayerCreative);
        else
            world.setBlockToAir(x,y,z);

        return true;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {//TODO fix lack of extendability
        final int meta = world.getBlockMetadata(x,y,z);

        addCollisionBoxesToListUsingPattern(world, x, y, z, aabb, boundingBoxList, entityColliding, getPattern(),
                Orientation.getdecodedOrientation(meta), isMirrored(meta));
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int meta, float sx, float sy, float sz)
    {
        if (!world.isRemote) print("master");
        return onStructureBlockActivated(world,x,y,z,player,meta,sx,sy,sz,0x0,x,y,z);
    }

    @Override
    public boolean onStructureBlockActivated(World world, int x, int y, int z, EntityPlayer player, int meta, float sx, float sy, float sz, int sbID, int sbx, int sby, int sbz) {
        if (!world.isRemote) print("onStructureBlockActivated: ", Vec3.createVectorHelper(x,y,z), " : ", sbID, " : ", Vec3.createVectorHelper(sbx, sby, sbz));
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        if (te != null)
        {
            final Orientation o = Orientation.getdecodedOrientation(meta);
            final boolean isMirrored = isMirrored(meta);

            final float sAjt = 0.05f;

            final StructureBlockIterator itr = new StructureBlockIterator(
                    getPattern(),
                    Vec3.createVectorHelper(x, y, z),
                    o,
                    isMirrored
            );

            while (itr.hasNext()) {
                final StructureBlockCoord coord = itr.next();

                float xSpeed = 0.0f;
                float ySpeed = 0.0f;
                float zSpeed = 0.0f;
                int count = 0;

                for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    if (!coord.hasGlobalNeighbour(d)) {
                        xSpeed += d.offsetX;
                        ySpeed += d.offsetY;
                        zSpeed += d.offsetZ;
                    }
                }

                if (++count > 5) continue;

                spawnBreakParticle(world, te, coord, xSpeed * sAjt, ySpeed * sAjt, zSpeed * sAjt);
            }
        }

        return true; //No Destroy Effects
    }

    @SideOnly(Side.CLIENT)
    protected abstract void spawnBreakParticle(World world, SteamNSteelStructureTE te, StructureBlockCoord coord, float sx, float sy, float sz);

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
        return true; //No Digging Effects
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return getBoundingBoxUsingPattern(x, y, z, getPattern(), Orientation.getdecodedOrientation(world.getBlockMetadata(x, y, z)));
    }


    //=======================================================
    //       S t r u c t u r e   B l o c k   C o d e
    //=======================================================

    public static boolean isMirrored(int meta)
    {
        return (meta & flagMirrored) != 0;
    }

    public static void onSharedNeighbourBlockChange(World world, int x, int y, int z, Block block)
    {//todo finish/fix
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);
        final int meta = world.getBlockMetadata(x,y,z) & maskMeta;

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
        {
            if (te.hasNeighbour(d))
            {
                final int nx = x + d.offsetX;
                final int ny = y + d.offsetY;
                final int nz = z + d.offsetZ;

                final Block nBlock = world.getBlock(nx,ny,nz);
                final int nMeta = world.getBlockMetadata(nx, ny, nz) & maskMeta;

                if (neighborCheck(meta, nMeta, nBlock))
                {
                    world.setBlock(x, y, z, te.getTransmutedBlock(), te.getTransmutedMeta(), 0x3);
                    if (!world.isRemote)
                        ((WorldServer)world).func_147487_a("hugeexplosion", x, y, z, 0,0,0,0,0); //send particle packet

                    return;
                }
            }
        }
    }

    private static boolean neighborCheck(int meta, int nMeta, Block block)
    {
        if (meta == nMeta)
            if (block instanceof StructureShapeBlock || block instanceof SteamNSteelStructureBlock)
                return false;

        return true;
    }

    public static void addCollisionBoxesToListUsingPattern(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding, StructureDefinition sp, Orientation o, boolean isMirrored)
    {
        final float[][] collB = sp.getCollisionBoxes();

        final Vec3 trans = sp.getHalfSize().addVector(-0.5, 0, -0.5);
        final float rot = (float) o.getRotationValue();

        final Vec3 lower = Vec3.createVectorHelper(0,0,0);
        final Vec3 upper = Vec3.createVectorHelper(0,0,0);

        final AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(0,0,0, 0,0,0);

        trans.rotateAroundY(rot);
        for (final float[] f: collB)
        {
            modifyVec3(lower, f[0], f[1], f[2] * (isMirrored?-1:1));
            modifyVec3(upper, f[3], f[4], f[5] * (isMirrored?-1:1));

            lower.rotateAroundY(rot);
            upper.rotateAroundY(rot);

            modifyAxisAlignedBB(bb,
                    x + min(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + lower.yCoord, z + min(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5,
                    x + max(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + upper.yCoord, z + max(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5);

            if (aabb.intersectsWith(bb))
                boundingBoxList.add(bb.copy());
        }
    }

    public static AxisAlignedBB getBoundingBoxUsingPattern(int x, int y, int z, StructureDefinition sp, Orientation o)
    {
        final Vec3 bbSize = Vec3.createVectorHelper(
                sp.getSizeX() - 0.5,
                sp.getSizeY(),
                sp.getSizeZ() - 0.5);
        final Vec3 b = Vec3.createVectorHelper(-0.5,0,-0.5);
        final float rot = (float) o.getRotationValue();

        bbSize.rotateAroundY(rot);
        b.rotateAroundY(rot);

        return AxisAlignedBB.getBoundingBox(
                x + 0.5 + min(b.xCoord, bbSize.xCoord),
                y,
                z + 0.5 + min(b.zCoord, bbSize.zCoord),

                x + 0.5 + max(b.xCoord, bbSize.xCoord),
                y + bbSize.yCoord,
                z + 0.5 + max(b.zCoord, bbSize.zCoord));
    }

    public static void breakStructure(World world, Vec3 mloc, StructureDefinition sp, Orientation o, boolean isMirrored, boolean isCreative)
    {
        final StructureBlockIterator itr = new StructureBlockIterator(
                sp,
                mloc,
                o,
                isMirrored
        );

        while (itr.hasNext())
        {
            final StructureBlockCoord coord = itr.next();
            final Block block = sp.getBlock(coord);

            //coord.setBlock(world, isCreative || block == null? Blocks.air : block, 0, 0x2);
            coord.setBlock(world, block == null? Blocks.air : block, 0, 0x2);
            coord.removeTileEntity(world);
        }

        itr.cleanIterator();

        while (itr.hasNext())
        {
            final StructureBlockCoord coord = itr.next();

            if (coord.isEdge())
                coord.updateNeighbors(world);
        }
    }

    private static void modifyVec3(Vec3 v, double x, double y ,double z)
    {
        v.xCoord = x; v.yCoord = y; v.zCoord = z;
    }

    private static void modifyAxisAlignedBB(AxisAlignedBB a, double lx, double ly ,double lz, double ux, double uy ,double uz)
    {
        a.minX = lx; a.minY = ly; a.minZ = lz;
        a.maxX = ux; a.maxY = uy; a.maxZ = uz;
    }
}
