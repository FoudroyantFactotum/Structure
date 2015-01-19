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
package mod.steamnsteel.utility.structure;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import cpw.mods.fml.common.registry.GameRegistry;
import mod.steamnsteel.TheMod;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.MetaCorrecter.SMCStoneStairs;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class StructurePattern
{
    private static String STRUCTURE_LOCATION = "structure/";
    private static String STRUCTURE_FILE_EXTENSION = ".structure.json";

    public static final StructurePattern MISSING_STRUCTURE = new StructurePattern();

    /*TODO Immutable?*/private static final Map<Block,IStructurePatternMetaCorrecter> META_CORRECTOR = new HashMap<Block, IStructurePatternMetaCorrecter>(11);
    private static final ImmutableMap<Integer, StructurePattern> PATTERNS;

    static {
        final IStructurePatternMetaCorrecter stairs = new SMCStoneStairs();

        registerMetaCorrector("minecraft:stone_stairs",stairs);
        registerMetaCorrector("minecraft:oak_stairs",stairs);
        registerMetaCorrector("minecraft:brick_stairs",stairs);
        registerMetaCorrector("minecraft:stone_brick_stairs",stairs);
        registerMetaCorrector("minecraft:nether_brick_stairs",stairs);
        registerMetaCorrector("minecraft:sandstone_stairs",stairs);
        registerMetaCorrector("minecraft:birch_stairs",stairs);
        registerMetaCorrector("minecraft:jungle_stairs",stairs);
        registerMetaCorrector("minecraft:quartz_stairs",stairs);
        registerMetaCorrector("minecraft:acacia_stairs",stairs);
        registerMetaCorrector("minecraft:dark_oak_stairs",stairs);


        Builder builder = ImmutableMap.builder();

        registerPatterns(builder, ModBlock.ballMill);
        registerPatterns(builder, ModBlock.blastFurnace);
        registerPatterns(builder, ModBlock.boiler);

        PATTERNS = builder.build();
    }

    private static void registerMetaCorrector(String blockName, IStructurePatternMetaCorrecter metaCorrecter)
    {
        final int blockDividePoint = blockName.indexOf(':');

        Block block = GameRegistry.findBlock(
                blockName.substring(0, blockDividePoint),
                blockName.substring(blockDividePoint + 1,blockName.length()));

        checkNotNull(block, blockName + " : Is missing from game Registry");
        checkNotNull(metaCorrecter, blockName + " : metaCorrecter class is null");

        META_CORRECTOR.put(block, metaCorrecter);
    }

    private static void registerPatterns(Builder b,Block block)
    {
        final ResourceLocation jsonStructure = getResourceLocation(getStructurePath(getBlockName(block.getUnlocalizedName())));
        StructurePattern blockPattern = null;
        try
        {
            final IResource res = Minecraft.getMinecraft().getResourceManager().getResource(jsonStructure);
            final InputStreamReader inpStream = new InputStreamReader(res.getInputStream());
            final BufferedReader buffRead = new BufferedReader(inpStream);

            blockPattern = JSONStructurePattern.gson.fromJson(buffRead, StructurePattern.class);

            buffRead.close();
            inpStream.close();
        } catch (IOException e)
        {
            Logger.info("file does not exist : " + e.getMessage());
        }

        b.put(block.getUnlocalizedName().hashCode(), blockPattern == null?MISSING_STRUCTURE:blockPattern);
    }

    public static StructurePattern getPattern(int hash)
    {
        final StructurePattern pattern = PATTERNS.get(hash);
        return pattern == null ? MISSING_STRUCTURE : pattern;
    }

    private static String getBlockName(String unlocName)
    {
        return unlocName.substring(unlocName.indexOf(':')+1);
    }

    private static ResourceLocation getResourceLocation(String path)
    {
        return new ResourceLocation(TheMod.MOD_ID.toLowerCase(), path);
    }

    private static String getStructurePath(String name)
    {
        return STRUCTURE_LOCATION + name + STRUCTURE_FILE_EXTENSION;
    }


    private final ImmutableTriple<Integer,Integer,Integer> size;
    private final ImmutableList<ImmutableList<Block>> blocks;
    private final ImmutableList<ImmutableList<Byte>> metadata;
    private final float[][] collisionBoxes;
    private final ImmutableListMultimap<Integer, StructureBlockSideAccess> blockSideAccess;

    public StructurePattern()
    {
        this(1,1,1);
    }

    public StructurePattern(int xSize, int ySize, int zSize)
    {
        this(
                ImmutableTriple.of(xSize,ySize,zSize),
                null,
                null,
                null,
                new float[][]{{
                        -xSize / 2,
                        0,
                        -zSize / 2,
                        (float) Math.ceil(xSize / 2),
                        ySize,
                        (float) Math.ceil(zSize / 2)}});
    }

    public StructurePattern(ImmutableTriple<Integer,Integer,Integer> size,
            ImmutableList<ImmutableList<Block>> blocks,
            ImmutableList<ImmutableList<Byte>> metadata,
            ImmutableListMultimap<Integer, StructureBlockSideAccess> blockSideAccess,
            float[][] collisionBoxes)
    {
        this.size = size;
        this.blocks = blocks;
        this.metadata = metadata;
        this.collisionBoxes = collisionBoxes;
        this.blockSideAccess = blockSideAccess;
    }

    public Block getBlock(StructureBlockCoord coord)
    {
        return getBlock(coord.getLX(), coord.getLY(), coord.getLZ()-1);
    }

    public Block getBlock(Vec3 v)
    {
        return getBlock((int)v.xCoord, (int)v.yCoord, (int)v.zCoord);
    }

    public Block getBlock(int x, int y, int z)
    {
        if (blocks != null)
        {
            final int layerCount = getLayerCount(y, z);

            if (checkNotOutOfBounds(blocks, x, layerCount))
                return blocks.get(layerCount).get(x);
        }

        return Blocks.air;
    }

    public int getBlockMetadata(StructureBlockCoord coord)
    {
        return getBlockMetadata(coord.getLX(), coord.getLY(), coord.getLZ(), coord.getOrienetation(), coord.isMirrored());
    }

    public int getBlockMetadata(int x, int y, int z, Orientation o, boolean isMirrored)
    {
        byte meta = 0;

        if (metadata != null)
        {
            final int layerCount = getLayerCount(y, z);

            if (checkNotOutOfBounds(metadata, x, layerCount))
                meta = metadata.get(layerCount).get(x);
        }

        final IStructurePatternMetaCorrecter metaCorrecter = META_CORRECTOR.get(getBlock(x, y, z));

        return metaCorrecter == null ? meta : metaCorrecter.correctMeta(meta, o, isMirrored);
    }

    public StructureBlockSideAccess getSideAccess(int x, int y, int z, ForgeDirection direction, Orientation orientation)
    {//todo transform direction with orientation

        if (blockSideAccess != null)
        {
            final int hash = getPosHash(x, y, z);
            final ImmutableList<StructureBlockSideAccess> sideAccessList = blockSideAccess.get(hash);

            for (StructureBlockSideAccess sideAccess: sideAccessList)
                if (sideAccess.hasSide(direction))
                    return sideAccess;
        }

        return StructureBlockSideAccess.MISSING_SIDE_ACCESS;
    }

    public static int getPosHash(int x, int y, int z)
    {
        return (byte) x << 16 + (byte) y << 8 + (byte) z;
    }

    private int getLayerCount(int y, int z)
    {
        return z + y*size.right;
    }

    private static <E> boolean checkNotOutOfBounds(ImmutableList<ImmutableList<E>> list, int x, int layerCount)
    {
        return layerCount < list.size() && layerCount > -1 && x < list.get(layerCount).size();
    }

    public Vec3 getSize()
    {
        return Vec3.createVectorHelper(
                size.getLeft(),
                size.getMiddle(),
                size.getRight());
    }

    public int getSizeX()
    {
        return size.getLeft();
    }

    public int getSizeY()
    {
        return size.getMiddle();
    }

    public int getSizeZ()
    {
        return size.getRight();
    }

    public Vec3 getHalfSize()
    {
        final Vec3 size = getSize();

        size.xCoord /= 2;
        size.yCoord /= 2;
        size.zCoord /= 2;

        return size;
    }

    public float[][] getCollisionBoxes()
    {
        return collisionBoxes;
    }

    public String toString(){
        return Objects.toStringHelper(this)
                .add("size", size)
                .add("blocks", blocks)
                .add("metadata", metadata)
                .add("collisionBoxes", collisionBoxes)
                .add("blockSideAccess", blockSideAccess)
                .toString();
    }

    //=========================
    //DIRECT BLOCK RELATED CODE
    //=========================
    //todo dbrc
    //lol none

}
