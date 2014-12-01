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
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.block.Block;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.*;

public class JSONStructurePattern implements JsonSerializer<StructurePattern>, JsonDeserializer<StructurePattern>
{
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(StructurePattern.class, new JSONStructurePattern()).create();

    private static final String SIZE = "size";
    private static final String PATTERN = "pattern";
    private static final String COLLISION_BOXES = "collisionBoxes";

    private static final String BLOCK_CHAR = "char";
    private static final String BLOCK_NAME = "block";
    private static final String BLOCK_MAP = "blocks";

    @Override
    public JsonElement serialize(StructurePattern src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject result = new JsonObject();

        //Master Block Location
        final JsonArray size = new JsonArray();
        size.add(new JsonPrimitive(src.size.xCoord));
        size.add(new JsonPrimitive(src.size.yCoord));
        size.add(new JsonPrimitive(src.size.zCoord));
        result.add(SIZE, size);

        //Block Map
        JsonArray blocks = null;
        if (src.blocks != null)
        {
            blocks = new JsonArray();
            for (Entry<Character, Block> pair:src.blocks.entrySet())
            {
                final UniqueIdentifier blockName = GameRegistry.findUniqueIdentifierFor(pair.getValue());
                checkNotNull(blockName, pair.toString() + " : Block does not exist");

                JsonObject blockData = new JsonObject();
                blockData.add(BLOCK_CHAR, new JsonPrimitive(pair.getKey()));
                blockData.add(BLOCK_NAME, new JsonPrimitive(blockName.toString()));
                blocks.add(blockData);
            }
        }
        result.add(BLOCK_MAP, blocks);

        //Block Recipe
        JsonArray pattern = null;
        if (src.pattern != null)
        {
            pattern = new JsonArray();
            for (String s:src.pattern) pattern.add(new JsonPrimitive(s));
        }
        result.add(PATTERN, pattern);

        //Collision
        JsonArray collE = null;
        if (src.collisionBoxes != null)
        {
            collE = new JsonArray();
            for (float[][] bc:src.collisionBoxes)
            {
                final JsonArray collS = new JsonArray();
                for (float[] c: bc)
                {
                    final JsonArray collB = new JsonArray();
                    for (float cp: c)collB.add(new JsonPrimitive(cp));
                    collS.add(collB);
                }
                collE.add(collS);
            }
        }
        result.add(COLLISION_BOXES, collE);

        return result;
    }

    @Override
    public StructurePattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {//TODO do validation on json file
        final JsonObject obj = json.getAsJsonObject();
        StructurePattern sp = new StructurePattern(1,1,1);

        //Master Block Location
        final JsonArray size = obj.get(SIZE).getAsJsonArray();
        sp.size.xCoord = size.get(0).getAsInt();
        sp.size.yCoord = size.get(1).getAsInt();
        sp.size.zCoord = size.get(2).getAsInt();

        //Block Map
        final JsonElement jsonBlockMap = obj.get(BLOCK_MAP);
        if (jsonBlockMap != null)
        {
            final JsonArray blockMap = obj.get(BLOCK_MAP).getAsJsonArray();
            ImmutableMap.Builder<Character, Block> builder = ImmutableMap.builder();

            for (JsonElement jsonBlock: blockMap)
            {
                final JsonObject blockData = jsonBlock.getAsJsonObject();

                final char blockChar = blockData.get(BLOCK_CHAR).getAsCharacter();
                final String blockName = blockData.get(BLOCK_NAME).getAsString();
                final int blockDividePoint = blockName.indexOf(':');

                final Block block = GameRegistry.findBlock(
                        blockName.substring(0, blockDividePoint),
                        blockName.substring(blockDividePoint + 1,blockName.length()));

                checkNotNull(block, blockName + " : Block does not exist");

                builder.put(blockChar, block);
            }

            sp.blocks = builder.build();
        }

        //Block Recipe
        final JsonElement jsonPatternList = obj.get(BLOCK_MAP);
        if (jsonPatternList != null)
        {
            final JsonArray pattern = obj.get(PATTERN).getAsJsonArray();
            final Builder builder = ImmutableList.builder();

            for(JsonElement jsonPattern: pattern) builder.add(jsonPattern.getAsString());

            sp.pattern = builder.build();
        }

        //Collision
        final JsonElement jsonCollisionList = obj.get(COLLISION_BOXES);
        if (jsonPatternList != null)
        {
            final JsonArray collS = jsonCollisionList.getAsJsonArray();
            final float[][][] collSList = new float[collS.size()][][];
            for (int i=0; i< collS.size(); ++i)
            {
                final JsonArray collB = collS.get(i).getAsJsonArray();
                final float[][] collBList = new float[collB.size()][];
                for (int ii=0; ii< collB.size(); ++ii)
                {
                    final JsonArray collC = collB.get(ii).getAsJsonArray();
                    final float[] coll = {
                            collC.get(0).getAsFloat(),
                            collC.get(1).getAsFloat(),
                            collC.get(2).getAsFloat(),
                            collC.get(3).getAsFloat(),
                            collC.get(4).getAsFloat(),
                            collC.get(5).getAsFloat()
                    };
                    collBList[ii] = coll;
                }
                collSList[i] = collBList;
            }
            sp.collisionBoxes = collSList;
        }
        sp.buildBlockCollisions();

        return sp;
    }
}
