package mod.steamnsteel.structure.coordinates;

import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;

public final class BlockPosUtil
{
    public static final int BLOCKPOS_MASK = 0x00FFFFFF;
    public static final int BLOCKPOS_BITLEN = 24;

    public static BlockPos of(int x, int y, int z)
    {
        return new BlockPos(x, y, z);
    }

    public static int toInt(int x, int y, int z)
    {
        return  (((byte) x) << 16) +
                (((byte) y) << 8)  +
                 ((byte) z);
    }

    public static BlockPos fromInt(int val)
    {
        return new BlockPos(
                (byte) (val >> 16),
                (byte) (val >> 8),
                (byte)  val
        );
    }

    public static MutableBlockPos mutOffset(MutableBlockPos pos, EnumFacing facing)
    {
        pos.x += facing.getFrontOffsetX();
        pos.y += facing.getFrontOffsetY();
        pos.z += facing.getFrontOffsetZ();

        return pos;
    }
}

