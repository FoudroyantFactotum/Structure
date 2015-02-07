/*
 * Copyright (c) 2014 Rosie Alexander and Scott Killen.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */
package mod.steamnsteel.structure.registry;

import com.google.common.base.Objects;
import net.minecraftforge.common.util.ForgeDirection;

public class StructureBlockSideAccess
{
    private static final int[] EMPTY_SLOTS = new int[0];
    public static final StructureBlockSideAccess MISSING_SIDE_ACCESS = new StructureBlockSideAccess();

    private int inputSides = 0;
    private int[] accessibleSlots = EMPTY_SLOTS;
    private boolean canInsert = false;
    private boolean canExtract = false;

    private StructureBlockSideAccess()
    {
        //no op
    }

    @SuppressWarnings({"BooleanParameter", "AssignmentToCollectionOrArrayFieldFromParameter"})
    public StructureBlockSideAccess(int inputSides, int[] accessibleSlots, boolean canInsert, boolean canExtract)
    {
        this.inputSides = inputSides;
        if (accessibleSlots != null) this.accessibleSlots = accessibleSlots;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public int[] getAccessibleSlotsFromSide()
    {
        return accessibleSlots;
    }

    public boolean canInsertItem()
    {
        return canInsert;
    }

    public boolean canExtractItem()
    {
        return canExtract;
    }

    public boolean hasSide(ForgeDirection side)
    {
        return (inputSides & side.flag) != 0;
    }

    private static String arrayToString(int[] l)
    {
        final StringBuilder b = new StringBuilder(l.length);

        b.append('[');
        for (int i: l) {
            b.append(i);
            b.append(',');
        }
        b.deleteCharAt(b.length()-1);
        b.append(']');

        return b.toString();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("inputSides", ForgeDirection.getOrientation(inputSides))
                .add("accessibleSlots", arrayToString(accessibleSlots))
                .add("canInsert", canInsert)
                .add("canExtract", canExtract)
                .toString();
    }
}
