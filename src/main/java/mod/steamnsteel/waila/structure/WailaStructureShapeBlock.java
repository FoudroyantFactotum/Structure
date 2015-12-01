package mod.steamnsteel.waila.structure;

import mcp.mobius.waila.api.ITaggedList.ITipList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataAccessorServer;
import mcp.mobius.waila.api.IWailaDataProvider;
import mod.steamnsteel.tileentity.structure.StructureShapeTE;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class WailaStructureShapeBlock implements IWailaDataProvider
{
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return new ItemStack(te.getMasterBlockInstance());
            }
        }

        return null;
    }

    @Override
    public ITipList getWailaHead(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return te.getMasterBlockInstance().getWailaHead(itemStack, currenttip, accessor, config);
            }
        }

        currenttip.clear();

        return currenttip;
    }

    @Override
    public ITipList getWailaBody(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return te.getMasterBlockInstance().getWailaBody(itemStack, currenttip, accessor, config);
            }
        }

        currenttip.clear();

        return currenttip;
    }

    @Override
    public ITipList getWailaTail(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return te.getMasterBlockInstance().getWailaTail(itemStack, currenttip, accessor, config);
            }
        }

        currenttip.clear();

        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(TileEntity te, NBTTagCompound tag, IWailaDataAccessorServer accessor)
    {
        return tag;
    }
}
