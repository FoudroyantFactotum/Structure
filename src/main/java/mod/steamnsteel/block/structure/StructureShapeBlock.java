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
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import mod.steamnsteel.tileentity.StructureShapeTE;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class StructureShapeBlock extends SteamNSteelMachineBlock implements ITileEntityProvider
{
    public static final String NAME = "structureShape";

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
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                final Vec3 mloc = te.getMasterLocation(world.getBlockMetadata(x,y,z));
                return block.getSelectedBoundingBoxFromPool(world, (int)mloc.xCoord, (int)mloc.yCoord, (int)mloc.zCoord); //todo fix needs static implemetation to fix errors
            }
        }

        return AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1); //TODO null equivelent;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                final Vec3 mloc = te.getMasterLocation(world.getBlockMetadata(x,y,z));
                block.addCollisionBoxesToList(world, (int)mloc.xCoord, (int)mloc.yCoord, (int)mloc.zCoord, aabb, boundingBoxList, entityColliding);
            }
        }

        //no collision
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
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                final Vec3 mloc = te.getMasterLocation(meta);
                return block.addDestroyEffects(world, (int)mloc.xCoord, (int)mloc.yCoord, (int)mloc.zCoord, meta, effectRenderer);
            }
        }

        return true; //No Destroy Effects
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion)
    {
        world.spawnParticle("hugeexplosion", x, y, z, 1,1,1);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block pBlock, int meta)
    {
        if ((SteamNSteelStructureBlock.flagInvalidBreak & meta) != 0) return;

        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            if (block != null)
            {
                final Vec3 mloc = te.getMasterLocation(meta);
                SteamNSteelStructureBlock.breakStructure(world, mloc, te.getPattern(), Orientation.getdecodedOrientation(meta), SteamNSteelStructureBlock.isMirrored(meta));
            }
        }

        super.breakBlock(world, x, y, z, pBlock, meta);
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int meta, float sx, float sy, float sz)
    {
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);

        if (te != null)
        {
            final SteamNSteelStructureBlock block = te.getMasterBlockInstance();

            print("onBlockActivated: ", te);

            if (block != null)
            {
                final Vec3 mloc = te.getMasterLocation(meta);
                return block.onStructureBlockActivated(world, (int)mloc.xCoord, (int)mloc.yCoord, (int)mloc.zCoord, player, meta, sx, sy, sz, te.getBlockID(), x, y, z);
            }
        }

        return false;
    }
}
