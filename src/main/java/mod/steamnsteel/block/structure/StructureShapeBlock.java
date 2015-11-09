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

import mod.steamnsteel.block.SteamNSteelMachineBlock;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.IStructure.IStructureTE;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.tileentity.structure.StructureShapeTE;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.*;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobalCollisionBoxes;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public class StructureShapeBlock extends SteamNSteelMachineBlock implements ITileEntityProvider
{
    public static boolean _DEBUG = false;
    public static final String NAME = "structureShape";
    public static final AxisAlignedBB EMPTY_BOUNDS = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);

    public StructureShapeBlock()
    {
        setUnlocalizedName(NAME);
        setDefaultState(this.blockState.getBaseState().withProperty(BlockDirectional.FACING, EnumFacing.NORTH));
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
    {
        return EMPTY_BOUNDS;
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(pos);

        if (te != null)
        {
            final TripleCoord mloc = te.getMasterBlockLocation();
            final SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(te.getRegHash());

            if (sb == null || sb.getPattern().getCollisionBoxes() == null)
            {
                return;
            }

            localToGlobalCollisionBoxes(mloc.x, mloc.y, mloc.z,
                    mask, list, sb.getPattern().getCollisionBoxes(), getdecodedOrientation(state), isMirrored(state), sb.getPattern().getBlockBounds());
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new StructureShapeTE();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(pos);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                return block.addDestroyEffects(world, te.getMasterBlockLocationMinecraft(), effectRenderer);
            }
        }

        return true; //No Destroy Effects
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        final IBlockState state = world.getBlockState(pos);
        final IStructureTE te = (IStructureTE) world.getTileEntity(pos);
        final boolean isPlayerCreative = player != null && player.capabilities.isCreativeMode;

        final SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(te.getRegHash());

        if (sb != null)
        {
            breakStructure(world,
                    te.getMasterBlockLocation(),
                    sb.getPattern(),
                    getdecodedOrientation(state),
                    isMirrored(state),
                    isPlayerCreative
            );
            updateExternalNeighbours(world,
                    te.getMasterBlockLocation(),
                    sb.getPattern(),
                    getdecodedOrientation(state),
                    isMirrored(state),
                    false
            );

        } else
        {
            world.setBlockToAir(pos);
        }

        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float sx, float sy, float sz)
    {
        final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(pos);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                final TripleCoord mloc = te.getMasterBlockLocation();
                final BlockPos mbp = new BlockPos(mloc.x, mloc.y, mloc.z);

                return block.onStructureBlockActivated(world, mbp, player, pos, te.getLocal(), sx, sy, sz);
            }
        }

        world.setBlockToAir(pos);

        return false;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbourBlock)
    {
        onSharedNeighbourBlockChange(world, pos, ((StructureShapeTE) world.getTileEntity(pos)).getRegHash(), world.getBlockState(pos).getBlock());
    }

    //======================================
    //      V i s u a l   D e b u g
    //======================================
/*
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
    }*/
}
