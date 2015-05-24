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
import mod.steamnsteel.structure.registry.StructureBlockSideAccess;
import mod.steamnsteel.structure.registry.StructureDefinition;
import net.minecraft.block.Block;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.BitSet;

public final class StructureDefinitionBuilder
{
    public BitSet sbLayout;
    public ImmutableTriple<Integer, Integer, Integer> sbLayoutSize;
    public boolean cleanUpOnBuild = true;

    public ImmutableTriple<Integer,Integer,Integer> adjustmentCtS = ImmutableTriple.of(0,0,0);
    public ImmutableTriple<Integer,Integer,Integer> mps;
    public ImmutableTriple<Integer,Integer,Integer> tfps;//todo at constrains test

    public Block[][][] blocks;
    public byte[][][] metadata;
    public ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> sideAccess;
    public float[][] collisionBoxes;

    private int xzSize;

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

        xzSize = sbLayoutSize.getLeft()*sbLayoutSize.getRight();

        if (sideAccess != null)
            sideAccess = cleanIODefinition(sbLayout, sideAccess);

        if (collisionBoxes == null)
            collisionBoxes = generator_collisionBoxes(1,1,1);

        if (mps == null)
            mps = ImmutableTriple.of(0, 0, 0);

        if (tfps == null)
            tfps = mps;

        translateCollisions(collisionBoxes, mps,
                ImmutableTriple.of(
                        sbLayoutSize.getLeft()/2.0f,
                        sbLayoutSize.getMiddle()/2.0f,
                        sbLayoutSize.getRight()/2.0f)
        );


        return new StructureDefinition(
                sbLayout,
                sbLayoutSize,
                cleanUpOnBuild,
                adjustmentCtS,
                mps,
                tfps,
                blocks,
                metadata,
                sideAccess,
                collisionBoxes
        );
    }

    private BitSet generate_sbLayout(int x, int y, int z)
    {
        final BitSet line = new BitSet(x*y*z);

        for(int i=0; i < line.length(); ++i)   line.set(i);

        sbLayoutSize = ImmutableTriple.of(x,y,z);

        return line;
    }

    private static float[][] generator_collisionBoxes(int x, int y, int z)
    {
        return new float[][]{{
            0,0,0,
            x,y,z
        }};
    }

    private ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> cleanIODefinition(
            BitSet sbLayout,
            ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> sideAccess
    )
    {
        //TODO optimize StructureBlockSideAccessList
        /*final Set<Integer> remove = new LinkedHashSet<Integer>(1);

        for (Integer i: sideAccess.keySet())
            //check hash to see if exists in size for optimization
            if (!checkForValidBlockLocation(sbLayout, dehashLoc(i)))
                remove.add(i);


        if (!remove.isEmpty())
        {
            //build new ImmutableMap
            Builder<Integer, ImmutableList<StructureBlockSideAccess>> sideAccessReplacement
                    = new Builder<Integer, ImmutableList<StructureBlockSideAccess>>();

            //todo correct logger
            for (Integer i : sideAccess.keySet())
                if (remove.contains(i))
                    Logger.info("Leftover side access @" + dehashLoc(i)
                        + " for " + sideAccess.get(i));
                else
                    sideAccessReplacement.put(i, sideAccess.get(i));

            return sideAccessReplacement.build();
        }*/

        return sideAccess;
    }

    private static void translateCollisions(float[][] collisionBoxes,
                                           ImmutableTriple<Integer,Integer,Integer> mps,
                                           ImmutableTriple<Float, Float, Float> hlfSz)
    {
        /*final float xShift = mps.getLeft() + hlfSz.getLeft();
        final int   yShift = mps.getMiddle();
        final float zShift = mps.getLeft() + hlfSz.getLeft();

        for (float[] box: collisionBoxes)
        {
            box[0] -= xShift; box[1] -= yShift; box[2] -= zShift;
            box[3] -= xShift; box[4] -= yShift; box[5] -= zShift;
        }*/
    }

    private boolean checkForValidBlockLocation(BitSet sbLayout, ImmutableTriple<Byte,Byte,Byte> loc)
    {
        if (sbLayoutSize.getMiddle() > loc.getMiddle())
            if (sbLayoutSize.getRight() > loc.getRight())
                if (sbLayoutSize.getLeft() > loc.getLeft())
                    return sbLayout.get(loc.getLeft() + sbLayoutSize.getLeft()*loc.getRight() + xzSize*loc.getMiddle());

        return false;
    }
}
