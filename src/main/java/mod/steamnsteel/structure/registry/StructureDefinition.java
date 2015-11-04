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
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.coordinates.TripleIterator;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Structures contain two states. Construction & Form state C->F, F->C.
 *
 * 2D Example: (Ref grid CG-coord)
 *      Construction       |              Form
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
 *     metadata         = metadata of the blocks
 *     toolFormPosition = eg. (2,2) Note: player needs to be able to hit location with the tool else you'll never be
 *                            able to form the structure
 * }
 *
 * S(F) {
 *     sbLayout{Size, SizeHlf} = specify the location of shape blocks within State(Form)
 *     masterPosition          = translation of origin from S(C).origin -> S(F).origin eg. (0,0) -> (1,1)
 *                               Note: location must exist in sbLayout
 *
 *     collisionBoxes          = #imagination
 * }
 *
 * Also watch out. The "getBlock(...)" can return blocks that are unregistered with minecraft (and null (no block). Double check with an
 * (result instanceOf IGeneralBlock) for safety.
 */
public class StructureDefinition
{
    private BitSet sbLayout;
    private TripleCoord sbLayoutSize;
    private TripleCoord sbLayoutSizeHlf;

    private TripleCoord masterPosition;
    private TripleCoord toolFormPosition;

    private Block[][][] blocks;
    private byte[][][] metadata;
    private float[][] collisionBoxes;

    private StructureDefinition()
    {
        //noop
    }

    public StructureDefinition(BitSet sbLayout,
                               TripleCoord sbLayoutSize,
                               TripleCoord masterPosition,
                               TripleCoord toolFormPosition,

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

        sbLayoutSizeHlf = TripleCoord.of(
                sbLayoutSize.x/2,
                sbLayoutSize.y/2,
                sbLayoutSize.z/2);
    }

    public boolean hasBlockAt(TripleCoord loc, ForgeDirection d) { return hasBlockAt(loc.x + d.offsetX, loc.y + d.offsetY, loc.z + d.offsetZ);}
    public boolean hasBlockAt(TripleCoord loc) { return hasBlockAt(loc.x, loc.y, loc.z);}
    public boolean hasBlockAt(int x, int y, int z)
    {
        x += masterPosition.x;
        y += masterPosition.y;
        z += masterPosition.z;

        return  x < sbLayoutSize.x && x > -1 &&
                y < sbLayoutSize.y && y > -1 &&
                z < sbLayoutSize.z && z > -1 &&
                sbLayout.get(y * sbLayoutSize.z * sbLayoutSize.x + z * sbLayoutSize.x + x);

    }

    public Block getBlock(TripleCoord loc) { return getBlock(loc.x, loc.y, loc.z);}
    public Block getBlock(int x, int y, int z)
    {
        x += masterPosition.x;
        y += masterPosition.y;
        z += masterPosition.z;

        if (blocks.length > x        &&
                blocks[x].length > y &&
                blocks[x][y].length > z)
            return blocks[x][y][z];

        return null;
    }

    public int getBlockMetadata(TripleCoord loc) { return getBlockMetadata(loc.x,loc.y, loc.z);}
    public int getBlockMetadata(int x, int y, int z)
    {
        x += masterPosition.x;
        y += masterPosition.y;
        z += masterPosition.z;

        if (metadata.length > x &&
                metadata[x].length > y &&
                metadata[x][y].length > z)
            return metadata[x][y][z];

        return 0;
    }

    public TripleCoord getBlockBounds()
    {
        return sbLayoutSize;
    }

    public TripleCoord getHalfBlockBounds()
    {
        return sbLayoutSizeHlf;
    }

    public TripleCoord getMasterLocation()
    {
        return masterPosition;
    }

    public TripleCoord getToolFormLocation()
    {
        return toolFormPosition;
    }

    public TripleIterator getStructureItr()
    {
        return new TripleIterator(
                -masterPosition.x,                -masterPosition.y,                   -masterPosition.z,
                blocks.length - masterPosition.x, blocks[0].length - masterPosition.y, blocks[0][0].length - masterPosition.z);
    }

    public float[][] getCollisionBoxes()
    {
        return collisionBoxes;
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
