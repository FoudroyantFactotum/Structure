package mod.steamnsteel.world.paintbrush;

import net.minecraft.util.BlockPos;

import java.util.Set;

public interface IBrush
{
    void intersectBrushWithWorld(BlockPos center, Set<BlockPos> path);
}
