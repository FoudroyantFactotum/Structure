/*
 * Copyright (c) 2016 Foudroyant Factotum
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
package com.foudroyantfactotum.tool.structure.block;

import com.foudroyantfactotum.tool.structure.IStructure.ICanMirror;
import com.foudroyantfactotum.tool.structure.IStructure.IStructureTE;
import com.foudroyantfactotum.tool.structure.IStructure.structure.IPatternHolder;
import com.foudroyantfactotum.tool.structure.IStructure.structure.IStructureAspects;
import com.foudroyantfactotum.tool.structure.StructureRegistry;
import com.foudroyantfactotum.tool.structure.coordinates.BlockPosUtil;
import com.foudroyantfactotum.tool.structure.net.StructureNetwork;
import com.foudroyantfactotum.tool.structure.net.StructurePacket;
import com.foudroyantfactotum.tool.structure.net.StructurePacketOption;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import com.foudroyantfactotum.tool.structure.tileentity.StructureTE;
import com.foudroyantfactotum.tool.structure.waila.WailaProvider;
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import static com.foudroyantfactotum.tool.structure.block.StructureShapeBlock.DIRECTION;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.*;

@Optional.Interface(modid = WailaProvider.WAILA, iface = "mcp.mobius.waila.api.IWailaDataProvider", striprefs = true)
public abstract class StructureBlock extends Block implements IPatternHolder, IStructureAspects, ICanMirror//, IWailaDataProvider
{
    private int regHash = 0;
    private StructureShapeBlock shapeBlock = null;
    private StructureDefinition structureDefinition = null;
    private final boolean canMirror;

    public StructureBlock(boolean canMirror)
    {
        super(Material.piston);
        this.canMirror = canMirror;
        setSoundType(SoundType.STONE);
        setHardness(0.5f);

        IBlockState defaultState = this.blockState
                .getBaseState()
                .withProperty(DIRECTION, EnumFacing.NORTH);

        if (canMirror)
        {
            defaultState = defaultState.withProperty(MIRROR, false);
        }

        setDefaultState(defaultState);
    }

    public void setStructureDefinition(StructureDefinition d, StructureShapeBlock b, int h)
    {
        regHash = h;
        shapeBlock = b;
        structureDefinition = d;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        if (canMirror){
            return new BlockStateContainer(this, DIRECTION, MIRROR);
        }

        return new BlockStateContainer(this, DIRECTION);
    }

    public IBlockState getStateFromMeta(int meta)
    {
        final EnumFacing facing = EnumFacing.getHorizontal(meta & 0x3);

        IBlockState state = getDefaultState().withProperty(DIRECTION, facing);

        if (canMirror)
        {
            state = state.withProperty(MIRROR, (meta & 0x4) != 0);
        }

        return state;
    }

    public int getMetaFromState(IBlockState state)
    {
        final EnumFacing facing = state.getValue(DIRECTION);
        final boolean mirror = getMirror(state);

        if (canMirror)
        {
            return facing.getHorizontalIndex() | (mirror ? 1 << 2 : 0);
        } else {
            return facing.getHorizontalIndex();
        }
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        // total immobility and stop pistons
        return EnumPushReaction.BLOCK;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        final EnumFacing orientation = state.getValue(DIRECTION);
        final boolean mirror = getMirror(state);

        formStructure(world, pos, state, 0x2);
        updateExternalNeighbours(world, pos, getPattern(), orientation, mirror, false);
    }

    @Override
    public boolean onBlockEventReceived(World world, BlockPos pos, IBlockState blockState, int eventId, int eventParameter)
    {
        super.onBlockEventReceived(world, pos, blockState, eventId, eventParameter);
        final TileEntity te = world.getTileEntity(pos);
        return te != null && te.receiveClientEvent(eventId, eventParameter);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        onSharedNeighbourBlockChange(worldIn, pos, regHash, neighborBlock, state);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        final StructureTE te = (StructureTE) world.getTileEntity(pos);
        final boolean isPlayerCreative = player != null && player.capabilities.isCreativeMode;
        final boolean isPlayerSneaking = player != null && player.isSneaking();

        if (te != null)
        {
            breakStructure(world, pos, te.getOrientation(), te.getMirror(), isPlayerCreative, isPlayerSneaking);
            updateExternalNeighbours(world, pos, getPattern(), te.getOrientation(), te.getMirror(), false);
        } else
        {
            world.setBlockToAir(pos);
        }

        return true;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entityIn)
    {
        if (getPattern().getCollisionBoxes() != null)
        {
            localToGlobalCollisionBoxes(
                    pos.getX(), pos.getY(), pos.getZ(),
                    mask, list, getPattern().getCollisionBoxes(),
                    state.getValue(DIRECTION), getMirror(state),
                    getPattern().getBlockBounds()
            );
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onStructureBlockActivated(world, pos, player, pos, side, BlockPos.ORIGIN, hitX, hitY, hitZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer)
    {
        final float scaleVec = 0.05f;
        final TileEntity ute = world.getTileEntity(pos);

        if (ute instanceof StructureTE)
        {
            final StructureTE te = (StructureTE) ute;

            for (MutableBlockPos local : getPattern().getStructureItr())
            {
                //outward Vector
                float xSpeed = 0.0f;
                float ySpeed = 0.0f;
                float zSpeed = 0.0f;

                for (EnumFacing d : EnumFacing.VALUES)
                {
                    if (!getPattern().hasBlockAt(local, d))
                    {
                        d = localToGlobal(d, te.getOrientation(), te.getMirror());

                        xSpeed += d.getFrontOffsetX();
                        ySpeed += d.getFrontOffsetY();
                        zSpeed += d.getFrontOffsetZ();
                    }
                }

                mutLocalToGlobal(local, pos, te.getOrientation(), te.getMirror(), getPattern().getBlockBounds());

                spawnBreakParticle(world, te, local, xSpeed * scaleVec, ySpeed * scaleVec, zSpeed * scaleVec);
            }
        }

        return true; //No Destroy Effects
    }

    @SideOnly(Side.CLIENT)
    public abstract void spawnBreakParticle(World world, StructureTE te, BlockPos local, float sx, float sy, float sz);

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, EffectRenderer effectRenderer)
    {
        return super.addHitEffects(state, worldObj, target, effectRenderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
    {
        //return EMPTY_BOUNDS;
        return world.getTileEntity(pos).getRenderBoundingBox();
    }


    //=======================================================
    //       S t r u c t u r e   B l o c k   C o d e
    //=======================================================

    public static final PropertyBool MIRROR = PropertyBool.create("mirror");

    public static boolean getMirror(IBlockState state)
    {
        return ((ICanMirror) state.getBlock()).canMirror() && state.getValue(MIRROR);
    }

    @Override
    public boolean canMirror()
    {
        return canMirror;
    }

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
    public boolean onStructureBlockActivated(World world, BlockPos pos, EntityPlayer player, BlockPos callPos, EnumFacing side, BlockPos local, float sx, float sy, float sz)
    {
        return false;
    }

    public static void onSharedNeighbourBlockChange(IBlockAccess world, BlockPos pos, int hash, Block neighbourBlock, IBlockState state)
    {
        final TileEntity ute = world.getTileEntity(pos);

        if (!(ute instanceof IStructureTE))
        {
            return;
        }

        final IStructureTE te = (IStructureTE) ute;
        final StructureBlock sb = StructureRegistry.getStructureBlock(te.getRegHash());

        if (sb == null)
        {
            ute.getWorld().setBlockToAir(pos);
            return;
        }

        for (final EnumFacing f : EnumFacing.VALUES)
        {
            if (!sb.getPattern().hasBlockAt(te.getLocal(), f))
            {
                continue;
            }

            final boolean mirror = getMirror(state);
            final EnumFacing orientation = state.getValue(DIRECTION);

            final BlockPos nPos = BlockPosUtil.of(pos, localToGlobal(f, orientation, mirror));
            final IBlockState nState = world.getBlockState(nPos);

            if ((nState.getBlock() instanceof StructureBlock || nState.getBlock() instanceof StructureShapeBlock) &&
                (state.getBlock()  instanceof StructureBlock || state.getBlock()  instanceof StructureShapeBlock))
            {
                final boolean nmirror = getMirror(nState);
                final EnumFacing norientation = nState.getValue(DIRECTION);

                if (mirror == nmirror && orientation == norientation)
                {
                    continue;
                }
            }

            //break as the above simple condition for structure test failed.

            ute.getWorld().setBlockState(pos, te.getTransmutedBlock(), 0x3);

            if (te.getLocal().equals(BlockPos.ORIGIN))
            {
                StructureNetwork.network.sendToAllAround(
                        new StructurePacket(pos, hash, orientation, mirror, StructurePacketOption.BOOM_PARTICLE),
                        new NetworkRegistry.TargetPoint(ute.getWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 30)
                );
            }

            return;
        }
    }

    public void formStructure(World world, BlockPos origin, IBlockState state, int flag)
    {
        final EnumFacing orientation = state.getValue(DIRECTION);
        final boolean mirror = getMirror(state);
        IBlockState shapeState = shapeBlock
                .getDefaultState()
                .withProperty(DIRECTION, orientation);

        if (canMirror)
        {
            shapeState = shapeState.withProperty(MIRROR, mirror);
        }

        for (final MutableBlockPos local : getPattern().getStructureItr())
        {
            if (!getPattern().hasBlockAt(local))
            {
                continue;
            }

            final BlockPos blockCoord = bindLocalToGlobal(origin, local, orientation, mirror, getPattern().getBlockBounds());

            world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                    blockCoord.getX() + 0.5f,
                    blockCoord.getY() + 0.5f,
                    blockCoord.getZ() + 0.5f, (-0.5 + Math.random()) * 0.25f, 0.05f, (-0.5 + Math.random()) * 0.2f);

            if (!local.equals(BlockPos.ORIGIN))
            {
                world.setBlockState(blockCoord, shapeState, flag);
            }

            final IStructureTE ssBlock = (IStructureTE) world.getTileEntity(blockCoord);

            if (ssBlock != null)
            {
                ssBlock.configureBlock(new BlockPos(local), regHash);
            } else
            {
                world.setBlockToAir(blockCoord);
                return;
            }
        }
    }

    public void breakStructure(World world, BlockPos origin, EnumFacing orientation, boolean mirror, boolean isCreative, boolean isSneaking)
    {
        for (final MutableBlockPos local : getPattern().getStructureItr())
        {
            if (getPattern().hasBlockAt(local))
            {
                final IBlockState block = getPattern().getBlock(local).getBlockState();
                mutLocalToGlobal(local, origin, orientation, mirror, getPattern().getBlockBounds());
                final IBlockState worldBlock = world.getBlockState(local);

                if (worldBlock.getBlock() instanceof StructureBlock || worldBlock.getBlock() instanceof StructureShapeBlock)
                {
                    world.removeTileEntity(local);

                    world.setBlockState(new BlockPos(local), (isCreative && !isSneaking) ?
                            Blocks.air.getDefaultState() :
                            localToGlobal(block, orientation, mirror)
                            , 0x2);
                }
            }
        }
    }

    public static void updateExternalNeighbours(World world, BlockPos origin, StructureDefinition sd, EnumFacing orientation, boolean mirror, boolean notifyBlocks)
    {
        for (final MutableBlockPos local : sd.getStructureItr())
        {
            for (EnumFacing d : EnumFacing.VALUES)
            {
                if (!sd.hasBlockAt(local, d))
                {
                    final IBlockState updatedBlock = sd.getBlock(local).getBlockState();

                    if (updatedBlock == null)
                    {
                        continue;
                    }

                    final MutableBlockPos mutLocal = BlockPosUtil.newMutBlockPos(local);
                    BlockPosUtil.mutOffset(mutLocal, d);

                    mutLocalToGlobal(
                            mutLocal,
                            origin,
                            orientation, mirror,
                            sd.getBlockBounds()
                    );

                    world.notifyNeighborsOfStateChange(mutLocal, updatedBlock.getBlock());
                }
            }
        }
    }

    public static BlockPos bindLocalToGlobal(
            BlockPos origin,
            BlockPos local,
            EnumFacing orientation, boolean mirror,
            BlockPos structureSize)
    {
        return localToGlobal(
                local.getX(), local.getY(), local.getZ(),
                origin.getX(), origin.getY(), origin.getZ(),
                orientation, mirror, structureSize
        );
    }


    //=======================================================
    //        W a i l a   D a t a   P r o v i d e r
    //=======================================================

/*    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        //no op
        return null;
    }

    @Override
    public ITipList getWailaHead(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    @Override
    public ITipList getWailaBody(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    @Override
    public ITipList getWailaTail(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(TileEntity te, NBTTagCompound tag, IWailaDataAccessorServer accessor)
    {
        return null;
    }*/

    //=======================================================
    //                     C l a s s
    //=======================================================

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("Structure Definition", getPattern())
                .toString();
    }
}
