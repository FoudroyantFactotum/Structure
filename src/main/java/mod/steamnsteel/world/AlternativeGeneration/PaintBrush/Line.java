package mod.steamnsteel.world.AlternativeGeneration.PaintBrush;

import net.minecraft.util.BlockPos;

import java.util.Set;

public class Line implements IDrawWithBrush
{
    private final double angleXY;
    private final double angleYZ;
    private final float length;

    public Line(double angleXY, double angleYZ, float length)
    {
        this.angleXY = angleXY;
        this.angleYZ = angleYZ;
        this.length = length;
    }

    @Override
    public void drawWithBrush(IBrush brush, Set<BlockPos> path, BlockPos startPos)
    {
        final double x = Math.cos(angleXY);
        final double y = Math.sin(angleXY);
        final double z = Math.sin(angleYZ);

        for (int l = 0; l < length; ++l)
        {
            brush.intersectBrushWithWorld(startPos.add(x*l, y*l, z*l), path);
        }
    }
}
