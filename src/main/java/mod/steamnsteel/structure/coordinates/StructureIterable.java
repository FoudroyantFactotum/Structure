package mod.steamnsteel.structure.coordinates;

import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is used as the basis of all iteration through out the structure. It allows the iteration over all the
 * values (designed in this case for (x,y,z))
 */
public class StructureIterable implements Iterator<MutableBlockPos>
{
    private int layerNo, depthNo, rowNo;
    private int layerNoU, depthNoU, rowNoU;
    private int layerNoL, depthNoL, rowNoL;

    //because private constructor
    private MutableBlockPos pos = (MutableBlockPos) BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, BlockPos.ORIGIN).iterator().next();

    private boolean hasNext;

    private StructureIterable()
    {
        this(0,0,0);
    }

    /**
     * (0,0,0) - (x,y,z)
     * @param x upper x-coord
     * @param y upper y-coord
     * @param z upper z-coord
     */
    public StructureIterable(int x, int y, int z)
    {
        this(0,0,0, x,y,z);
    }

    /**
     * (xl,yl,zl) - (xu,yu,zu)
     * @param xl lower x-coord
     * @param yl lower y-coord
     * @param zl lower z-coord
     * @param xu upper x-coord
     * @param yu upper y-coord
     * @param zu upper z-coord
     */
    public StructureIterable(int xl, int yl, int zl, int xu, int yu, int zu)
    {
        rowNoL = xl-1; layerNoL = yl; depthNoL = zl;
        rowNoU = xu; layerNoU = yu; depthNoU = zu;

        rowNo = rowNoL; depthNo = depthNoL; layerNo = layerNoL;

        hasNext = true;

        shiftReadHead();
    }

    private void shiftReadHead()
    {
        while (layerNo < layerNoU)
        {
            while (depthNo < depthNoU)
            {
                if (++rowNo < rowNoU)
                {
                    return;
                }

                rowNo = rowNoL;
                depthNo++;
            }

            depthNo = depthNoL;
            layerNo++;
        }

        hasNext = false;
    }

    @Override
    public boolean hasNext()
    {
        return hasNext;
    }

    @Override
    public MutableBlockPos next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }

        pos.x = rowNo;
        pos.y = layerNo;
        pos.z = depthNo;

        shiftReadHead();

        return pos;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
