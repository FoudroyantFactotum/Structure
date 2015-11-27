package mod.steamnsteel.structure.IStructure;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public interface IStructureSidedInventory extends ISidedInventory
{
    boolean canStructureInsertItem(int slot, ItemStack item, EnumFacing side, BlockPos local);
    boolean canStructureExtractItem(int slot, ItemStack item, EnumFacing side, BlockPos local);

    int[] getSlotsForStructureFace(EnumFacing side, BlockPos local);
}
