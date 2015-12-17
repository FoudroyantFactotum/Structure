package mod.steamnsteel.world.AlternativeGeneration.PaintBrush;

import net.minecraft.util.BlockPos;

import java.util.Set;

public interface IDrawWithBrush
{
    void drawWithBrush(IBrush brush, Set<BlockPos> path, BlockPos startPos);
}
