package mod.steamnsteel.world.structure.listeners;

import mod.steamnsteel.world.SchematicLoader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class VinesInterceptionEventListener implements SchematicLoader.IPreSetBlockEventListener
{
    @Override
    public void preBlockSet(SchematicLoader.PreSetBlockEvent event)
    {
        if (event.getBlock() != Blocks.vine) { return; }
        final BlockPos worldCoord = event.worldCoord;
        final IBlockState existingBlock = event.world.getBlockState(worldCoord);
        if (existingBlock.getBlock() == Blocks.air || existingBlock.getBlock().isFoliage(event.world, worldCoord)) {
            event.denySet();
        }
    }
}
