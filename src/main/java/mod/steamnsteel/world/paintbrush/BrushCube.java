package mod.steamnsteel.world.paintbrush;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Random;
import java.util.Set;

public class BrushCube implements IBrush
{
    private final Random r;
    private final float chance;

    public BrushCube(Random r, float chance)
    {
        this.r = r;
        this.chance = chance;
    }

    @Override
    public void intersectBrushWithWorld(BlockPos corner, Set<BlockPos> path)
    {
        path.add(corner);

        for (final EnumFacing f : EnumFacing.VALUES)
        {
            if (r.nextDouble() < chance)
            {
                path.add(corner.add(f.getDirectionVec()));
            }
        }
    }
}
