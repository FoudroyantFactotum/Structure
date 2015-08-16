package mod.steamnsteel.waila.structure;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mod.steamnsteel.tileentity.structure.StructureShapeTE;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class WailaStructureShapeBlock implements IWailaDataProvider
{
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            return new ItemStack(te.getMasterBlockInstance());
        }

        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            return te.getMasterBlockInstance().getWailaHead(itemStack, currenttip, accessor, config);
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            return te.getMasterBlockInstance().getWailaBody(itemStack, currenttip, accessor, config);
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            return te.getMasterBlockInstance().getWailaTail(itemStack, currenttip, accessor, config);
        }

        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z)
    {
        return tag;
    }
}
