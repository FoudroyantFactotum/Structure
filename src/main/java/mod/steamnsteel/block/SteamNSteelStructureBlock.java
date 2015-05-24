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

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.structure.IStructure.IPatternHolder;
import mod.steamnsteel.structure.IStructure.IStructureAspects;
import mod.steamnsteel.structure.IStructureTE;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.coordinates.StructureBlockIterator;
import mod.steamnsteel.structure.net.StructureParticleChoice;
import mod.steamnsteel.structure.net.StructureParticlePacket;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import mod.steamnsteel.utility.ModNetwork;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.List;
import java.util.Random;

import static mod.steamnsteel.block.structure.StructureShapeBlock.EMPTY_BOUNDS;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IPatternHolder, IStructureAspects, ITileEntityProvider
{
    public static final int flagMirrored = 1 << 2;
    public static final int maskMeta = 0x7;

    private int regHash = 0;
    private StructureDefinition structureDefinition = null;

    @Override
    public StructureDefinition getPattern()
    {
        return structureDefinition;
    }

    public int getRegHash()
    {
        return regHash;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack)
    {
        super.onBlockPlacedBy(world, x, y, z, entity, itemStack);

        int meta = world.getBlockMetadata(x, y, z);
        final boolean mirror = false; //entity.isSneaking(); Disabled until fix :p todo fix structure mirroring
        final Orientation o = getdecodedOrientation(meta);

        if (mirror)
        {
            meta |= flagMirrored;
            world.setBlockMetadataWithNotify(x, y, z, meta, 0x2);
        }

        final StructureBlockIterator itr =
                new StructureBlockIterator(
                        getPattern(),
                        ImmutableTriple.of(x,y,z),
                        o,
                        mirror
                );

        formStructure(world, itr, meta);
    }

    public void formStructure(World world, StructureBlockIterator itr, int meta)
    {
        //place Blocks
        while (itr.hasNext())
        {
            final StructureBlockCoord block = itr.next();

            if (!block.isMasterBlock())
                block.setBlock(world, ModBlock.structureShape, meta, 0x2);

            final IStructureTE ssBlock = (IStructureTE) block.getTileEntity(world);

            ssBlock.configureBlock(block, regHash);
        }
    }

    @Override
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta)
    {
        final StructureBlockIterator itr =
                new StructureBlockIterator(
                        getPattern(),
                        ImmutableTriple.of(x,y,z),
                        getdecodedOrientation(meta),
                        isMirrored(meta)
                );

        //update blocks after structure is build
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
        onSharedNeighbourBlockChange(world, x, y, z, regHash, block);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        final int meta = world.getBlockMetadata(x,y,z);
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);
        final boolean isPlayerCreative = player != null && player.capabilities.isCreativeMode;

        if (te != null)
            breakStructure(world, ImmutableTriple.of(x, y, z), getPattern(), getdecodedOrientation(meta), isMirrored(meta), isPlayerCreative);
        else
            world.setBlockToAir(x,y,z);

        return true;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {
        final int meta = world.getBlockMetadata(x,y,z);

        localToGlobal(x,y,z,aabb, boundingBoxList, getPattern().getCollisionBoxes(), getdecodedOrientation(meta), isMirrored(meta), getPattern().getBlockBounds());
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz)
    {
        return onStructureBlockActivated(world,x,y,z,player,side,sx,sy,sz,SteamNSteelStructureTE.ORIGIN_ZERO,x,y,z);
    }

    @Override
    public boolean onStructureBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz, ImmutableTriple<Byte, Byte, Byte> sbID, int sbx, int sby, int sbz) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        if (te != null)
        {
            final Orientation o = getdecodedOrientation(meta);
            final boolean isMirrored = isMirrored(meta);

            final float sAjt = 0.05f;

            final StructureBlockIterator itr = new StructureBlockIterator(
                    getPattern(),
                    ImmutableTriple.of(x,y,z),
                    o,
                    isMirrored
            );

            while (itr.hasNext())
            {
                final StructureBlockCoord coord = itr.next();

                float xSpeed = 0.0f;
                float ySpeed = 0.0f;
                float zSpeed = 0.0f;

                for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
                {
                    if (!coord.hasGlobalNeighbour(d))
                    {
                        xSpeed += d.offsetX;
                        ySpeed += d.offsetY;
                        zSpeed += d.offsetZ;
                    }
                }

                spawnBreakParticle(world, te, coord, xSpeed * sAjt, ySpeed * sAjt, zSpeed * sAjt);
            }
        }

        return true; //No Destroy Effects
    }

    @SideOnly(Side.CLIENT)
    public abstract void spawnBreakParticle(World world, SteamNSteelStructureTE te, StructureBlockCoord coord, float sx, float sy, float sz);

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
        return EMPTY_BOUNDS;
    }


    //=======================================================
    //       S t r u c t u r e   B l o c k   C o d e
    //=======================================================

    public static boolean isMirrored(int meta)
    {
        return (meta & flagMirrored) != 0;
    }

    public static void onSharedNeighbourBlockChange(World world, int x, int y, int z, int hash, Block block)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(x,y,z);
        final int meta = world.getBlockMetadata(x,y,z) & maskMeta;

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
        {
            if (te.getNeighbours().hasNeighbour(d))
            {
                final int nx = x + d.offsetX;
                final int ny = y + d.offsetY;
                final int nz = z + d.offsetZ;

                final Block nBlock = world.getBlock(nx,ny,nz);
                final int nMeta = world.getBlockMetadata(nx, ny, nz) & maskMeta;

                if (neighborCheck(meta, nMeta, nBlock))
                {
                    //Break all things!
                    world.setBlock(x, y, z, te.getTransmutedBlock(), te.getTransmutedMeta(), 0x3);

                    if (!world.isRemote)
                    {
                        if (te.getBlockID().equals(SteamNSteelStructureTE.ORIGIN_ZERO))
                        {
                            ModNetwork.network.sendToAllAround(
                                    new StructureParticlePacket(x, y, z, hash, getdecodedOrientation(meta), isMirrored(meta), StructureParticleChoice.BOOM),
                                    new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 30)
                            );
                        }
                    }

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

    public static void breakStructure(World world, ImmutableTriple<Integer, Integer, Integer> mloc, StructureDefinition sp, Orientation o, boolean isMirrored, boolean isCreative)
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
            final Block block = sp.getBlock(coord.getLX(), coord.getLY(), coord.getLZ());
            final int meta = sp.getBlockMetadata(coord.getLX(), coord.getLY(), coord.getLZ());

            coord.setBlock(world, block == null? Blocks.air : block,
                    localToGlobal(meta, block == null? Blocks.air : block, o, false)
                    , 0x2);
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

    //todo remove function below
    @SafeVarargs
    public static <E> void print(E... a)
    {
        final StringBuilder s = new StringBuilder(a.length);
        for (final E b:a) s.append(b);

        Logger.info(s.toString());
    }
}
