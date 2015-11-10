package mod.steamnsteel.waila.structure;

import mcp.mobius.waila.api.ITaggedList.ITipList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataAccessorServer;
import mcp.mobius.waila.api.IWailaDataProvider;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class WailaStructureBlock implements IWailaDataProvider
{
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public ITipList getWailaHead(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return ((SteamNSteelStructureBlock)accessor.getBlock()).getWailaHead(itemStack, currenttip, accessor, config);
    }

    @Override
    public ITipList getWailaBody(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return ((SteamNSteelStructureBlock)accessor.getBlock()).getWailaBody(itemStack, currenttip, accessor, config);
    }

    @Override
    public ITipList getWailaTail(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return ((SteamNSteelStructureBlock)accessor.getBlock()).getWailaTail(itemStack, currenttip, accessor, config);
    }

    @Override
    public NBTTagCompound getNBTData(TileEntity te, NBTTagCompound tag, IWailaDataAccessorServer accessor)
    {
        return tag;//?
    }
}
