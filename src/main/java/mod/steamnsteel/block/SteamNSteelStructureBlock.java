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
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.coordinates.TripleIterator;
import mod.steamnsteel.structure.registry.GeneralBlock.IGeneralBlock;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.waila.WailaProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import static mod.steamnsteel.block.structure.StructureShapeBlock.EMPTY_BOUNDS;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobalBoundingBox;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobalCollisionBoxes;
import static net.minecraft.block.BlockDirectional.*;
import static net.minecraft.block.BlockDirectional.FACING;

@Optional.Interface(modid = WailaProvider.WAILA, iface = "mcp.mobius.waila.api.IWailaDataProvider", striprefs = true)
public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IPatternHolder, IStructureAspects,IWailaDataProvider
{
    public static final TripleCoord ORIGIN = TripleCoord.of(0, 0, 0);

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
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING, propMirror);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        final EnumFacing orientation = (EnumFacing) state.getValue(FACING);
        final boolean mirror = isMirrored(state); //todo entity.isSneaking();

        if (mirror)
        {
            world.setBlockState(pos, state.withProperty(propMirror, Boolean.TRUE), 0x2);
        }

        formStructure(world, TripleCoord.of(pos), state, 0x2);
        updateExternalNeighbours(world, TripleCoord.of(pos), getPattern(), orientation, mirror, false);
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
            final TripleCoord origin = TripleCoord.of(pos);

            breakStructure(world, origin, getPattern(), te.getOrientation(), te.getMirror(), isPlayerCreative, isPlayerSneaking);
            updateExternalNeighbours(world, origin, getPattern(), te.getOrientation(), te.getMirror(), false);
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
                    (EnumFacing)state.getValue(FACING), isMirrored(state),
                    getPattern().getBlockBounds()
            );
        }
    }

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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onStructureBlockActivated(world, pos, player, pos, side, ORIGIN, hitX, hitY, hitZ);
    }

    @Override
    public boolean onStructureBlockActivated(World world, BlockPos pos, EntityPlayer player, BlockPos callPos, EnumFacing side, TripleCoord sbID, float sx, float sy, float sz)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer)
    {
        final float scaleVec = 0.05f;
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(pos);

        if (te != null)
        {
            final TripleIterator itr = getPattern().getStructureItr();

            while (itr.hasNext())
            {
                final TripleCoord local = itr.next();

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

                final TripleCoord global = localToGlobal(
                        local.x, local.y, local.z,
                        pos.getX(), pos.getY(), pos.getZ(),
                        te.getOrientation(), te.getMirror(), getPattern().getBlockBounds());

                spawnBreakParticle(world, te, global, xSpeed * scaleVec, ySpeed * scaleVec, zSpeed * scaleVec);
            }
        }

        return true; //No Destroy Effects
    }

    @SideOnly(Side.CLIENT)
    public abstract void spawnBreakParticle(World world, SteamNSteelStructureTE te, TripleCoord coord, float sx, float sy, float sz);

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

    public static final PropertyBool propMirror = PropertyBool.create("mirror");

    /**
     * checks if meta is mirrored
     *
     * @param state block state
     * @return true if mirrored else false
     */
    public static boolean isMirrored(IBlockState state)
    {
        return (Boolean)state.getValue(propMirror);
    }

    /**
     * Checks to see if the Structure block (that's called) is still valid in case of a bad break.
     *
     * @param world world obj
     * @param x     x coord
     * @param y     y coord
     * @param z     z coord
     * @param hash  structure hash
     * @param pos  position of the changed block
     * @param state block state
     */
    public static void onSharedNeighbourBlockChange(IBlockAccess world, BlockPos pos, int hash, Block neibourBlock, IBlockState state)
    {//todo complete
        /*final IStructureTE te = (IStructureTE) world.getTileEntity(pos);
        final int meta = world.getBlockMetadata(x) & maskMeta;
        final SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(te.getRegHash());

        if (sb == null)
        {
            world.setBlock(x, y, z, Blocks.air, 0, 0x3);
            return;
        }

        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) //local
        {
            if (!sb.getPattern().hasBlockAt(te.getLocal(), d))
            {
                continue;
            }

            d = localToGlobal(d, getdecodedOrientation(meta), isMirrored(meta));

            final int ngx = x + d.offsetX;
            final int ngy = y + d.offsetY;
            final int ngz = z + d.offsetZ;

            final Block nBlock = world.getBlock(ngx, ngy, ngz);
            final int nMeta = world.getBlockMetadata(ngx, ngy, ngz) & maskMeta;

            if (neighbourCheck(meta, nMeta, nBlock))
            {
                //Break all things!
                world.setBlock(x, y, z, te.getTransmutedBlock(), te.getTransmutedMeta(), 0x3);

                if (te.getLocal().equals(ORIGIN))
                {
                    ModNetwork.network.sendToAllAround(
                            new StructurePacket(x, y, z, hash, getdecodedOrientation(meta), isMirrored(meta), StructurePacketOption.BOOM_PARTICLE),
                            new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 30)
                    );
                }

                return;
            }
        }*/
    }

    private static boolean neighbourCheck(int meta, int nMeta, Block block)
    {
        return !(meta == nMeta && (block instanceof StructureShapeBlock || block instanceof SteamNSteelStructureBlock));
    }

    public void formStructure(World world, TripleCoord origin, IBlockState state, int flag)
    {
        final TripleIterator itr = getPattern().getStructureItr();
        final EnumFacing orientation = (EnumFacing) state.getValue(FACING);
        final boolean isMirrored = isMirrored(state);
        final IBlockState shapeState = ModBlock.structureShape
                .getDefaultState()
                .withProperty(propMirror, isMirrored)
                .withProperty(FACING, orientation);

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();

            if (!getPattern().hasBlockAt(local))
            {
                continue;
            }

            final BlockPos blockCoord = bindLocalToGlobal(origin, local, orientation, isMirrored, getPattern().getBlockBounds());

            world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                    blockCoord.getX() + 0.5f,
                    blockCoord.getY() + 0.5f,
                    blockCoord.getZ() + 0.5f, (-0.5 + Math.random()) * 0.25f, 0.05f, (-0.5 + Math.random()) * 0.2f);

            if (!local.equals(ORIGIN))
            {
                world.setBlockState(blockCoord, shapeState, flag);
            }

            final IStructureTE ssBlock = (IStructureTE) world.getTileEntity(blockCoord);

            if (ssBlock != null)
            {
                ssBlock.configureBlock(local, regHash);
            } else
            {
                Logger.info("formStructure: Error te: " + local + " : " + blockCoord + " : " + world.getBlockState(blockCoord)); //todo sub with proper error fix
            }
        }
    }

    public static void breakStructure(World world, TripleCoord origin, StructureDefinition sd, EnumFacing orientation, boolean isMirrored, boolean isCreative, boolean isSneaking)
    {
        final TripleIterator itr = sd.getStructureItr();

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();

            if (sd.hasBlockAt(local))
            {
                final BlockPos blockCoord = bindLocalToGlobal(origin, local, orientation, isMirrored, sd.getBlockBounds());
                final IBlockState worldBlock = world.getBlockState(blockCoord);
                final IBlockState block = sd.getBlock(local);

                if (block != null && !(block instanceof IGeneralBlock))
                {
                    world.removeTileEntity(blockCoord);

                    if (isCreative && !isSneaking)
                    {
                        world.setBlockToAir(blockCoord);
                    }
                    else
                    {
                        world.setBlockState(blockCoord, localToGlobal(block, orientation, isMirrored), 0x2);
                    }
                }
            }
        }
    }

    public static void updateExternalNeighbours(World world, TripleCoord origin, StructureDefinition sd, EnumFacing orientation, boolean mirror, boolean notifyBlocks)
    {
        //neighbour update
        TripleIterator itr = sd.getStructureItr();

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();

            for (EnumFacing d : EnumFacing.VALUES)
            {
                if (!sd.hasBlockAt(local, d))
                {
                    final BlockPos blockCoord = bindLocalToGlobal(
                            origin, TripleCoord.of(local, d),
                            orientation, mirror, sd.getBlockBounds()
                    );

                    if (sd.getBlock(local) instanceof IGeneralBlock)
                    {
                        continue;
                    }

                    world.notifyNeighborsOfStateChange(blockCoord, sd.getBlock(local).getBlock());
                }
            }
        }
    }

    public static BlockPos bindLocalToGlobal(
            TripleCoord origin,
            TripleCoord local,
            EnumFacing orientation, boolean isMirrored,
            TripleCoord structureSize)
    {
        return localToGlobal(
                local.x, local.y, local.z,
                origin.x, origin.y, origin.z,
                orientation, isMirrored, structureSize)
                .getBlockPos();
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
}
