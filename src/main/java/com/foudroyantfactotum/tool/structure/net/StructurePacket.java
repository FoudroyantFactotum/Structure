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
package com.foudroyantfactotum.tool.structure.net;

import io.netty.buffer.ByteBuf;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.StructureRegistry;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static com.foudroyantfactotum.tool.structure.block.StructureBlock.*;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobal;

//todo fix and clean up this packet code
public class StructurePacket implements IMessage
{
    public static final int flagMirrored = 1 << 3;
    public static final int orientationMask = 0x7;

    private BlockPos pos;
    private int structureHash;
    private int orientationAndMirror;
    private StructurePacketOption sc;

    public StructurePacket()
    {
        //no op
    }

    public StructurePacket(BlockPos pos, int structureHash, EnumFacing orientation, boolean mirror, StructurePacketOption sc)
    {
        this.pos = pos;

        this.structureHash = structureHash;
        orientationAndMirror = orientation.ordinal() | (mirror ? flagMirrored : 0);
        this.sc = sc;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        final int x = ByteBufUtils.readVarInt(buf, 5);
        final int y = ByteBufUtils.readVarInt(buf, 5);
        final int z = ByteBufUtils.readVarInt(buf, 5);

        pos = new BlockPos(x,y,z);
        structureHash = ByteBufUtils.readVarInt(buf, 5);
        orientationAndMirror = ByteBufUtils.readVarShort(buf);
        sc = StructurePacketOption.values()[ByteBufUtils.readVarShort(buf)];
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeVarInt(buf, pos.getX(), 5);
        ByteBufUtils.writeVarInt(buf, pos.getY(), 5);
        ByteBufUtils.writeVarInt(buf, pos.getZ(), 5);

        ByteBufUtils.writeVarInt(buf, structureHash, 5);

        ByteBufUtils.writeVarShort(buf, orientationAndMirror);
        ByteBufUtils.writeVarShort(buf, sc.ordinal());
    }

    public static class Handler implements IMessageHandler<StructurePacket, IMessage>
    {
        @Override
        public IMessage onMessage(StructurePacket msg, MessageContext ctx)
        {
            final World world = Minecraft.getMinecraft().theWorld;
            final StructureBlock block = StructureRegistry.getStructureBlock(msg.structureHash);

            if (block == null)
            {
                return null;
            }

            int particleCount = 0;
            final float sAjt = 0.05f;
            final EnumFacing orientation = EnumFacing.VALUES[msg.orientationAndMirror & orientationMask];
            final boolean mirror = (msg.orientationAndMirror & flagMirrored) != 0;

            if (msg.sc == StructurePacketOption.BUILD)
            {
                final IBlockState state = block.getDefaultState()
                        .withProperty(BlockDirectional.FACING, orientation)
                        .withProperty(MIRROR, mirror);

                world.setBlockState(msg.pos, state, 0x2);
                block.formStructure(world, msg.pos, state, 0x2);
                updateExternalNeighbours(world, msg.pos, block.getPattern(), orientation, mirror, true);

                return null;
            }

            for (final MutableBlockPos local : block.getPattern().getStructureItr())
            {
                final BlockPos coord = bindLocalToGlobal(
                        msg.pos, local,
                        orientation, mirror,
                        block.getPattern().getBlockBounds()
                );

                //outward Vector
                float xSpeed = 0.0f;
                float ySpeed = 0.0f;
                float zSpeed = 0.0f;

                for (EnumFacing d :EnumFacing.VALUES)
                {
                    if (!block.getPattern().hasBlockAt(local, d))
                    {
                        d = localToGlobal(d, orientation, mirror);

                        xSpeed += d.getFrontOffsetX();
                        ySpeed += d.getFrontOffsetY();
                        zSpeed += d.getFrontOffsetZ();
                    }
                }

                switch (msg.sc)
                {
                    case BOOM_PARTICLE:
                        if (particleCount++ % 9 != 0)
                        {
                            world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, coord.getX(), coord.getY(), coord.getZ(), xSpeed * sAjt, ySpeed * sAjt, zSpeed * sAjt);
                        }
                }
            }

            return null;
        }
    }
}
