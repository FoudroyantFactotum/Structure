package mod.steamnsteel.structure.coordinates;

import net.minecraft.util.AxisAlignedBB;

public class MutableAxisAlignedBB
{
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public MutableAxisAlignedBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static MutableAxisAlignedBB fromBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        return new MutableAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean intersectsWith(AxisAlignedBB other)
    {
        return other.maxX > minX && other.minX < maxX &&
                other.maxY > minY && other.minY < maxY &&
                other.maxZ > minZ && other.minZ < maxZ;
    }

    public AxisAlignedBB getAxisAlignedBB()
    {
        return AxisAlignedBB.fromBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
