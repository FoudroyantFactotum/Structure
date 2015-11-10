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
import mod.steamnsteel.structure.registry.IStructurePatternStateCorrecter;
import mod.steamnsteel.structure.registry.MetaCorrecter.DefaultMinecraftRotation;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.GameRegistry;
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
    private static final ImmutableMap<Block,IStructurePatternStateCorrecter> META_CORRECTOR;

    static {
        final Builder<Block, IStructurePatternStateCorrecter> builder = ImmutableMap.builder();
        final IStructurePatternStateCorrecter defaultMinecraftRotation = new DefaultMinecraftRotation();

        for (ItemStack itemSk: OreDictionary.getOres("stairWood"))//all oreDic stairWood
            builder.put(Block.getBlockFromItem(itemSk.getItem()), defaultMinecraftRotation);

        registerMetaCorrector(builder, "minecraft:stone_stairs"          , defaultMinecraftRotation);
        registerMetaCorrector(builder, "minecraft:brick_stairs"          , defaultMinecraftRotation);
        registerMetaCorrector(builder, "minecraft:stone_brick_stairs"    , defaultMinecraftRotation);
        registerMetaCorrector(builder, "minecraft:nether_brick_stairs"   , defaultMinecraftRotation);
        registerMetaCorrector(builder, "minecraft:sandstone_stairs"      , defaultMinecraftRotation);
        registerMetaCorrector(builder, "minecraft:quartz_stairs"         , defaultMinecraftRotation);

        META_CORRECTOR = builder.build();
    }

    /**
     * Used to validate state correctors.
     * @param builder   ImmutableMap builder
     * @param blockName block to register
     * @param metaCorrecter correcter class
     */
    private static void registerMetaCorrector(Builder<Block, IStructurePatternStateCorrecter> builder, String blockName, IStructurePatternStateCorrecter metaCorrecter)
    {
        final int blockDividePoint = blockName.indexOf(':');

        Block block = GameRegistry.findBlock(
                blockName.substring(0, blockDividePoint),
                blockName.substring(blockDividePoint + 1, blockName.length())
        );

        checkNotNull(block,         blockName + " : Is missing from game Registry");
        checkNotNull(metaCorrecter, blockName + " : metaCorrecter class is null");

        builder.put(block, metaCorrecter);
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
    public static TripleCoord localToGlobal(int lx, int ly, int lz,
                                            int gx, int gy, int gz,
                                            EnumFacing orientation, boolean ismirrored,
                                            TripleCoord strucSize)
    {
        final int rotIndex = orientation.ordinal()-2;

        if (ismirrored)
        {
            lz *= -1;
            if (strucSize.z % 2 == 0) ++lz;
        }

        final int rx = rotationMatrix[rotIndex][0][0] * lx + rotationMatrix[rotIndex][0][1] * lz;
        final int rz = rotationMatrix[rotIndex][1][0] * lx + rotationMatrix[rotIndex][1][1] * lz;

        return TripleCoord.of(
                gx + rx,
                gy + ly,
                gz + rz
        );
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

        switch (orientation)
        {
            case SOUTH:
                direction = direction.rotateY().rotateY();
                break;
            case WEST:
                direction = direction.rotateY();
                break;
            case EAST:
                direction = direction.rotateYCCW();
                break;
            default://North
        }

        return direction;
    }

    //meta corrector
    public static IBlockState localToGlobal(IBlockState state, Block block, EnumFacing orientation, boolean ismirrored)
    {
        if (META_CORRECTOR.containsKey(block))
        {
            return META_CORRECTOR.get(block).alterBlockState(state, orientation, ismirrored);
        }

        return state;
    }

    //collision boxes
    public static void localToGlobalCollisionBoxes(
            int x, int y, int z,
            AxisAlignedBB aabb, List<AxisAlignedBB> boundingBoxList, float[][] collB,
            EnumFacing orientation, boolean isMirrored, TripleCoord size)
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
            TripleCoord local,
            StructureDefinition sd, EnumFacing orientation, boolean ismirrored)
    {
        final int l_lbx = local.x - sd.getMasterLocation().x;
        final int l_lby = local.y - sd.getMasterLocation().y;
        final int l_lbz = local.z - sd.getMasterLocation().z;

        final int l_ubx = local.x + sd.getBlockBounds().x;
        final int l_uby = local.y + sd.getBlockBounds().y;
        final int l_ubz = local.z + sd.getBlockBounds().z;

        final TripleCoord lb
                = localToGlobal(l_lbx, l_lby, l_lbz, pos.getX(), pos.getY(), pos.getZ(), orientation, ismirrored, sd.getBlockBounds());

        final TripleCoord ub
                = localToGlobal(l_ubx, l_uby, l_ubz, pos.getX(), pos.getY(), pos.getZ(), orientation, ismirrored, sd.getBlockBounds());

        final int[][] matrix = rotationMatrix[orientation.ordinal()-2];

        //todo fix fish-e if statement
        final int ntx = orientation == EnumFacing.SOUTH || orientation == EnumFacing.WEST? -1:0;
        final int ntz = orientation == EnumFacing.SOUTH || orientation == EnumFacing.EAST? -1:0;
        final int tx = matrix[0][0] * ntx + matrix[0][1] * ntz;
        final int tz = matrix[1][0] * ntx + matrix[1][1] * ntz;

        return AxisAlignedBB.fromBounds(
                lb.x + tx, lb.y, lb.z + tz,
                ub.x + tx, ub.y, ub.z + tz
        );
    }

    public static TripleCoord transformFromDefinitionToMaster(StructureDefinition sd, TripleCoord loc)
    {
        final TripleCoord newLoc = TripleCoord.of(loc);
        final TripleCoord ml = sd.getMasterLocation();

        newLoc.x -= ml.x;
        newLoc.y -= ml.y;
        newLoc.z -= ml.z;

        return newLoc;
    }
}
