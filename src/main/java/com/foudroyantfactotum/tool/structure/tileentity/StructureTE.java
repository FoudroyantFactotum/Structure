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
package com.foudroyantfactotum.tool.structure.tileentity;

import com.foudroyantfactotum.tool.structure.IStructure.IStructureFluidHandler;
import com.foudroyantfactotum.tool.structure.IStructure.IStructureSidedInventory;
import com.foudroyantfactotum.tool.structure.IStructure.IStructureTE;
import com.foudroyantfactotum.tool.structure.StructureRegistry;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.coordinates.BlockPosUtil;
import com.foudroyantfactotum.tool.structure.net.StructurePacket;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.*;
import static net.minecraft.block.BlockDirectional.FACING;

public abstract class StructureTE extends TileEntity implements IStructureTE, IStructureSidedInventory, IStructureFluidHandler
{
    static final String BLOCK_INFO = "blockINFO";
    static final String BLOCK_PATTERN_NAME = "blockPatternHash";

    private BlockPos local = BlockPos.ORIGIN;
    private int definitionHash = -1;

    protected EnumFacing orientation = EnumFacing.NORTH;
    protected boolean mirror = false;

    private Optional<AxisAlignedBB> renderBounds = Optional.absent();

    public StructureTE()
    {
        //noop
    }

    public StructureTE(StructureDefinition sd, EnumFacing orientation, boolean mirror)
    {
        this.orientation = orientation;
        this.mirror = mirror;

        transformDirectionsOnLoad(sd);
    }

    //================================================================
    //                 S T R U C T U R E   C O N F I G
    //================================================================

    @Override
    public StructureBlock getMasterBlockInstance()
    {
        return StructureRegistry.getStructureBlock(definitionHash);
    }

    @Override
    public BlockPos getMasterBlockLocation()
    {
        return pos;
    }

    @Override
    public int getRegHash()
    {
        return definitionHash;
    }

    @Override
    public void configureBlock(BlockPos local, int definitionHash)
    {
        this.definitionHash = definitionHash;
    }

    @Override
    public IBlockState getTransmutedBlock()
    {
        StructureBlock sb = StructureRegistry.getStructureBlock(definitionHash);

        if (sb != null)
        {
            final IBlockState state = worldObj.getBlockState(pos);

            if (state != null && state.getBlock() instanceof StructureBlock)
            {
                final IBlockState block = sb.getPattern().getBlock(local).getBlockState();

                return block == null ?
                        Blocks.air.getDefaultState() :
                        localToGlobal(
                                block,
                                state.getValue(FACING),
                                StructureBlock.getMirror(state)
                        );
            }
        }


        return Blocks.air.getDefaultState();
    }

    @Override
    public BlockPos getLocal()
    {
        return local;
    }

    public EnumFacing getOrientation()
    {
        return orientation;
    }

    public boolean getMirror()
    {
        return mirror;
    }

    //================================================================
    //                     I T E M   I N P U T
    //================================================================

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack itemStack, EnumFacing side)
    {
        return canStructureInsertItem(slotIndex, itemStack, side, local);
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStack, EnumFacing side)
    {
        return canStructureExtractItem(slotIndex, itemStack, side, local);
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {

    }

    //================================================================
    //                  F L U I D   H A N D L E R
    //================================================================

    public static final FluidTankInfo[] emptyFluidTankInfo = {};

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill)
    {
        return structureFill(from, resource, doFill, local);
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
    {
        return structureDrain(from, resource, doDrain, local);
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
    {
        return structureDrain(from, maxDrain, doDrain, local);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid)
    {
        return canStructureFill(from, fluid, local);
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid)
    {
        return canStructureDrain(from, fluid, local);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from)
    {
        return getStructureTankInfo(from, local);
    }

    //================================================================
    //                            N B T
    //================================================================

    @Override
    public Packet getDescriptionPacket()
    {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);

        return new S35PacketUpdateTileEntity(pos, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        final int blockInfo = nbt.getInteger(BLOCK_INFO);

        definitionHash = nbt.getInteger(BLOCK_PATTERN_NAME);

        orientation = EnumFacing.VALUES[blockInfo >> BlockPosUtil.BLOCKPOS_BITLEN & 0x7];
        mirror = (blockInfo >> BlockPosUtil.BLOCKPOS_BITLEN & StructurePacket.flagMirrored) != 0;

        transformDirectionsOnLoad(getMasterBlockInstance().getPattern());
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_INFO, local.hashCode() | (orientation.ordinal() | (mirror ? StructurePacket.flagMirrored:0)) << BlockPosUtil.BLOCKPOS_BITLEN);
        nbt.setInteger(BLOCK_PATTERN_NAME, definitionHash);
    }

    protected void transformDirectionsOnLoad(StructureDefinition sd) { }


    //================================================================
    //                    C L I E N T   S I D E
    //================================================================

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        if (!renderBounds.isPresent())
        {
            final StructureBlock sb = getMasterBlockInstance();

            if (sb == null)
            {
                return INFINITE_EXTENT_AABB;
            }

            final IBlockState state = getWorld().getBlockState(pos);
            final EnumFacing orientation = state.getValue(FACING);
            final boolean mirror = StructureBlock.getMirror(state);

            renderBounds = Optional.of(localToGlobalBoundingBox(pos, local, sb.getPattern(), orientation, mirror));
        }

        return renderBounds.get();
    }

    //================================================================
    //              S t r u c t u r e   H e l p e r s
    //================================================================

    public static boolean isSide(int flag, EnumFacing d)
    {
        return (flag & flagEnumFacing(d)) != 0;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("local", local)
                .add("renderBounds", renderBounds)
                .add("blockPatternHash", definitionHash)
                .add("mirror", mirror)
                .add("orientation", orientation)
                .toString();
    }
}
