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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.*;

public class JSONStructurePattern implements JsonSerializer<StructurePattern>, JsonDeserializer<StructurePattern>
{
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(StructurePattern.class, new JSONStructurePattern()).create();

    private static final String SIZE = "size";
    private static final String PATTERN_META = "patternMeta";
    private static final String COLLISION_BOXES = "collisionBoxes";

    private static final String BLOCKS_DEFINITION = "blocksDefinition";
    private static final String BLOCKS_CHAR = "char";
    private static final String BLOCKS_NAME = "block";
    private static final String BLOCKS_PATTERN = "pattern";

    private static final String BLOCK_SIDE_INPUT_DEFINITION = "blockSideInputDefinition";
    private static final String BLOCK_SIDE_INPUT_CHAR = "char";
    private static final String BLOCK_SIDE_INPUT_SIDES = "sides";
    private static final String BLOCK_SIDE_INPUT_ACCESSIBLE_SLOTS = "accessibleSlots";
    private static final String BLOCK_SIDE_INPUT_CAN_ITEM = "can-Item";
    private static final String BLOCK_SIDE_INPUT = "blockSideInput";

    @Override
    public JsonElement serialize(StructurePattern src, Type typeOfSrc, JsonSerializationContext context)
    {
    //todo redo
        //TODO metadata serializer
        //TODO BLOCK_SIDE_INPUT
        /*JsonObject result = new JsonObject();

        //Master Block Location
        final JsonArray size = new JsonArray();
        size.add(new JsonPrimitive(src.size.xCoord));
        size.add(new JsonPrimitive(src.size.yCoord));
        size.add(new JsonPrimitive(src.size.zCoord));
        result.add(SIZE, size);

        //Block Map
        JsonArray blocks = null;
        if (src.blocksDefinition != null)
        {
            blocks = new JsonArray();
            for (Entry<Character, Block> pair:src.blocksDefinition.entrySet())
            {
                final UniqueIdentifier blockName = GameRegistry.findUniqueIdentifierFor(pair.getValue());
                checkNotNull(blockName, pair.toString() + " : Block does not exist");

                JsonObject blockData = new JsonObject();
                blockData.add(BLOCKS_CHAR, new JsonPrimitive(pair.getKey()));
                blockData.add(BLOCKS_NAME, new JsonPrimitive(blockName.toString()));
                blocks.add(blockData);
            }
        }
        result.add(BLOCKS_DEFINITION, blocks);

        //Block Recipe
        JsonArray pattern = null;
        if (src.pattern != null)
        {
            pattern = new JsonArray();
            for (String s:src.pattern) pattern.add(new JsonPrimitive(s));
        }
        result.add(PATTERN, pattern);

        //Collision
        if (src.collisionBoxes != null)
        {
            JsonArray jsonL = new JsonArray();
            for (float[] collL:src.collisionBoxes)
            {
                    final JsonArray jsonP = new JsonArray();
                    for (float collP: collL)jsonP.add(new JsonPrimitive(collP));
                    jsonL.add(jsonP);
            }

            result.add(COLLISION_BOXES, jsonL);
        }


        return result;*/
        return null;
    }

    @Override
    public StructurePattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {//TODO do validation on json file
        final JsonObject obj = json.getAsJsonObject();

        final ImmutableTriple<Integer,Integer,Integer> size = deserializeMasterBlock(obj);

        return new StructurePattern(
                size,
                deserializeBlocks(obj),
                deserializeMetadata(obj),
                deserializeSideInput(obj),
                deserializeCollision(obj,
                        Vec3.createVectorHelper(
                                size.getLeft()/ 2.0f,
                                size.getMiddle()/ 2.0f,
                                size.getRight()/ 2.0f))
        );
    }

    private static ImmutableTriple<Integer,Integer,Integer> deserializeMasterBlock(JsonObject obj)
    {
        //Master Block Location
        final JsonArray size = obj.get(SIZE).getAsJsonArray();

        return ImmutableTriple.of(
                size.get(0).getAsInt(),
                size.get(1).getAsInt(),
                size.get(2).getAsInt()
        );
    }

