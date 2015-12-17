package mod.steamnsteel.world.AlternativeGeneration.PaintBrush;

import net.minecraft.util.BlockPos;

import java.util.Set;

public class BrushCube implements IBrush
{
    final float width;
    final float depth;
    final float height;

    public BrushCube(float width, float height, float depth)
    {
        this.width = width;
        this.depth = depth;
        this.height = height;
    }

    @Override
    public void intersectBrushWithWorld(BlockPos center, Set<BlockPos> path)
    {
        for (int x=0; x<Math.ceil(width); ++x)
        {
            for (int y=0; y<Math.ceil(height); ++y)
            {
                for (int z=0; z<Math.ceil(depth); ++z)
                {
                    path.add(center.add(x,y,z));
                }
            }
        }
    }
}
