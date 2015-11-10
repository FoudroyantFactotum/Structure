package mod.steamnsteel.structure.IStructure;

import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IStructureSidedInventory extends ISidedInventory
{
    boolean canStructureInsertItem(int slot, ItemStack item, EnumFacing side, TripleCoord blockID);
    boolean canStructureExtractItem(int slot, ItemStack item, EnumFacing side, TripleCoord blockID);

    int[] getSlotsForStructureFace(EnumFacing side, TripleCoord blockID);
}