    private static ImmutableList<ImmutableList<Block>> deserializeBlocks(JsonObject obj)
    {
        final JsonElement jsonBlocksDefinition = obj.get(BLOCKS_DEFINITION);
        final JsonElement jsonBlocksPattern = obj.get(BLOCKS_PATTERN);

        if (jsonBlocksDefinition != null && jsonBlocksPattern != null)
        {
            final ImmutableMap<Character,Block> blocksDefinition =
                    deserializeBlocksDefinition(jsonBlocksDefinition.getAsJsonArray());

            final JsonArray pattern = jsonBlocksPattern.getAsJsonArray();
            final Builder<ImmutableList<Block>> listBuilder = ImmutableList.builder();

            for (final JsonElement jsonPatternY: pattern)
            {
                for (final JsonElement jsonPatternZ: jsonPatternY.getAsJsonArray())
                {
                    final Builder<Block> blockBuilder = ImmutableList.builder();
                    final String charBlockLine = jsonPatternZ.getAsString();

                    for (int x=0; x<charBlockLine.length(); ++x)
                    {
                        final char c = charBlockLine.charAt(x);
                        final Block block = blocksDefinition.get(c);

                        checkNotNull(block, "block char not defined -" + c + '-');

                        blockBuilder.add(block);
                    }

                    listBuilder.add(blockBuilder.build());
                }
            }

            return listBuilder.build();
        }

        return null;
    }

    private static ImmutableMap<Character,Block> deserializeBlocksDefinition(JsonArray blocksDefinition)
    {
        ImmutableMap.Builder<Character, Block> builder = ImmutableMap.builder();

        //Implicit blocks
        builder.put(' ', Blocks.air);

        //Load block definitions
        for (JsonElement jsonBlock: blocksDefinition)
        {
            final JsonObject blockData = jsonBlock.getAsJsonObject();

            final char blockChar = blockData.get(BLOCKS_CHAR).getAsCharacter();
            final String blockName = blockData.get(BLOCKS_NAME).getAsString();
            final int blockDividePoint = blockName.indexOf(':');

            Block block = GameRegistry.findBlock(
                    blockName.substring(0, blockDividePoint),
                    blockName.substring(blockDividePoint + 1,blockName.length()));

            checkNotNull(block, "Block does not exist " + blockName);

            builder.put(blockChar, block);
        }

        return builder.build();
    }

    private static ImmutableList<ImmutableList<Byte>> deserializeMetadata(JsonObject obj)
    {
        final JsonElement jsonPatternMetaList = obj.get(PATTERN_META);

        if (jsonPatternMetaList != null)
        {
            final JsonArray patternMeta = jsonPatternMetaList.getAsJsonArray();
            final Builder builder = ImmutableList.builder();

            for(JsonElement jsonMetaY:patternMeta)
            {
                for(JsonElement jsonMetaZ:jsonMetaY.getAsJsonArray())
                {
                    final Builder builderInner = ImmutableList.builder();
                    final String line = jsonMetaZ.getAsString();

                    for (char c : line.toCharArray())
                        builderInner.add(Byte.parseByte(String.valueOf(c), 16));

                    builder.add(builderInner.build());
                }
            }

            return builder.build();
        }

        return null;
    }

    private static ImmutableListMultimap<Integer, StructureBlockSideAccess> deserializeSideInput(JsonObject obj)
    {
        final JsonElement jsonBlockSideInput = obj.get(BLOCK_SIDE_INPUT);
        final JsonElement jsonBlockSideInputDefinition = obj.get(BLOCK_SIDE_INPUT_DEFINITION);

        if (jsonBlockSideInput != null && jsonBlockSideInputDefinition != null)
        {
            final ImmutableMap<Character, StructureBlockSideAccess> sideInputDefinition =
                    deserializeSideInputDefinition(jsonBlockSideInputDefinition);

            final ImmutableListMultimap.Builder<Integer, StructureBlockSideAccess> builder = ImmutableListMultimap.builder();
            final JsonArray jsonBlockSideArrayY = jsonBlockSideInput.getAsJsonArray();

            for (int locY=0; locY<jsonBlockSideArrayY.size(); ++locY)
            {
                final JsonArray jsonBlockSideArrayZ = jsonBlockSideArrayY.get(locY).getAsJsonArray();

                for (int locZ = 0; locZ < jsonBlockSideArrayZ.size(); ++locZ)
                {
                    final String elementLineX = jsonBlockSideArrayZ.get(locZ).getAsString();

                    for (int locX = 0; locX < elementLineX.length(); ++locX)
                    {
                        final char c = elementLineX.charAt(locX);

                        if (c != ' ')
                        {
                            StructureBlockSideAccess sideAccess = sideInputDefinition.get(c);

                            checkNotNull(sideAccess, "sideAccess char not defined -" + c + '-');

                            builder.put(StructurePattern.getPosHash(locX, locY, locZ), sideAccess);
                        }
                    }
                }
            }

            return builder.build();
        }

        return null;
    }

