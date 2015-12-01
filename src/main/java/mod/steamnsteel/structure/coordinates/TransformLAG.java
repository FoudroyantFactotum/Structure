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
package mod.steamnsteel.structure.coordinates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import mod.steamnsteel.structure.registry.MetaCorrecter.DefaultMinecraftRotation;
import mod.steamnsteel.structure.registry.MetaCorrecter.IStructurePatternStateCorrecter;
import mod.steamnsteel.structure.registry.StateMatcher.IStateMatcher;
import mod.steamnsteel.structure.registry.StateMatcher.StairMatcher;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * This class is used as a utility class holding onto the function implementations that involve a basic transform.
 */
public final class TransformLAG
{
    public static void initStatic()
    {
        //noop
    }

    private static final ImmutableMap<Block,IStructurePatternStateCorrecter> META_CORRECTOR;
    private static final ImmutableMap<Block,IStateMatcher> STATE_MATCHER;

    static
    {
        final String[] minecraftStairs = {
                "minecraft:stone_stairs",
                "minecraft:brick_stairs",
                "minecraft:stone_brick_stairs",
                "minecraft:nether_brick_stairs",
                "minecraft:sandstone_stairs",
                "minecraft:quartz_stairs"
        };

        final Builder<Block, IStructurePatternStateCorrecter> builderS = ImmutableMap.builder();
        final Builder<Block, IStateMatcher> builderM = ImmutableMap.builder();

        final IStructurePatternStateCorrecter defaultRotation = new DefaultMinecraftRotation();
        final IStateMatcher stairMatcher = new StairMatcher();

        for (final ItemStack itemSk: OreDictionary.getOres("stairWood"))//all oreDic stairWood
        {
            builderS.put(Block.getBlockFromItem(itemSk.getItem()), defaultRotation);
            builderM.put(Block.getBlockFromItem(itemSk.getItem()), stairMatcher);
        }

        for (final String s: minecraftStairs)
        {
            registerStateCorrector(builderS, s, defaultRotation);
            registerStateMatcher(builderM,   s, stairMatcher);
        }

        META_CORRECTOR = builderS.build();
        STATE_MATCHER = builderM.build();
    }

    /**
     * Used to validate state correctors.
     * @param builder   ImmutableMap builder
     * @param blockName block to register
     * @param stateCorrecter correcter class
     */
    private static void registerStateCorrector(Builder<Block, IStructurePatternStateCorrecter> builder, String blockName, IStructurePatternStateCorrecter stateCorrecter)
    {
        final Block block = Block.getBlockFromName(blockName);

        checkNotNull(block,          blockName + " : Is missing from game Registry");
        checkNotNull(stateCorrecter, blockName + " : stateCorrecter is null");

        builder.put(block, stateCorrecter);
    }

    private static void registerStateMatcher(Builder<Block, IStateMatcher> builder, String blockName, IStateMatcher stateMatcher)
    {
        final Block block = Block.getBlockFromName(blockName);

        checkNotNull(block,        blockName + " : Is missing from game Registry");
        checkNotNull(stateMatcher, blockName + " : stateMatcher is null");

        builder.put(block, stateMatcher);
    }


    //===============================================================================
    //                              T R A N S F O R M S
    //===============================================================================


    private static final int[][][] rotationMatrix = {
            {{1, 0}, {0, 1}}, // north
            {{-1, 0}, {0, -1}}, //south
            {{0, 1}, {-1, 0}}, //west
            {{0, -1}, {1, 0}}, //east
    };

    //from external with local to master
    public static BlockPos localToGlobal(int lx, int ly, int lz,
                                            int gx, int gy, int gz,
                                            EnumFacing orientation, boolean ismirrored,
                                            BlockPos strucSize)
    {
        final int rotIndex = orientation.ordinal()-2;

        if (ismirrored)
        {
            lz *= -1;
            if (strucSize.getZ() % 2 == 0) ++lz;
        }

        final int rx = rotationMatrix[rotIndex][0][0] * lx + rotationMatrix[rotIndex][0][1] * lz;
        final int rz = rotationMatrix[rotIndex][1][0] * lx + rotationMatrix[rotIndex][1][1] * lz;

        return BlockPosUtil.of(
                gx + rx,
                gy + ly,
                gz + rz
        );
    }

    public static void mutLocalToGlobal(MutableBlockPos local,
                                            BlockPos global,
                                            EnumFacing orientation, boolean ismirrored,
                                            BlockPos strucSize)
    {
        final int rotIndex = orientation.ordinal()-2;

        if (ismirrored)
        {
            local.z *= -1;
            if (strucSize.getZ() % 2 == 0) ++local.z;
        }

        final int rx = rotationMatrix[rotIndex][0][0] * local.x + rotationMatrix[rotIndex][0][1] * local.z;
        final int rz = rotationMatrix[rotIndex][1][0] * local.x + rotationMatrix[rotIndex][1][1] * local.z;

        local.x = global.getX() + rx;
        local.y += global.getY();
        local.z = global.getZ() + rz;
    }

    public static int localToGlobalDirection(int ld, EnumFacing o, boolean mirror)
    {
        int fdNew = 0;

        for (EnumFacing d : EnumFacing.VALUES)
        {
            if (SteamNSteelStructureTE.isSide(ld, o))
            {
                fdNew |= flagEnumFacing(localToGlobal(d, o, mirror));
            }
        }

        return fdNew;
    }

    public static int flagEnumFacing(final EnumFacing f)
    {
        return 1 << f.ordinal();
    }

