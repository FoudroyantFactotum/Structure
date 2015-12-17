package mod.steamnsteel.world.AlternativeGeneration.PaintBrush;

import net.minecraft.util.BlockPos;

import java.util.Set;

public interface IBrush
{
    void intersectBrushWithWorld(BlockPos center, Set<BlockPos> path);
}