    private static ImmutableMap<Character, StructureBlockSideAccess> deserializeSideInputDefinition(JsonElement jsonBlockSideInputDefinition)
    {
        final JsonArray blockSideInputDefinition = jsonBlockSideInputDefinition.getAsJsonArray();
        final ImmutableMap.Builder<Character, StructureBlockSideAccess> builderSideInputDef
                = ImmutableMap.builder();

        for(JsonElement jsonElementSideDef:blockSideInputDefinition)
        {
            final JsonObject jsonSideDef = jsonElementSideDef.getAsJsonObject();

            //IdentityChar
            final char sideChar = jsonSideDef.getAsJsonPrimitive(BLOCK_SIDE_INPUT_CHAR).getAsCharacter();

            //InputSides
            final JsonElement jsonInputSides = jsonSideDef.get(BLOCK_SIDE_INPUT_SIDES);
            final String inputSides = jsonSideDef.get(BLOCK_SIDE_INPUT_SIDES).getAsString();
            byte sideFlags = 0;

            for (char c: inputSides.toUpperCase().toCharArray()) sideFlags |= (byte) getFlagFromChar(c);

            //AccessibleSlots
            final JsonArray jsonAccessibleSlots = jsonSideDef.getAsJsonArray(BLOCK_SIDE_INPUT_ACCESSIBLE_SLOTS);
            final int[] accessibleSlots = new int[jsonAccessibleSlots.size()];

            for (int i=0; i< jsonAccessibleSlots.size(); ++i)
                accessibleSlots[i] = jsonAccessibleSlots.get(i).getAsInt();

            //Can Input/Extract Item
            final String stringCanItem = jsonSideDef.getAsJsonPrimitive(BLOCK_SIDE_INPUT_CAN_ITEM).getAsString();
            final boolean canInsertItem = stringCanItem.toUpperCase().indexOf('I') != -1;
            final boolean canExtractItem = stringCanItem.toUpperCase().indexOf('E') != -1;

            builderSideInputDef.put(
                    sideChar,
                    new StructureBlockSideAccess(
                            sideFlags,
                            accessibleSlots,
                            canInsertItem,
                            canExtractItem));
        }

        return builderSideInputDef.build();
    }

    private static float[][] deserializeCollision(JsonObject obj, Vec3 hlfSz)
    {//todo replace outside array with immutable collection. also inside one?
        final JsonElement jsonCollisionObj = obj.get(COLLISION_BOXES);

        if (jsonCollisionObj != null)
        {
            final JsonArray jsonCollisionArray = jsonCollisionObj.getAsJsonArray();
            final float[][] collArray = new float[jsonCollisionArray.size()][];

            for (int i = 0; i < collArray.length; ++i)
            {
                final JsonArray jsonCollision = jsonCollisionArray.get(i).getAsJsonArray();
                collArray[i] = new float[]{
                        (float) (jsonCollision.get(0).getAsFloat() - hlfSz.xCoord),
                        jsonCollision.get(1).getAsFloat(),
                        (float) (jsonCollision.get(2).getAsFloat() - hlfSz.zCoord),

                        (float) (jsonCollision.get(3).getAsFloat() - hlfSz.xCoord),
                        jsonCollision.get(4).getAsFloat(),
                        (float) (jsonCollision.get(5).getAsFloat() - hlfSz.zCoord)};
            }
            return collArray;
        } else {
            return new float[][]{{
                    (float)-hlfSz.xCoord,
                    0,
                    (float)-hlfSz.zCoord,

                    (float)hlfSz.xCoord,
                    (float)hlfSz.yCoord,
                    (float)hlfSz.zCoord}};
        }
    }

    private static int getFlagFromChar(char c)
    {
        switch (c)
        {
            case 'U':
                return ForgeDirection.UP.flag;
            case 'D':
                return ForgeDirection.DOWN.flag;
            case 'N':
                return ForgeDirection.NORTH.flag;
            case 'S':
                return ForgeDirection.SOUTH.flag;
            case 'E':
                return ForgeDirection.EAST.flag;
            case 'W':
                return ForgeDirection.WEST.flag;
            default:
                return ForgeDirection.UNKNOWN.flag;
        }
    }
}
