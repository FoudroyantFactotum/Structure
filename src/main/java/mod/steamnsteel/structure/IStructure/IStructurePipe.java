package mod.steamnsteel.structure.IStructure;

import mod.steamnsteel.api.plumbing.IPipeTileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public interface IStructurePipe extends IPipeTileEntity
{
    boolean isStructureSideConnected(EnumFacing opposite, BlockPos local);
    boolean tryStructureConnect(EnumFacing opposite, BlockPos local);
    boolean canStructureConnect(EnumFacing opposite, BlockPos local);

    void disconnectStructure(EnumFacing opposite, BlockPos local);
}
