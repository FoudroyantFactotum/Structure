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
package mod.steamnsteel.structure.net;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.coordinates.StructureBlockIterator;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.*;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public class StructureParticlePacket implements IMessage
{

    public StructureParticlePacket()
    {
        //no op
    }

    public StructureParticlePacket(int x, int y, int z, int structureHash, Orientation o, boolean mirror)
    {
        this.x = x;
        this.y = y;
        this.z = z;

        this.structureHash = structureHash;
        orientationAndMirror = o.encode() | (mirror? flagMirrored : 0);

    }

    private int x,y,z;
    private int structureHash;
    private int orientationAndMirror;

    @Override
    public void fromBytes(ByteBuf buf)
    {
        x = ByteBufUtils.readVarInt(buf, 5);
        y = ByteBufUtils.readVarInt(buf, 5);
        z = ByteBufUtils.readVarInt(buf, 5);

        structureHash = ByteBufUtils.readVarInt(buf, 5);
        orientationAndMirror = ByteBufUtils.readVarShort(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeVarInt(buf, x, 5);
        ByteBufUtils.writeVarInt(buf, y, 5);
        ByteBufUtils.writeVarInt(buf, z, 5);

        ByteBufUtils.writeVarInt(buf, structureHash, 5);

        ByteBufUtils.writeVarShort(buf, orientationAndMirror);
    }

    public static class Handler implements IMessageHandler<StructureParticlePacket, IMessage>
    {
        @Override
        public IMessage onMessage(StructureParticlePacket msg, MessageContext ctx)
        {
            final World world = Minecraft.getMinecraft().theWorld;
            final SteamNSteelStructureBlock block = StructureRegistry.getBlock(msg.structureHash);//todo deal with incorrect hash

            int particleCount = 0;

            final float sAjt = 0.05f;

            final TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);

            if (!(te instanceof SteamNSteelStructureTE))
                return null;

            final StructureBlockIterator itr = new StructureBlockIterator(
                    block.getPattern(),
                    Vec3.createVectorHelper(msg.x, msg.y, msg.z),
                    getdecodedOrientation(msg.orientationAndMirror),
                    isMirrored(msg.orientationAndMirror)
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

                if (particleCount++ % 9 == 0)
                    world.spawnParticle("hugeexplosion", coord.getX(), coord.getY(),coord.getZ(), xSpeed * sAjt, ySpeed * sAjt, zSpeed * sAjt);
                //block.spawnBreakParticle(world, (SteamNSteelStructureTE) te, coord, xSpeed * sAjt, ySpeed * sAjt, zSpeed * sAjt);
            }

            return null;
        }
    }
}
