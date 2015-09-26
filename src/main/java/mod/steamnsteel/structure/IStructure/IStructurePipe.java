package mod.steamnsteel.structure.IStructure;

import mod.steamnsteel.api.plumbing.IPipeTileEntity;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraftforge.common.util.ForgeDirection;

public interface IStructurePipe extends IPipeTileEntity
{
    boolean isStructureSideConnected(ForgeDirection opposite, TripleCoord blockID);
    boolean tryStructureConnect(ForgeDirection opposite, TripleCoord blockID);
    boolean canStructureConnect(ForgeDirection opposite, TripleCoord blockID);

    void disconnectStructure(ForgeDirection opposite, TripleCoord blockID);
}
