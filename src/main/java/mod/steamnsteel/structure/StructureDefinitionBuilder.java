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

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.registry.GameRegistry;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import mod.steamnsteel.structure.coordinates.TripleIterator;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.structure.registry.StructureRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.BitSet;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StructureDefinitionBuilder
{
    private BitSet sbLayout;
    private TripleCoord sbLayoutSize;

    private TripleCoord masterPosition;
    private TripleCoord toolFormPosition;

    private Block[][][] blocks;
    private byte[][][] metadata;
    private float[][] collisionBoxes;

    public StructureDefinition build()
    {
        if(blocks == null)
        {
            throw new StructureDefinitionError("Missing Blocks");
        }

        if (metadata == null)
        {
            metadata = new byte[blocks.length][blocks[0].length][blocks[0][0].length];
        }

        //blocks jagged map test
        for (Block[][] b: blocks)
        {
            if (b.length != blocks[0].length)
            {
                throw new StructureDefinitionError("Construction map jagged");
            }

            for (Block[] bb: b)
                if (bb.length != b[0].length)
                {
                    throw new StructureDefinitionError("Construction map jagged");
                }
        }

        //metadata jagged map test
        for (byte[][] b: metadata)
        {
            if (b.length != metadata[0].length)
            {
                throw new StructureDefinitionError("Metadata map jagged");
            }

            for (byte[] bb: b)
            {
                if (bb.length != b[0].length)
                {
                    throw new StructureDefinitionError("Metadata map jagged");
                }
            }
        }

        if (blocks.length != metadata.length ||
                blocks[0].length != metadata[0].length ||
                blocks[0][0].length != metadata[0][0].length)
            throw new StructureDefinitionError("Block map size != metadata size (" +
                    blocks.length + "," +blocks[0].length+ "," + blocks[0][0].length + ") - " +
                    "(" + metadata.length + "," +metadata[0].length+ "," + metadata[0][0].length + ")");

        if (toolFormPosition == null)
        {
            throw new StructureDefinitionError("tool form location missing");
        }

        //From this point structure definition is valid.
        //correct collision bounds.
        for (float[] bb: collisionBoxes)
        {
            bb[0] -= masterPosition.x; bb[3] -= masterPosition.x;
            bb[1] -= masterPosition.y; bb[4] -= masterPosition.y;
            bb[2] -= masterPosition.z; bb[5] -= masterPosition.z;
        }

        //correct tool form location
        toolFormPosition.x -= masterPosition.x;
        toolFormPosition.y -= masterPosition.y;
        toolFormPosition.z -= masterPosition.z;

        return new StructureDefinition(
                sbLayout,
                sbLayoutSize,
                masterPosition,
                toolFormPosition,
                blocks,
                metadata,
                collisionBoxes);
    }

    private ImmutableMap<Character, Block> representation = ImmutableMap.of();

    /**
     * Define what each character represents within the map
     * @param representation char to unlocalized block name map
     * @exception NullPointerException thrown if block doesn't exist.
     */
    public void assignBlockDefinitions(ImmutableMap<Character, String> representation)
    {
        ImmutableMap.Builder<Character, Block> builder = ImmutableMap.builder();

        for (Character c: representation.keySet())
        {
            final String blockName = representation.get(c);
            final int splitPos = blockName.indexOf(':');

            final Block block = GameRegistry.findBlock(blockName.substring(0, splitPos), blockName.substring(splitPos+1));

            checkNotNull(block, "assignBlockDefinitions.Block does not exist " + blockName);

            builder.put(c, block);
        }

        //default
        builder.put(' ', Blocks.air);
        builder.put('-', StructureRegistry.generalNull);

        this.representation = builder.build();
    }

    /**
     * builds the block array using the representation map and the layout(String[]...)
     * String = x-line
     * String[] = z-line
     * String[]... = y-line
     * @param layer the layout of the blocks.
     * @exception NullPointerException the layout is missing a map
     */
    public void assignConstructionBlocks(String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        blocks = new Block [xsz][ysz][zsz];

        final TripleIterator itr = new TripleIterator(xsz, ysz, zsz);

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();
            final char c = layer[local.y][local.z].charAt(local.x);

            if (!representation.containsKey(c))
            {
                throw new NullPointerException("assignConstructionBlocks.Map missing " + c);
            }

            blocks[local.x][local.y][local.z] = representation.get(c);
        }
    }

    /**
     * Configures the location of the blocks.
     * M => Master block location. Specify only once
     * - => Block position
     *   => No block
     *
     * @param shift translation of S(C).origin to S(F).origin
     * @param layer
     */
    public void setConfiguration(TripleCoord shift, String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        sbLayoutSize = TripleCoord.of(xsz, ysz, zsz);
        sbLayout = new BitSet(xsz * ysz *zsz);

        final TripleIterator itr = new TripleIterator(xsz, ysz, zsz);

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();
            final char c = Character.toUpperCase(layer[local.y][local.z].charAt(local.x));

            switch (c)
            {
                case 'M': // Master block location
                    if (masterPosition == null)
                    {
                        masterPosition = TripleCoord.of(
                                local.x + shift.x,
                                local.y + shift.y,
                                local.z + shift.z
                        );
                    } else
                    {
                        throw new StructureDefinitionError("setConfiguration.Master position defined more then once.");
                    }

                case ' ':
                case '-':
                    sbLayout.set(
                            local.x + local.z * xsz + local.y *zsz*xsz,
                            c != ' ');
                    break;
                default:
                {
                    throw new StructureDefinitionError("setConfiguration.Unknown char '" + c + '\'');
                }
            }
        }
    }

    /**
     * String of hex vals where each char represents a single block
     * @param layer layout of hex values representing the metadata
     */
    public void assignMetadata(String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        metadata = new byte[xsz][ysz][zsz];

        final TripleIterator itr = new TripleIterator(xsz, ysz, zsz);

        while (itr.hasNext())
        {
            final TripleCoord local = itr.next();

            metadata[local.x][local.y][local.z]
                    = Byte.parseByte(
                        String.valueOf(layer[local.y][local.z].charAt(local.x)),
                        16);
        }

    }

    public void assignToolFormPosition(TripleCoord toolFormPosition)
    {
        this.toolFormPosition = toolFormPosition;
    }
    public void setCollisionBoxes(float[]... collisionBoxes)
    {
        this.collisionBoxes = collisionBoxes;
    }

    public static class StructureDefinitionError extends Error
    {
        public StructureDefinitionError(String msg)
        {
            super(msg);
        }
    }
}
