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
package mod.steamnsteel.tileentity;

import com.google.common.base.Optional;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.inventory.Inventory;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

public class StructureShapeTE extends SteamNSteelStructureTE
{//todo fix :p
    protected Optional<SteamNSteelStructureTE> masterTileEntity = Optional.absent();

    @Override
    protected boolean hasSharedInventory()
    {
        return false;
    }

    @Override
    protected Inventory getSharedInventory()
    {
        return null;
    }

    public void getMasterTE()
    {
        Vec3 mLoc;

        if (!masterLocation.isPresent())
        {
            final int meta = getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord);
            final Orientation o = Orientation.getdecodedOrientation(meta);
            final boolean isMirrored = SteamNSteelStructureBlock.isMirrored(meta);

            mLoc = getMasterLocation(o, isMirrored);
        } else
            mLoc = masterLocation.get();

        final TileEntity mTe = getWorldObj()
                .getTileEntity((int) mLoc.xCoord, (int) mLoc.yCoord, (int) mLoc.zCoord);

        masterTileEntity =  Optional.fromNullable(mTe instanceof SteamNSteelStructureTE? (SteamNSteelStructureTE) mTe: null);
    }

    private boolean hasMasterTEInventory()
    {
        if (masterTileEntity.isPresent())
        {
            if (masterTileEntity.get().isInvalid())
                masterTileEntity = Optional.absent();
            else
                return masterTileEntity.get().hasSharedInventory();
        } else
            getMasterTE();

        return false;
    }

    @Override
    public int getSizeInventory()
    {
        if (hasMasterTEInventory())
            return masterTileEntity.get().getSharedInventory().getSize();

        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        if (hasMasterTEInventory())
            return masterTileEntity.get().getSharedInventory().getStack(slotIndex);

        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int decrAmount)
    {
        if (hasMasterTEInventory())
            return masterTileEntity.get().getSharedInventory().decrStackSize(slotIndex, decrAmount);

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        if (hasMasterTEInventory())
            return masterTileEntity.get().getSharedInventory().getStackOnClosing(slotIndex);

        return null;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack)
    {
        if (hasMasterTEInventory())
            masterTileEntity.get().getSharedInventory().setSlot(slotIndex, itemStack);
    }

    @Override
    public String getInventoryName()
    {
        return null;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (hasMasterTEInventory())
            return masterTileEntity.get().getSharedInventory().getStackSizeMax();

        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void openInventory()
    {

    }

    @Override
    public void closeInventory()
    {

    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
    {
        return hasMasterTEInventory() && masterTileEntity.get().isItemValidForSlot(slotIndex, itemStack);
    }
}
