package mod.steamnsteel.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static mod.steamnsteel.block.SteamNSteelStructureBlock.isMirrored;
import static mod.steamnsteel.block.SteamNSteelStructureBlock.print;
import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static mod.steamnsteel.structure.registry.StructureDefinition.dehashLoc;
import static mod.steamnsteel.utility.Orientation.getdecodedOrientation;

public class ExampleTE extends SteamNSteelStructureTE
{
    @Override
    public boolean canStructreInsertItem(int slot, ItemStack item, int side, ImmutableTriple<Byte, Byte, Byte> blockID)
    {
        return false;
    }

    @Override
    public boolean canStructreExtractItem(int slot, ItemStack item, int side, ImmutableTriple<Byte, Byte, Byte> blockID)
    {
        return false;
    }

    @Override
    public int getSizeInventory()
    {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int p_70301_1_)
    {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
    {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
    {

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
        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
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

    public static final String EYE_LIGHTS = "eyeLights";
    public boolean isLightsActive = false;
    public boolean ISACTIVATE = false;
    public int delayTick =0;

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        isLightsActive = nbt.getBoolean(EYE_LIGHTS);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setBoolean(EYE_LIGHTS, isLightsActive);
    }



    @Override
    public void updateEntity()
    {
        final int meta = getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord);

        //print("Direction: ", Orientation.getdecodedOrientation(meta));

        if (getPattern() == null)
        {
            if (!worldObj.isRemote) print("getPattern Error: ID ", getRegHash(), " : ", dehashLoc(getHashedBlockID()), " : ", ImmutableTriple.of(xCoord, yCoord, zCoord));
            return;
        }

        final ImmutableTriple<Integer,Integer,Integer> b1L = localToGlobal(
                -2, -1, -2,
                xCoord, yCoord, zCoord,
                getdecodedOrientation(meta), isMirrored(meta),
                getPattern());

        final ImmutableTriple<Integer,Integer,Integer> b2L = localToGlobal(
                -4, -1, -2,
                xCoord, yCoord, zCoord,
                getdecodedOrientation(meta), isMirrored(meta),
                getPattern());

        final ImmutableTriple<Integer,Integer,Integer> b3L = localToGlobal(
                -3, 1, -3,
                xCoord, yCoord, zCoord,
                getdecodedOrientation(meta), isMirrored(meta),
                getPattern());

        delayTick++;

        if (ISACTIVATE) setLightsBlocks(b1L);
        if (ISACTIVATE) setLightsBlocks(b2L);
        if (ISACTIVATE) setLightsBlocks(b3L);

        if (delayTick%11 == 0) isLightsActive ^= true;
    }

    private void setLightsBlocks(ImmutableTriple<Integer,Integer,Integer> b1L)
    {
        final Block b1 = getWorldObj().getBlock(b1L.getLeft(), b1L.getMiddle(), b1L.getRight());

        if (b1 == Blocks.lit_redstone_lamp || b1 == Blocks.redstone_lamp)
            getWorldObj().setBlock(b1L.getLeft(), b1L.getMiddle(), b1L.getRight(),
                    isLightsActive? Blocks.lit_redstone_lamp: Blocks.redstone_lamp);
    }
}
