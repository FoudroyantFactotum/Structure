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
package mod.steamnsteel.tileentity.structure;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.IStructure.IStructureSidedInventory;
import mod.steamnsteel.structure.IStructure.IStructureTE;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.registry.StructureRegistry;
import mod.steamnsteel.tileentity.SteamNSteelTE;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.ORIGIN;
import static mod.steamnsteel.block.SteamNSteelStructureBlock.isMirrored;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobalBoundingBox;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public abstract class SteamNSteelStructureTE extends SteamNSteelTE implements IStructureTE, IStructureSidedInventory
{
    static final int maskBlockID = 0x00FFFFFF;

    static final String BLOCK_INFO = "blockINFO";
    static final String BLOCK_PATTERN_NAME = "blockPatternHash";

    private TripleCoord local = ORIGIN;
    private int definitionHash = -1;

    private Optional<AxisAlignedBB> renderBounds = Optional.absent();

    //================================================================
    //                 S T R U C T U R E   C O N F I G
    //================================================================

    @Override
    public SteamNSteelStructureBlock getMasterBlockInstance()
    {
        return StructureRegistry.getStructureBlock(definitionHash);
    }

    @Override
    public TripleCoord getMasterLocation(int meta)
    {
        return TripleCoord.of(xCoord, yCoord, zCoord);
    }

    @Override
    public int getRegHash()
    {
        return definitionHash;
    }

    @Override
    public void configureBlock(TripleCoord local, int definitionHash)
    {
        this.definitionHash = definitionHash;
        this.local = local;
    }

    @Override
    public Block getTransmutedBlock()
    {
        SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(definitionHash);

        if (sb != null)
        {
            Block block = sb.getPattern().getBlock(local);
            return block == null ?
                    Blocks.air :
                    block;
        }

        return Blocks.air;
    }

    @Override
    public int getTransmutedMeta()
    {
        final SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(definitionHash);

        if (sb != null)
        {
            final int meta = getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord);

            return localToGlobal(
                    sb.getPattern().getBlockMetadata(local),
                    getTransmutedBlock(),
                    getdecodedOrientation(meta),
                    isMirrored(meta)
            );
        }

        return 0;
    }

    @Override
    public TripleCoord getLocal()
    {
        return local;
    }

    //================================================================
    //                     I T E M   I N P U T
    //================================================================

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side)
    {
        return canStructureInsertItem(slotIndex, itemStack, side, ORIGIN);
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side)
    {
        return canStructureExtractItem(slotIndex, itemStack, side, ORIGIN);
    }

    //================================================================
    //                            N B T
    //================================================================

    @Override
    public Packet getDescriptionPacket()
    {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        readFromNBT(packet.func_148857_g());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        final int blockInfo = nbt.getInteger(BLOCK_INFO);
        definitionHash = nbt.getInteger(BLOCK_PATTERN_NAME);

        local = TripleCoord.dehashLoc(blockInfo & maskBlockID);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_INFO, local.hashCode());
        nbt.setInteger(BLOCK_PATTERN_NAME, definitionHash);
    }

    //================================================================
    //                    C L I E N T   S I D E
    //================================================================

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        if (!renderBounds.isPresent())
        {
            SteamNSteelStructureBlock sb = StructureRegistry.getStructureBlock(definitionHash);

            if (sb == null)
            {
                return INFINITE_EXTENT_AABB;
            }

            final Orientation o = getdecodedOrientation(getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord));

            renderBounds = Optional.of(localToGlobalBoundingBox(xCoord, yCoord, zCoord, local, sb.getPattern(), o, false));
        }

        return renderBounds.get();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("local", local)
                .add("renderBounds", renderBounds)
                .add("blockPatternHash", definitionHash)
                .toString();
    }
}
