package mod.steamnsteel.structure.IStructure;

import mod.steamnsteel.api.plumbing.IPipeTileEntity;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.util.EnumFacing;

public interface IStructurePipe extends IPipeTileEntity
{
    boolean isStructureSideConnected(EnumFacing opposite, TripleCoord blockID);
    boolean tryStructureConnect(EnumFacing opposite, TripleCoord blockID);
    boolean canStructureConnect(EnumFacing opposite, TripleCoord blockID);

    void disconnectStructure(EnumFacing opposite, TripleCoord blockID);
}
