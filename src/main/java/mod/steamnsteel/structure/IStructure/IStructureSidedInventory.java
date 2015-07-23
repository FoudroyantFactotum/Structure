package mod.steamnsteel.structure.IStructure;

import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public interface IStructureSidedInventory extends ISidedInventory
{
    boolean canStructureInsertItem(int slot, ItemStack item, int side, TripleCoord blockID);
    boolean canStructureExtractItem(int slot, ItemStack item, int side, TripleCoord blockID);

    int[] getAccessibleSlotsFromStructureSide(int side, TripleCoord blockID);
}
