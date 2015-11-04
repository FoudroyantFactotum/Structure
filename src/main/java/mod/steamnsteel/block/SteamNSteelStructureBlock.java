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

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.structure.IStructure.IPatternHolder;
import mod.steamnsteel.structure.IStructure.IStructureAspects;
import mod.steamnsteel.structure.IStructure.IStructureTE;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.coordinates.TripleIterator;
import mod.steamnsteel.structure.net.StructurePacket;
import mod.steamnsteel.structure.net.StructurePacketOption;
import mod.steamnsteel.structure.registry.GeneralBlock.IGeneralBlock;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE;
import mod.steamnsteel.utility.ModNetwork;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import mod.steamnsteel.waila.WailaProvider;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

import static mod.steamnsteel.block.structure.StructureShapeBlock.EMPTY_BOUNDS;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobalCollisionBoxes;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

@Optional.Interface(modid = WailaProvider.WAILA, iface = "mcp.mobius.waila.api.IWailaDataProvider", striprefs = true)
public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IPatternHolder, IStructureAspects, ITileEntityProvider, IWailaDataProvider
{
    public static final TripleCoord ORIGIN = TripleCoord.of(0, 0, 0);
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
        final boolean mirror = false; //todo entity.isSneaking();

        if (mirror)
        {
            meta |= flagMirrored;
            world.setBlockMetadataWithNotify(x, y, z, meta, 0x2);
        }

        formStructure(world, TripleCoord.of(x, y, z), meta, 0x2);
    }

    @Override
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta)
    {
        final Orientation orientation = getdecodedOrientation(meta);
        final boolean isMirrored = isMirrored(meta);

        updateExternalNeighbours(world, TripleCoord.of(x, y, z), getPattern(), orientation, isMirrored, false);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        onSharedNeighbourBlockChange(world, x, y, z, regHash, block);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        final int meta = world.getBlockMetadata(x, y, z);
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x, y, z);
        final boolean isPlayerCreative = player != null && player.capabilities.isCreativeMode; //todo change?

        if (te != null)
        {
            final TripleCoord origin = TripleCoord.of(x, y, z);

            breakStructure(world, origin, getPattern(), getdecodedOrientation(meta), isMirrored(meta), isPlayerCreative);
            updateExternalNeighbours(world, origin, getPattern(), getdecodedOrientation(meta), isMirrored(meta), false);
        } else
        {
            world.setBlockToAir(x, y, z);
        }

        return true;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {
        final int meta = world.getBlockMetadata(x, y, z);

        if (getPattern().getCollisionBoxes() != null)
        {
            localToGlobalCollisionBoxes(x, y, z, aabb, boundingBoxList, getPattern().getCollisionBoxes(), getdecodedOrientation(meta), isMirrored(meta), getPattern().getBlockBounds());
        }
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz)
    {
        return onStructureBlockActivated(world, x, y, z, player, side, sx, sy, sz, ORIGIN, x, y, z);
    }

    @Override
    public boolean onStructureBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz, TripleCoord sbID, int sbx, int sby, int sbz)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
        final float scaleVec = 0.05f;
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x, y, z);

        if (te != null)
        {
            final Orientation o = getdecodedOrientation(meta);
            final boolean isMirrored = isMirrored(meta);

            final TripleIterator itr = getPattern().getStructureItr();

            while (itr.hasNext())
            {
                final TripleCoord local = itr.next();

                //outward Vector
                float xSpeed = 0.0f;
                float ySpeed = 0.0f;
                float zSpeed = 0.0f;

                for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
                {
                    if (!getPattern().hasBlockAt(local, d))
                    {
                        d = localToGlobal(d, o, isMirrored);

                        xSpeed += d.offsetX;
                        ySpeed += d.offsetY;
                        zSpeed += d.offsetZ;
                    }
                }

                final TripleCoord global = localToGlobal(
                        local.x, local.y, local.z,
                        x, y, z,
                        o, isMirrored, getPattern().getBlockBounds());

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
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return EMPTY_BOUNDS;
    }


    //=======================================================
    //       S t r u c t u r e   B l o c k   C o d e
    //=======================================================

    /**
     * checks if meta is mirrored
     *
     * @param meta block metadata
     * @return true if mirrored else false
     */
    public static boolean isMirrored(int meta)
    {
        return (meta & flagMirrored) != 0;
    }

    /**
     * Checks to see if the Structure block (that's called) is still valid in case of a bad break.
     *
     * @param world world obj
     * @param x     x coord
     * @param y     y coord
     * @param z     z coord
     * @param hash  structure hash
     * @param block changed block
     */
    public static void onSharedNeighbourBlockChange(World world, int x, int y, int z, int hash, Block block)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(x, y, z);
        final int meta = world.getBlockMetadata(x, y, z) & maskMeta;
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
        }
    }

    private static boolean neighbourCheck(int meta, int nMeta, Block block)
    {
        return !(meta == nMeta && (block instanceof StructureShapeBlock || block instanceof SteamNSteelStructureBlock));
    }

    public void formStructure(World world, TripleCoord origin, int meta, int flag)
    {
        final TripleIterator itr = getPattern().getStructureItr();
        final Orientation orientation = getdecodedOrientation(meta);
        final boolean isMirrored = isMirrored(meta);

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();

            if (!getPattern().hasBlockAt(local))
            {
                continue;
            }

            final WorldBlockCoord blockCoord = bindLocalToGlobal(origin, local, orientation, isMirrored, getPattern().getBlockBounds());

            world.spawnParticle("explode",
                    blockCoord.getX() + 0.5f,
                    blockCoord.getY() + 0.5f,
                    blockCoord.getZ() + 0.5f, (-0.5 + Math.random()) * 0.25f, 0.05f, (-0.5 + Math.random()) * 0.2f);

            if (!local.equals(ORIGIN))
            {
                blockCoord.setBlock(world, ModBlock.structureShape, meta, flag);
            }

            final IStructureTE ssBlock = (IStructureTE) blockCoord.getTileEntity(world);

            if (ssBlock != null)
            {
                ssBlock.configureBlock(local, regHash);
            } else
            {
                Logger.info("formStructure: Error te: " + local + " : " + blockCoord); //todo sub with proper error fix
            }
        }
    }

    public static void breakStructure(World world, TripleCoord origin, StructureDefinition sd, Orientation orientation, boolean isMirrored, boolean isCreative)
    {
        final TripleIterator itr = sd.getStructureItr();

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();

            if (sd.hasBlockAt(local))
            {
                final WorldBlockCoord blockCoord = bindLocalToGlobal(origin, local, orientation, isMirrored, sd.getBlockBounds());
                final Block worldBlock = blockCoord.getBlock(world);

                final Block block = sd.getBlock(local);
                final int meta = sd.getBlockMetadata(local.x, local.y, local.z);

                if (block != null && !(block instanceof IGeneralBlock))
                {
                    blockCoord.setBlock(world,
                            block,
                            localToGlobal(meta, block, orientation, false),
                            0x2);

                    world.removeTileEntity(blockCoord.getX(), blockCoord.getY(), blockCoord.getZ());
                }
            }
        }
    }

    public static void updateExternalNeighbours(World world, TripleCoord origin, StructureDefinition sd, Orientation orientation, boolean isMirrored, boolean notifyBlocks)
    {
        //neighbour update
        TripleIterator itr = sd.getStructureItr();

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();

            for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
            {
                if (!sd.hasBlockAt(local, d))
                {
                    final WorldBlockCoord blockCoord = bindLocalToGlobal(
                            origin, TripleCoord.of(local, d),
                            orientation, isMirrored, sd.getBlockBounds()
                    );

                    world.notifyBlockOfNeighborChange(
                            blockCoord.getX(),
                            blockCoord.getY(),
                            blockCoord.getZ(),
                            sd.getBlock(local)
                    );

                    if (notifyBlocks)
                    {
                        world.markBlockForUpdate(blockCoord.getX(), blockCoord.getY(), blockCoord.getZ());
                    }
                }
            }
        }
    }

    public static WorldBlockCoord bindLocalToGlobal(
            TripleCoord origin,
            TripleCoord local,
            Orientation o, boolean isMirrored,
            TripleCoord structureSize)
    {
        return WorldBlockCoord.of(
                localToGlobal(
                        local.x, local.y, local.z,
                        origin.x, origin.y, origin.z,
                        o, isMirrored, structureSize)
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

    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z)
    {
        //no op
        return tag;
    }
}
