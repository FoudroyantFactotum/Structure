package mod.steamnsteel.structure.registry;

import com.google.common.base.Objects;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import net.minecraftforge.common.util.ForgeDirection;

public final class StructureNeighbours
{
    public static final StructureNeighbours MISSING_NEIGHBOURS = new StructureNeighbours();
    private byte neighbours = 0x0;

    private StructureNeighbours()
    {
        //no op
    }

    public StructureNeighbours(byte neighbours)
    {
        this.neighbours = neighbours;
    }

    public StructureNeighbours(StructureBlockCoord sBlock)
    {
        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            if (sBlock.hasGlobalNeighbour(d))
                neighbours |= d.flag;
    }

    public boolean hasNeighbour(ForgeDirection d)
    {
        return (neighbours & d.flag) != 0;
    }

    public static String toStringNeighbour(int n)
    {
        final StringBuilder builder =
                new StringBuilder(ForgeDirection.VALID_DIRECTIONS.length);

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            builder.append((n & d.flag) == 0 ? ' ' : d.name().charAt(0));

        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        return neighbours;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("neighbours", toStringNeighbour(neighbours))
                .toString();
    }
}
