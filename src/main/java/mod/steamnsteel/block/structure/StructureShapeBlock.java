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
package mod.steamnsteel.block.structure;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelMachineBlock;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.IStructureTE;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.StructureShapeTE;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.List;
import java.util.Random;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.*;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public final class StructureShapeBlock extends SteamNSteelMachineBlock implements ITileEntityProvider
{
    public static boolean _DEBUG = true;
    public static final String NAME = "structureShape";
    public static final AxisAlignedBB EMPTY_BOUNDS = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

    public StructureShapeBlock()
    {
        setBlockName(NAME);
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return EMPTY_BOUNDS;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(x, y, z);

        if (te != null)
        {
            final int meta = world.getBlockMetadata(x, y, z);
            final ImmutableTriple<Integer, Integer, Integer> mloc = te.getMasterLocation(meta);
            final StructureDefinition sp = te.getPattern();

            localToGlobal(mloc.getLeft(), mloc.getMiddle(), mloc.getRight(),
                    aabb, boundingBoxList, sp.getCollisionBoxes(), getdecodedOrientation(meta), isMirrored(meta), sp.getBlockBounds());
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new StructureShapeTE();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(x, y, z);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                final ImmutableTriple<Integer, Integer, Integer> mloc = te.getMasterLocation(meta);
                return block.addDestroyEffects(world, mloc.getLeft(), mloc.getMiddle(), mloc.getRight(), meta, effectRenderer);
            }
        }

        return true; //No Destroy Effects
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        final int meta = world.getBlockMetadata(x, y, z);
        final IStructureTE te = (IStructureTE) world.getTileEntity(x, y, z);
        final boolean isPlayerCreative = player != null && player.capabilities.isCreativeMode;

        if (te != null)
        {
            breakStructure(world,
                    te.getMasterLocation(meta),
                    te.getPattern(),
                    getdecodedOrientation(meta),
                    isMirrored(meta),
                    isPlayerCreative
            );
        } else
            world.setBlockToAir(x, y, z);

        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz)
    {
        final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(x, y, z);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                final int meta = world.getBlockMetadata(x, y, z);
                final ImmutableTriple<Integer, Integer, Integer> mloc = te.getMasterLocation(meta);

                return block.onStructureBlockActivated(world, mloc.getLeft(), mloc.getMiddle(), mloc.getRight(), player, meta, sx, sy, sz, te.getBlockID(), x, y, z);
            }
        }

        world.setBlockToAir(x, y, z);

        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);
        onSharedNeighbourBlockChange(world, x, y, z, ((StructureShapeTE) world.getTileEntity(x, y, z)).getRegHash(), block);
    }

    //======================================
    //      V i s u a l   D e b u g
    //======================================

    @Override
    public boolean renderAsNormalBlock() {
        return !_DEBUG && super.renderAsNormalBlock();
    }

    @Override
    public int getRenderType()
    {
        return _DEBUG?0:super.getRenderType();
    }

    @Override
    public int getRenderBlockPass() {
        return _DEBUG?1:super.getRenderType();
    }

    @Override
    public boolean isOpaqueCube()
    {
        return !_DEBUG && super.isOpaqueCube();
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return _DEBUG?Blocks.stained_glass.getIcon(side, meta): super.getIcon(side, meta);
    }
}
