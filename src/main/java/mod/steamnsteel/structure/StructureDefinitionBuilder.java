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
import com.google.common.collect.ImmutableMap.Builder;
import mod.steamnsteel.structure.coordinates.BlockPosUtil;
import mod.steamnsteel.structure.registry.StructureDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;

import java.util.BitSet;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StructureDefinitionBuilder
{
    private BitSet sbLayout;
    private BlockPos sbLayoutSize;

    private BlockPos masterPosition;
    private BlockPos toolFormPosition;

    private IBlockState[][][] states;
    private float[][] collisionBoxes;

    public StructureDefinition build()
    {
        if(states == null)
        {
            throw new StructureDefinitionError("Missing block states");
        }

        //blocks jagged map test
        for (final IBlockState[][] b: states)
        {
            if (b.length != states[0].length)
            {
                throw new StructureDefinitionError("Construction map jagged");
            }

            for (final IBlockState[] bb: b)
            {
                if (bb.length != b[0].length)
                {
                    throw new StructureDefinitionError("Construction map jagged");
                }
            }
        }

        //state jagged map test
        if (state != null)
        {
            for (final String[][] s : state)
            {
                if (s.length != state[0].length)
                {
                    throw new StructureDefinitionError("Construction map jagged");
                }

                for (final String[] ss : s)
                {
                    if (ss.length != s[0].length)
                    {
                        throw new StructureDefinitionError("Construction map jagged");
                    }
                }
            }

            if (!(
                    states.length == state.length &&
                            states[0].length == state[0].length &&
                            states[0][0].length == state[0][0].length
            ))
                throw new StructureDefinitionError("block/state sizing mismatch");
        }

        if (toolFormPosition == null)
        {
            throw new StructureDefinitionError("tool form location missing");
        }

        //-------------------------------------------------------
        //Correct data and align it to the inner data structures.
        //-------------------------------------------------------
        final int xsz = states.length;
        final int ysz = states[0].length;
        final int zsz = states[0][0].length;

        for (final MutableBlockPos local : (Iterable<MutableBlockPos>) BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            states[local.x][local.y][local.z] = getBlockState(local);
        }

        //correct collision bounds.
        for (float[] bb: collisionBoxes)
        {
            bb[0] -= masterPosition.getX(); bb[3] -= masterPosition.getX();
            bb[1] -= masterPosition.getY(); bb[4] -= masterPosition.getY();
            bb[2] -= masterPosition.getZ(); bb[5] -= masterPosition.getZ();
        }

        //correct tool form location
        toolFormPosition = toolFormPosition.subtract(masterPosition);

        return new StructureDefinition(
                sbLayout,
                sbLayoutSize,
                masterPosition,
                toolFormPosition,
                states,
                collisionBoxes);
    }

    /**
     * Gets the clean error checked block state
     * @param local local coords of the block within the map
     * @return block state
     */
    private IBlockState getBlockState(MutableBlockPos local)
    {
        final IBlockState block = states[local.x][local.y][local.z];

        if (block == null) return null;
        if (state == null) return block;

        final String strBlockState = state[local.x][local.y][local.z];

        if (strBlockState == null) return block;

        IBlockState finalBlockState = block;

        for (final String singleFullState : strBlockState.split(","))
        {
            if (!singleFullState.contains(":"))
            {
                throw new StructureDefinitionError("Missing property divider @" + local);
            }

            final String propName = singleFullState.split(":")[0];
            final String propVal  = singleFullState.split(":")[1];
            final Collection<IProperty> defaultProp = block.getPropertyNames();

            boolean hasFoundProp = false;

            for (final IProperty prop: defaultProp)
            {
                if (prop.getName().equalsIgnoreCase(propName))
                {
                    boolean hasFoundVal = false;

                    for (final Object val : prop.getAllowedValues())
                    {
                        if (val.toString().equalsIgnoreCase(propVal))
                        {
                            finalBlockState = finalBlockState.withProperty(prop, (Comparable) val);

                            hasFoundVal = true;
                            break;
                        }
                    }

                    if (!hasFoundVal)
                    {
                        throw new StructureDefinitionError(
                                "Property value missing: '" + prop.getName() +
                                        "' value missing: '" + propVal +
                                        "' in '" + prop.getAllowedValues() +
                                        "' on '" + block.getBlock().getUnlocalizedName() +
                                        "' with property: '" + block.getPropertyNames() +
                                        "' @" + local
                        );
                    }

                    hasFoundProp = true;
                    break;
                }
            }

            if (!hasFoundProp)
            {
                throw new StructureDefinitionError(
                        "Missing property: '" + propName +
                        "' value: '" + propVal +
                        "' on block: '" + block.getBlock().getUnlocalizedName() +
                        "' with property: '" + block.getPropertyNames() +
                        "' @" + local
                );
            }
        }

        return finalBlockState;
    }

    private ImmutableMap<Character, IBlockState> repBlock = ImmutableMap.of();

    /**
     * Define what each character represents within the block map
     * @param representation char to unlocalized block name map
     * @exception NullPointerException thrown if block doesn't exist.
     */
    public void assignBlockDefinitions(ImmutableMap<Character, String> representation)
    {
        Builder<Character, IBlockState> builder = ImmutableMap.builder();

        for (final Character c: representation.keySet())
        {
            final String blockName = representation.get(c);
            final Block block = Block.getBlockFromName(blockName);

            checkNotNull(block, "assignBlockDefinitions.Block does not exist " + blockName);

            builder.put(c, block.getDefaultState());
        }

        //default
        builder.put(' ', Blocks.air.getDefaultState());

        repBlock = builder.build();
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

        states = new IBlockState[xsz][ysz][zsz];

        for (final MutableBlockPos local : (Iterable<MutableBlockPos>) BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            final char c = layer[local.y][local.z].charAt(local.x);

            if (!repBlock.containsKey(c) && c != '-')
            {
                throw new StructureDefinitionError("Map missing '" + c + "' @" + local);
            }

            states[local.x][local.y][local.z] = c == '-' ? null : repBlock.get(c);
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
    public void setConfiguration(BlockPos shift, String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        sbLayoutSize = BlockPosUtil.of(xsz, ysz, zsz);
        sbLayout = new BitSet(xsz * ysz *zsz);

        for (final MutableBlockPos local : (Iterable<MutableBlockPos>) BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            final char c = Character.toUpperCase(layer[local.y][local.z].charAt(local.x));

            switch (c)
            {
                case 'M': // Master block location
                    if (masterPosition == null)
                    {
                        masterPosition = BlockPosUtil.of(
                                local.x + shift.getX(),
                                local.y + shift.getY(),
                                local.z + shift.getZ()
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

        if (masterPosition == null)
        {
            throw new StructureDefinitionError("setConfiguration.Master position not defined");
        }
    }

    private ImmutableMap<Character, String> repState = ImmutableMap.of();
    private String[][][] state;

    /**
     * Define what each character represents within the state map
     * @param representation char to "equivelent json" state map
     * @exception NullPointerException thrown if block doesn't exist.
     */
    public void assignStateDefinitions(ImmutableMap<Character, String> representation)
    {
        repState = representation;
    }

    /**
     * builds the state array using the representation map and the layout(String[]...)
     * String = x-line
     * String[] = z-line
     * String[]... = y-line
     * @param layer the layout of the states.
     * @exception NullPointerException the layout is missing a map
     */
    public void assignConstructionStates(String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        state = new String[xsz][ysz][zsz];

        for (final MutableBlockPos local : (Iterable<MutableBlockPos>) BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            final char c = layer[local.y][local.z].charAt(local.x);

            if (!repState.containsKey(c) && c != ' ')
            {
                throw new StructureDefinitionError("Map missing '" + c + "' @" + local);
            }

            state[local.x][local.y][local.z] = repState.get(c);
        }
    }

    public void assignToolFormPosition(BlockPos toolFormPosition)
    {
        this.toolFormPosition = toolFormPosition;
    }

    /**
     * set collision boxes of structure
     * @param collisionBoxes arrays of collision. must have a length of 6 l=lower left back u=upper right front [lx, ly, lz, ux, uy, uz]
     */
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
