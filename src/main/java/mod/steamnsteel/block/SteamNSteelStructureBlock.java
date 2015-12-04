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

import com.google.common.base.Objects;
import mcp.mobius.waila.api.ITaggedList.ITipList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataAccessorServer;
import mcp.mobius.waila.api.IWailaDataProvider;
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.structure.IStructure.IPatternHolder;
import mod.steamnsteel.structure.IStructure.IStructureAspects;
import mod.steamnsteel.structure.IStructure.IStructureTE;
import mod.steamnsteel.structure.coordinates.BlockPosUtil;
import mod.steamnsteel.structure.net.StructurePacket;
import mod.steamnsteel.structure.net.StructurePacketOption;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE;
import mod.steamnsteel.utility.ModNetwork;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.waila.WailaProvider;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobalCollisionBoxes;
import static net.minecraft.block.BlockDirectional.FACING;

@Optional.Interface(modid = WailaProvider.WAILA, iface = "mcp.mobius.waila.api.IWailaDataProvider", striprefs = true)
public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IPatternHolder, IStructureAspects, IWailaDataProvider
{
    private int regHash = 0;
    private StructureDefinition structureDefinition = null;

    public SteamNSteelStructureBlock()
    {
        setDefaultState(
                this.blockState
                        .getBaseState()
                        .withProperty(FACING, EnumFacing.NORTH)
                        .withProperty(MIRROR, false)
        );
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING, MIRROR);
    }

    public IBlockState getStateFromMeta(int meta)
    {
        final EnumFacing facing = EnumFacing.getHorizontal(meta & 0x3);
        final boolean mirror = (meta & 0x4) != 0;

        return getDefaultState()
                .withProperty(FACING, facing)
                .withProperty(MIRROR, mirror);
    }

    public int getMetaFromState(IBlockState state)
    {
        final EnumFacing facing = getOrientation(state);
        final boolean mirror = getMirror(state);

        return facing.getHorizontalIndex() | (mirror? 1<<2:0);
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        final EnumFacing orientation = getOrientation(state);
        final boolean mirror = getMirror(state);

        formStructure(world, pos, state, 0x2);
        updateExternalNeighbours(world, pos, getPattern(), orientation, mirror, false);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        onSharedNeighbourBlockChange(worldIn, pos, regHash, neighborBlock, state);
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(pos);
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
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
    {
        if (getPattern().getCollisionBoxes() != null)
        {
            localToGlobalCollisionBoxes(
                    pos.getX(), pos.getY(), pos.getZ(),
                    mask, list, getPattern().getCollisionBoxes(),
                    getOrientation(state), getMirror(state),
                    getPattern().getBlockBounds()
            );
        }
    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onStructureBlockActivated(world, pos, player, pos, side, BlockPos.ORIGIN, hitX, hitY, hitZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer)
    {
        final float scaleVec = 0.05f;
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(pos);

        if (te != null)
        {
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

                final BlockPos global = localToGlobal(
                        local.x, local.y, local.z,
                        pos.getX(), pos.getY(), pos.getZ(),
                        te.getOrientation(), te.getMirror(), getPattern().getBlockBounds());

                spawnBreakParticle(world, te, global, xSpeed * scaleVec, ySpeed * scaleVec, zSpeed * scaleVec);
            }
        }

        return true; //No Destroy Effects
    }

    @SideOnly(Side.CLIENT)
    public abstract void spawnBreakParticle(World world, SteamNSteelStructureTE te, BlockPos local, float sx, float sy, float sz);

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
        return true; //No Digging Effects
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
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
        return (Boolean) state.getValue(MIRROR);
    }

    public static EnumFacing getOrientation(IBlockState state)
    {
        return (EnumFacing) state.getValue(FACING);
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
        final SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(te.getRegHash());

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
            final EnumFacing orientation = getOrientation(state);

            final BlockPos nPos = BlockPosUtil.of(pos, localToGlobal(f, getOrientation(state), mirror));
            final IBlockState nState = world.getBlockState(nPos);

            if ((nState.getBlock() instanceof SteamNSteelStructureBlock || nState.getBlock() instanceof StructureShapeBlock) &&
                (state.getBlock()  instanceof SteamNSteelStructureBlock || state.getBlock()  instanceof StructureShapeBlock))
            {
                final boolean nmirror = getMirror(nState);
                final EnumFacing norientation = getOrientation(nState);

                if (mirror == nmirror && orientation == norientation)
                {
                    continue;
                }
            }

            //break as the above simple condition for structure test failed.

            ute.getWorld().setBlockState(pos, te.getTransmutedBlock(), 0x3);

            if (te.getLocal().equals(BlockPos.ORIGIN))
            {
                ModNetwork.network.sendToAllAround(
                        new StructurePacket(pos, hash, orientation, mirror, StructurePacketOption.BOOM_PARTICLE),
                        new NetworkRegistry.TargetPoint(ute.getWorld().provider.getDimensionId(), pos.getX(), pos.getY(), pos.getZ(), 30)
                );
            }

                return;
        }
    }

    public void formStructure(World world, BlockPos origin, IBlockState state, int flag)
    {
        final EnumFacing orientation = getOrientation(state);
        final boolean isMirrored = getMirror(state);
        final IBlockState shapeState = ModBlock.structureShape
                .getDefaultState()
                .withProperty(MIRROR, isMirrored)
                .withProperty(FACING, orientation);

        for (final MutableBlockPos local : getPattern().getStructureItr())
        {
            if (!getPattern().hasBlockAt(local))
            {
                continue;
            }

            final BlockPos blockCoord = bindLocalToGlobal(origin, local, orientation, isMirrored, getPattern().getBlockBounds());

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
                Logger.info("formStructure: Error te: " + new BlockPos(local) + " : " + blockCoord + " : " + world.getBlockState(blockCoord)); //todo sub with proper error fix
            }
        }
    }

    public void breakStructure(World world, BlockPos origin, EnumFacing orientation, boolean isMirrored, boolean isCreative, boolean isSneaking)
    {
        for (final MutableBlockPos local : getPattern().getStructureItr())
        {
            if (getPattern().hasBlockAt(local))
            {
                final BlockPos blockCoord = bindLocalToGlobal(origin, local, orientation, isMirrored, getPattern().getBlockBounds());
                final IBlockState worldBlock = world.getBlockState(blockCoord);
                final IBlockState block = getPattern().getBlock(local);

                if (worldBlock.getBlock() instanceof SteamNSteelStructureBlock || worldBlock.getBlock() instanceof StructureShapeBlock)
                {
                    world.removeTileEntity(blockCoord);

                    world.setBlockState(blockCoord, (isCreative && !isSneaking) ?
                            Blocks.air.getDefaultState() :
                            localToGlobal(block, orientation, isMirrored)
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
                    final BlockPos blockCoord = bindLocalToGlobal(
                            origin, BlockPosUtil.mutOffset(local, d),
                            orientation, mirror, sd.getBlockBounds()
                    );

                    if (sd.getBlock(local) == null)
                    {
                        continue;
                    }

                    world.notifyNeighborsOfStateChange(blockCoord, sd.getBlock(local).getBlock());
                    BlockPosUtil.mutOffset(local, d.getOpposite());
                }
            }
        }
    }

    public static BlockPos bindLocalToGlobal(
            BlockPos origin,
            BlockPos local,
            EnumFacing orientation, boolean isMirrored,
            BlockPos structureSize)
    {
        return localToGlobal(
                local.getX(), local.getY(), local.getZ(),
                origin.getX(), origin.getY(), origin.getZ(),
                orientation, isMirrored, structureSize
        );
    }


    //=======================================================
    //        W a i l a   D a t a   P r o v i d e r
    //=======================================================

    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
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
    }

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
