package mod.steamnsteel.structure.IStructure;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public interface IStructureSidedInventory extends ISidedInventory
{
    // values passed in are global space.
    boolean canStructreInsertItem(int slot, ItemStack item, int side, ImmutableTriple<Byte, Byte, Byte> blockID);
    boolean canStructreExtractItem(int slot, ItemStack item, int side, ImmutableTriple<Byte, Byte, Byte> blockID);
}
