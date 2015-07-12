package mod.steamnsteel.structure.coordinates;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is used as the basis of all iteration through out the structure. It allows the iteration over all the
 * values (designed in this case for (x,y,z))
 */
public class TripleIterator implements Iterator<ImmutableTriple<Integer, Integer, Integer>>
{
    private int layerNo, depthNo, rowNo;
    private int layerNoU, depthNoU, rowNoU;
    private int layerNoL, depthNoL, rowNoL;

    private boolean hasNext;

    private TripleIterator()
    {
        this(0,0,0);
    }

    /**
     * (0,0,0) - (x,y,z)
     * @param x upper x-coord
     * @param y upper y-coord
     * @param z upper z-coord
     */
    public TripleIterator(int x, int y, int z)
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
    public TripleIterator(int xl, int yl, int zl, int xu, int yu, int zu)
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
                    return;

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
    public ImmutableTriple<Integer, Integer, Integer> next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        final ImmutableTriple<Integer, Integer, Integer> res =
                ImmutableTriple.of(rowNo, layerNo, depthNo);

        shiftReadHead();

        return res;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