    //direction - rotate
    public static EnumFacing localToGlobal(EnumFacing direction, EnumFacing orientation, boolean ismirrored)
    {
        //switch from local direction to global
        if (ismirrored && (direction == EnumFacing.NORTH || direction == EnumFacing.SOUTH))
        {
            direction = direction.getOpposite();
        }

        if (direction == EnumFacing.DOWN || direction == EnumFacing.UP)
        {
            return direction;
        }

        switch (orientation)
        {
            case SOUTH:
                direction = direction.rotateY().rotateY();
                break;
            case WEST:
                direction = direction.rotateYCCW();
                break;
            case EAST:
                direction = direction.rotateY();
                break;
            default://North
        }

        return direction;
    }

    //state modification on direction change.
    public static IBlockState localToGlobal(IBlockState state, EnumFacing orientation, boolean ismirrored)
    {
        if (META_CORRECTOR.containsKey(state.getBlock()))
        {
            return META_CORRECTOR.get(state.getBlock()).alterBlockState(state, orientation, ismirrored);
        }

        return state;
    }

    //state matcher. FIXME Prob doesn't belong here.
    public static boolean doBlockStatesMatch(IBlockState b1, IBlockState b2)
    {
        if (STATE_MATCHER.containsKey(b1.getBlock()))
        {
            return STATE_MATCHER.get(b1.getBlock()).matchBlockState(b1, b2);
        }
        else
        {
            //complete check against all properties
            /*
            final ImmutableMap<String, IProperty> b2Props = b2.getProperties();

            for (final IProperty prop : (Collection<IProperty>) b1.getPropertyNames())
            {
                if (!b2Props.containsKey(prop))
                {
                    return false;
                }

                final Comparable b2Prop = (Comparable) b2Props.get(prop);

                if (b1.getValue(prop).compareTo(b2Prop) != 0)
                {
                    return false;
                }
            }*/

            return true;
        }
    }

    //collision boxes
    public static void localToGlobalCollisionBoxes(
            int x, int y, int z,
            AxisAlignedBB aabb, List<AxisAlignedBB> boundingBoxList, float[][] collB,
            EnumFacing orientation, boolean isMirrored, BlockPos size)
    {
        final int[][] matrix = rotationMatrix[orientation.ordinal()-2];

        final int ntx = orientation == EnumFacing.SOUTH || orientation == EnumFacing.WEST? -1:0;
        final int ntz = orientation == EnumFacing.SOUTH || orientation == EnumFacing.EAST? -1:0;
        final int tx = matrix[0][0] * ntx + matrix[0][1] * ntz;
        final int tz = matrix[1][0] * ntx + matrix[1][1] * ntz;

        final MutableAxisAlignedBB bb = MutableAxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);

        for (final float[] f: collB)
        {
            final float c1x = matrix[0][0] * f[0] + matrix[0][1] * f[2];
            final float c1z = matrix[1][0] * f[0] + matrix[1][1] * f[2];

            final float c2x = matrix[0][0] * f[3] + matrix[0][1] * f[5];
            final float c2z = matrix[1][0] * f[3] + matrix[1][1] * f[5];

            bb.minX = x + min(c1x, c2x) + tx;
            bb.minY = y + f[1];
            bb.minZ = z + min(c1z, c2z) + tz;

            bb.maxX = x + max(c1x, c2x) + tx;
            bb.maxY = y + f[4];
            bb.maxZ = z + max(c1z, c2z) + tz;

            if (bb.intersectsWith(aabb))
            {
                boundingBoxList.add(bb.getAxisAlignedBB());
            }
        }
    }

    //Bounding box
    public static AxisAlignedBB localToGlobalBoundingBox(
            BlockPos pos,
            BlockPos local,
            StructureDefinition sd, EnumFacing orientation, boolean ismirrored)
    {
        final int l_lbx = local.getX() - sd.getMasterLocation().getX();
        final int l_lby = local.getY() - sd.getMasterLocation().getY();
        final int l_lbz = local.getZ() - sd.getMasterLocation().getZ();

        final int l_ubx = local.getX() + sd.getBlockBounds().getX() - sd.getMasterLocation().getX();
        final int l_uby = local.getY() + sd.getBlockBounds().getY() - sd.getMasterLocation().getY();
        final int l_ubz = local.getZ() + sd.getBlockBounds().getZ() - sd.getMasterLocation().getZ();

        final BlockPos lb
                = localToGlobal(l_lbx, l_lby, l_lbz, pos.getX(), pos.getY(), pos.getZ(), orientation, ismirrored, sd.getBlockBounds());

        final BlockPos ub
                = localToGlobal(l_ubx, l_uby, l_ubz, pos.getX(), pos.getY(), pos.getZ(), orientation, ismirrored, sd.getBlockBounds());

        final int[][] matrix = rotationMatrix[orientation.ordinal()-2];

        //todo fix fish-e if statement
        final int ntx = orientation == EnumFacing.SOUTH || orientation == EnumFacing.WEST? -1:0;
        final int ntz = orientation == EnumFacing.SOUTH || orientation == EnumFacing.EAST? -1:0;
        final int tx = matrix[0][0] * ntx + matrix[0][1] * ntz;
        final int tz = matrix[1][0] * ntx + matrix[1][1] * ntz;

        return AxisAlignedBB.fromBounds(
                lb.getX() + tx, lb.getY(), lb.getZ() + tz,
                ub.getX() + tx, ub.getY(), ub.getZ() + tz
        );
    }

    public static BlockPos transformFromDefinitionToMaster(StructureDefinition sd, BlockPos loc)
    {
        return loc.subtract(sd.getMasterLocation());
    }
}
