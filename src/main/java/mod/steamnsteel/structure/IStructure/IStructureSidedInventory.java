package mod.steamnsteel.structure.IStructure;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public interface IStructureSidedInventory extends ISidedInventory
{
    boolean canStructureInsertItem(int slot, ItemStack item, int side, ImmutableTriple<Integer, Integer, Integer> blockID);
    boolean canStructureExtractItem(int slot, ItemStack item, int side, ImmutableTriple<Integer, Integer, Integer> blockID);

    int[] getAccessibleSlotsFromStructureSide(int side, ImmutableTriple<Integer, Integer, Integer> blockID);
}
