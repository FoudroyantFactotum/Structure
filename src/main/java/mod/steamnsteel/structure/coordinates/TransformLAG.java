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
import cpw.mods.fml.common.registry.GameRegistry;
import mod.steamnsteel.structure.registry.IStructurePatternMetaCorrecter;
import mod.steamnsteel.structure.registry.MetaCorrecter.SMCStoneStairs;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.position.WorldBlockCoord;
import net.minecraft.block.Block;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import static com.google.common.base.Preconditions.checkNotNull;

public class TransformLAG
{
    private static final ImmutableMap<Block,IStructurePatternMetaCorrecter> META_CORRECTOR;

    static {
        final Builder<Block, IStructurePatternMetaCorrecter> builder = ImmutableMap.builder();
        final IStructurePatternMetaCorrecter stairs = new SMCStoneStairs();

        registerMetaCorrector(builder, "minecraft:stone_stairs"          , stairs);
        registerMetaCorrector(builder, "minecraft:oak_stairs"            , stairs);
        registerMetaCorrector(builder, "minecraft:brick_stairs"          , stairs);
        registerMetaCorrector(builder, "minecraft:stone_brick_stairs"    , stairs);
        registerMetaCorrector(builder, "minecraft:nether_brick_stairs"   , stairs);
        registerMetaCorrector(builder, "minecraft:sandstone_stairs"      , stairs);
        registerMetaCorrector(builder, "minecraft:birch_stairs"          , stairs);
        registerMetaCorrector(builder, "minecraft:jungle_stairs"         , stairs);
        registerMetaCorrector(builder, "minecraft:quartz_stairs"         , stairs);
        registerMetaCorrector(builder, "minecraft:acacia_stairs"         , stairs);
        registerMetaCorrector(builder, "minecraft:dark_oak_stairs"       , stairs);

        META_CORRECTOR = builder.build();
    }

    private static void registerMetaCorrector(Builder<Block, IStructurePatternMetaCorrecter> builder, String blockName, IStructurePatternMetaCorrecter metaCorrecter)
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


    private static final int[][][] rotationMatrix = {
            {{-1, 0}, {0, -1}}, //south
            {{0, 1}, {-1, 0}}, //west
            {{1, 0}, {0, 1}}, // north
            {{0, -1}, {1, 0}}, //east
    };

    public static WorldBlockCoord localToGlobal(int lx, int ly, int lz, Vec3 worldLoc, ImmutableTriple<Integer, Integer, Integer> ml, Orientation o, boolean ismirrored, int zSize)
    {
        final ImmutableTriple<Integer, Integer, Integer> loc
                = localToGlobal(lx, ly, lz,
                (int) worldLoc.xCoord - ml.getLeft(), (int) worldLoc.yCoord - ml.getMiddle(), (int) worldLoc.zCoord - ml.getRight(),
                o, ismirrored,
                zSize
        );

        return WorldBlockCoord.of(
                loc.getLeft(),
                loc.getMiddle(),
                loc.getRight()
        );
    }

    public static ImmutableTriple<Integer, Integer, Integer> localToGlobal(int lx, int ly, int lz, int gx, int gy, int gz, Orientation o, boolean ismirrored, int zSize)
    {
        final int rotIndex = o.encode();

        if (ismirrored)
        {
            lz *= -1;
            if (zSize % 2 == 0) ++lz;
        }

        final int rx = rotationMatrix[rotIndex][0][0] * lx + rotationMatrix[rotIndex][0][1] * lz;
        final int rz = rotationMatrix[rotIndex][1][0] * lx + rotationMatrix[rotIndex][1][1] * lz;

        return ImmutableTriple.of(
                gx - rx,
                gy - ly,
                gz - rz
        );
    }

    //direction - rotate
    public static ForgeDirection localToGlobal(ForgeDirection d, Orientation o, boolean ismirrored)
    {
        //switch from local direction to global

        if (ismirrored && (d == ForgeDirection.NORTH || d == ForgeDirection.SOUTH))
            d = d.getOpposite();

        switch (o)
        {
            case SOUTH:
                d = d.getRotation(ForgeDirection.DOWN).getRotation(ForgeDirection.DOWN);
                break;
            case WEST:
                d = d.getRotation(ForgeDirection.UP);
                break;
            case EAST:
                d = d.getRotation(ForgeDirection.DOWN);
                break;
            default://North
        }

        return d;
    }

    public static int localToGlobal(int meta, Block block, Orientation o, boolean ismirrored)
    {
        if (META_CORRECTOR.containsKey(block))
            return META_CORRECTOR.get(block).correctMeta((byte) meta, o, ismirrored);

        return meta;
    }

}
