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
package mod.steamnsteel.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import mod.steamnsteel.structure.registry.StructureBlockSideAccess;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.block.Block;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class StructureDefinitionBuilder
{
    public BitSet[][] sbLayout;
    public boolean cleanUpOnBuild = true;

    public ImmutableTriple<Integer,Integer,Integer> adjustmentCtS = ImmutableTriple.of(0,0,0);
    public ImmutableTriple<Integer,Integer,Integer> mps;

    public Block[][][] blocks;
    public byte[][][] metadata;
    public ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> sideAccess;
    public float[][] collisionBoxes;

    public StructureDefinition build()
    {
        if (blocks == null)
            metadata = null;

        if (sbLayout == null)
        {
            if (blocks != null)
                sbLayout = generate_sbLayout(blocks.length, blocks[0].length, blocks[0][0].length);
            else
                sbLayout = generate_sbLayout(1,1,1);

            mps = null;
        }

        if (sideAccess != null)
            sideAccess = cleanIODefinition(sbLayout, sideAccess);

        if (collisionBoxes == null)
            collisionBoxes = generator_collisionBoxes(1,1,1);

        if (mps == null)
            mps = ImmutableTriple.of(0, 0, 0);

        translateCollisions(collisionBoxes, mps,
                ImmutableTriple.of(
                        sbLayout[0][0].length()/2.0f,
                        sbLayout.length/2.0f,
                        sbLayout[0].length/2.0f)
        );


        return new StructureDefinition(
                sbLayout,
                cleanUpOnBuild,
                adjustmentCtS,
                mps,
                blocks,
                metadata,
                sideAccess,
                collisionBoxes
        );
    }

    private static BitSet[][] generate_sbLayout(int x, int y, int z)
    {
        final BitSet xLine = new BitSet(x);
        final BitSet[] zLine = new BitSet[z];
        final BitSet[][] yLine = new BitSet[y][];

        for(int i=0; i < xLine.length(); ++i)   xLine.set(i);
        for(int i=0; i < zLine.length; ++i)     zLine[i] = xLine;
        for(int i=0; i < yLine.length; ++i)     yLine[i] = zLine;

        return yLine;
    }

    private static float[][] generator_collisionBoxes(int x, int y, int z)
    {
        return new float[][]{{
            0,0,0,
            x,y,z
        }};
    }

    private ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> cleanIODefinition(
            BitSet[][] sbLayout,
            ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> sideAccess
    )
    {
        //TODO optimize StructureBlockSideAccessList
        final Set<Integer> remove = new LinkedHashSet<Integer>(1);

        for (Integer i: sideAccess.keySet())
            //check hash to see if exists in size for optimization
            if (!checkForValidBlockLocation(sbLayout, StructureDefinition.dehashLoc(i)))
                remove.add(i);


        if (!remove.isEmpty())
        {
            //build new ImmutableMap
            Builder<Integer, ImmutableList<StructureBlockSideAccess>> sideAccessReplacement
                    = new Builder<Integer, ImmutableList<StructureBlockSideAccess>>();

            //todo correct logger
            for (Integer i : sideAccess.keySet())
                if (remove.contains(i))
                    Logger.info("Leftover side access @" + StructureDefinition.dehashLoc(i)
                        + " for " + sideAccess.get(i));
                else
                    sideAccessReplacement.put(i, sideAccess.get(i));

            return sideAccessReplacement.build();
        }

        return sideAccess;
    }

    private static void translateCollisions(float[][] collisionBoxes,
                                           ImmutableTriple<Integer,Integer,Integer> mps,
                                           ImmutableTriple<Float, Float, Float> hlfSz)
    {
        final float xShift = mps.getLeft() + hlfSz.getLeft();
        final int   yShift = mps.getMiddle();
        final float zShift = mps.getLeft() + hlfSz.getLeft();

        for (float[] box: collisionBoxes)
        {
            box[0] -= xShift; box[1] -= yShift; box[2] -= zShift;
            box[3] -= xShift; box[4] -= yShift; box[5] -= zShift;
        }
    }

    private static boolean checkForValidBlockLocation(BitSet[][] sbLayout, ImmutableTriple<Byte,Byte,Byte> loc)
    {
        if (sbLayout.length > loc.getMiddle())
            if (sbLayout[loc.getMiddle()].length > loc.getRight())
                if (sbLayout[loc.getMiddle()][loc.getRight()].length() > loc.getLeft())
                    return sbLayout[loc.getMiddle()][loc.getRight()].get(loc.getLeft());

        return false;
    }
}
