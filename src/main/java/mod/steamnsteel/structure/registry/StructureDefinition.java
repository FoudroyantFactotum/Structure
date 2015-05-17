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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializer;
import mod.steamnsteel.structure.StructureDefinitionBuilder;
import mod.steamnsteel.structure.json.JSONStructureDefinition;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.Arrays;
import java.util.BitSet;

public class StructureDefinition
{
    public static final StructureDefinition MISSING_STRUCTURE = new StructureDefinitionBuilder().build();
    private static final JSONStructureDefinition jsonDeserializer = new JSONStructureDefinition();

    public static JsonDeserializer<StructureDefinition> getJsonDeserializer()
    {
        return jsonDeserializer;
    }

    private BitSet[][] sbLayout;
    private boolean cleanUpOnBuild = true;

    private ImmutableTriple<Integer,Integer,Integer> adjustmentCtS;
    private ImmutableTriple<Integer,Integer,Integer> mps;
    private ImmutableTriple<Integer,Integer,Integer> tfps;

    private Block[][][] blocks;
    private byte[][][] metadata;
    private ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> sideAccess;
    private float[][] collisionBoxes;

    private StructureDefinition()
    {
        //noop
    }

    public StructureDefinition(BitSet[][] sbLayout,
                                boolean cleanUpOnBuild,

                                ImmutableTriple<Integer,Integer,Integer> adjustmentCtS,
                                ImmutableTriple<Integer,Integer,Integer> mps,
                                ImmutableTriple<Integer,Integer,Integer> tfps,

                                Block[][][] blocks,
                                byte[][][] metadata,
                                ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> sideAccess,
                                float[][] collisionBoxes)
    {
        this.sbLayout = sbLayout;
        this.cleanUpOnBuild = cleanUpOnBuild;

        this.adjustmentCtS = adjustmentCtS;
        this.mps = mps;
        this.tfps = tfps;

        this.blocks = blocks;
        this.metadata = metadata;
        this.sideAccess = sideAccess;
        this.collisionBoxes = collisionBoxes;
    }

    public Block getBlock(int x, int y, int z)
    {
        x = x + mps.getLeft();
        y = y + mps.getMiddle();
        z = z + mps.getRight();

        if (blocks != null)
            if (blocks.length > y)
                if (blocks[y].length > z)
                    if (blocks[y][z].length > x)
                        return blocks[y][z][x];

        return null;
    }

    public int getBlockMetadata(int x, int y, int z)
    {
        x = x + mps.getLeft();
        y = y + mps.getMiddle();
        z = z + mps.getRight();

        if (metadata != null)
            if (metadata.length > y)
                if (metadata[y].length > z)
                    if (metadata[y][z].length > x)
                        return metadata[y][z][x];

        return 0;
    }

    public StructureBlockSideAccess getSideAccess(int x, int y, int z, ForgeDirection direction)
    {
        x = x + mps.getLeft();
        y = y + mps.getMiddle();
        z = z + mps.getRight();

        if (sideAccess != null)
        {
            final int hash = hashLoc(x, y, z);

            if (sideAccess.containsKey(hash))
                for (StructureBlockSideAccess side: sideAccess.get(hash))
                    if (side.hasSide(direction))
                        return side;
        }

        return StructureBlockSideAccess.MISSING_SIDE_ACCESS;
    }

    public BitSet[][] getBlockLayout()
    {
        return sbLayout;
    }

    public ImmutableTriple<Integer,Integer,Integer> getBlockBounds()
    {
        return sbLayout == null ?
                ImmutableTriple.of(0,0,0):
                ImmutableTriple.of(
                        sbLayout[0][0].length(),
                        sbLayout.length,
                        sbLayout[0].length);
    }

    public ImmutableTriple<Integer,Integer,Integer> getHalfBlockBounds()
    {
        return sbLayout == null ?
                ImmutableTriple.of(0,0,0):
                ImmutableTriple.of(
                        sbLayout[0][0].length()/2,
                        sbLayout.length/2,
                        sbLayout[0].length/2);
    }

    public ImmutableTriple<Integer,Integer,Integer> getMasterLocation()
    {
        return mps;
    }

    public ImmutableTriple<Integer,Integer,Integer> getToolBuildLocation()
    {
        return tfps;
    }

    public static int hashLoc(int x, int y, int z)
    {
        return  (((byte) x) << 16) +
                (((byte) y) << 8)  +
                 ((byte) z);
    }

    public static ImmutableTriple<Byte,Byte,Byte> dehashLoc(int val)
    {
        return ImmutableTriple.of(
                (byte) (val >> 16),
                (byte) (val >> 8),
                (byte)  val
        );
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
                .add("sideAccess", sideAccess)
                .add("adjustmentCtS", adjustmentCtS)
                .add("mps", mps)
                .add("tfps", tfps)
                .add("sbLayout", Arrays.toString(sbLayout))
                .add("cleanUpOnBuild", cleanUpOnBuild)
                .toString();
    }
}
