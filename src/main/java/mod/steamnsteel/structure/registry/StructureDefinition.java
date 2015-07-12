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
import mod.steamnsteel.structure.coordinates.TripleIterator;
import net.minecraft.block.Block;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Structures contain two states. Construction & Form state C->F, F->C.
 *
 * 2D Example: (Ref grid CG-coord)
 * b -> block, '-' -> null | none, S -> Structure, H -> Shape
 *
 *   State(Construction)      State(Form)
 *        b,b,b,b               -,-,H,-
 *        b,b,b,b     ====>     -,S,H,-
 *        b,b,b,b               -,-,H,-
 *
 * Var belongs to state:
 *
 * S(C) {
 *     blocks           = (as above)
 *     metadata         = (use your imagination)
 *     toolFormPosition = eg. (2,2) Note: player needs to be able to hit location with the tool
 * }
 *
 * S(F) {
 *     sbLayout{ ,Size, SizeHlf} = specify the location of shape blocks within State(Form)
 *     masterPosition            = translation of origin from S(C).origin -> S(F).origin eg. (0,0) -> (1,1)
 *                                 Note: location must exist in sbLayout
 *     collisionBoxes            = #imagination
 */
public class StructureDefinition
{
    private BitSet sbLayout;
    private ImmutableTriple<Integer, Integer, Integer> sbLayoutSize;
    private ImmutableTriple<Integer, Integer, Integer> sbLayoutSizeHlf;

    private ImmutableTriple<Integer,Integer,Integer> masterPosition;
    private ImmutableTriple<Integer,Integer,Integer> toolFormPosition;

    private Block[][][] blocks;
    private byte[][][] metadata;
    private float[][] collisionBoxes;

    private StructureDefinition()
    {
        //noop
    }

    public StructureDefinition(BitSet sbLayout,
                               ImmutableTriple<Integer, Integer, Integer> sbLayoutSize,
                               ImmutableTriple<Integer,Integer,Integer> masterPosition,
                               ImmutableTriple<Integer,Integer,Integer> toolFormPosition,

                               Block[][][] blocks,
                               byte[][][] metadata,
                               float[][] collisionBoxes)
    {
        this.sbLayout = sbLayout;
        this.sbLayoutSize = sbLayoutSize;

        this.masterPosition = masterPosition;
        this.toolFormPosition = toolFormPosition;

        this.blocks = blocks;
        this.metadata = metadata;
        this.collisionBoxes = collisionBoxes;

        sbLayoutSizeHlf = ImmutableTriple.of(
                sbLayoutSize.getLeft()/2,
                sbLayoutSize.getMiddle()/2,
                sbLayoutSize.getRight()/2);
    }

    public boolean hasBlockAt(int x, int y, int z)
    {
        x = x + masterPosition.getLeft();
        y = y + masterPosition.getMiddle();
        z = z + masterPosition.getRight();

        return  x < sbLayoutSize.getLeft() && x > -1 &&
                y < sbLayoutSize.getMiddle() && y > -1 &&
                z < sbLayoutSize.getRight() && z > -1 &&
                sbLayout.get(y * sbLayoutSize.getRight() * sbLayoutSize.getLeft() + z * sbLayoutSize.getLeft() + x);

    }

    public Block getBlock(int x, int y, int z)
    {
        x = x + masterPosition.getLeft();
        y = y + masterPosition.getMiddle();
        z = z + masterPosition.getRight();

        if (blocks.length > x        &&
                blocks[x].length > y &&
                blocks[x][y].length > z)
            return blocks[x][y][z];

        return null;
    }

    public int getBlockMetadata(int x, int y, int z)
    {
        x = x + masterPosition.getLeft();
        y = y + masterPosition.getMiddle();
        z = z + masterPosition.getRight();

        if (metadata.length > x &&
                metadata[x].length > y &&
                metadata[x][y].length > z)
            return metadata[x][y][z];

        return 0;
    }

    public ImmutableTriple<Integer,Integer,Integer> getBlockBounds()
    {
        return sbLayoutSize;
    }

    public ImmutableTriple<Integer,Integer,Integer> getHalfBlockBounds()
    {
        return sbLayoutSizeHlf;
    }

    public ImmutableTriple<Integer,Integer,Integer> getMasterLocation()
    {
        return masterPosition;
    }

    public ImmutableTriple<Integer,Integer,Integer> getToolFormLocation()
    {
        return toolFormPosition;
    }

    public TripleIterator getConstructionItr()
    {
        return new TripleIterator(blocks.length, blocks[0].length, blocks[0][0].length);
    }

    public TripleIterator getFormItr()
    {
        return new TripleIterator(
                -masterPosition.getLeft(),                -masterPosition.getMiddle(),                   -masterPosition.getRight(),
                blocks.length - masterPosition.getLeft(), blocks[0].length - masterPosition.getMiddle(), blocks[0][0].length - masterPosition.getRight());
    }

    public float[][] getCollisionBoxes()
    {
        return collisionBoxes;
    }

    public static int hashLoc(ImmutableTriple<Byte,Byte,Byte> loc)
    {
        return hashLoc(loc.getLeft(), loc.getMiddle(), loc.getRight());
    }

    public static int hashLoc(int x, int y, int z)
    {
        return  (((byte) x) << 16) +
                (((byte) y) << 8)  +
                 ((byte) z);
    }

    public static ImmutableTriple<Integer,Integer,Integer> dehashLoc(int val)
    {
        //byte used as a mask on vals.
        return ImmutableTriple.of(
                (int) (byte) (val >> 16),
                (int) (byte) (val >> 8),
                (int) (byte)  val
        );
    }

    public String toString(){
        return Objects.toStringHelper(this)
                .add("blocks", Arrays.toString(blocks))
                .add("metadata", Arrays.toString(metadata))
                .add("collisionBoxes", Arrays.toString(collisionBoxes))
                .add("masterPosition", masterPosition)
                .add("toolFormPosition", toolFormPosition)
                .add("sbLayoutSize", sbLayoutSize)
                .add("sbLayout", sbLayout)
                .toString();
    }
}
