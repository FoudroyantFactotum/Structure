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
package mod.steamnsteel.structure.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.*;
import mod.steamnsteel.structure.registry.StructureBlockSideAccess;
import mod.steamnsteel.structure.registry.StructureDefinition;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.lang.reflect.Type;
import java.util.BitSet;

public class JSONStructureDefinition implements JsonDeserializer<StructureDefinition>
{
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(StructureDefinition.class, StructureDefinition.getJsonDeserializer())
            .registerTypeAdapter(JsonStructureBlockSideAccess.class, new JsonStructureBlockSideAccess())
            .registerTypeAdapter(JSONBlock.class, new JSONBlock())
            .create();

    private static final String IO = "I/O";
    private static final String SIZE = "size";
    private static final String CONSTRUCTION = "construction";
    private static final String COLLISIONBOXES = "collisionBoxes";

    private static final String ERRORMSG = "Can't deserialize structure definition";

    @Override
    public StructureDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final StructureDefinitionBuilder builder = new StructureDefinitionBuilder();
        final JsonObject jsonObj = json.getAsJsonObject();

        if (jsonObj.has(CONSTRUCTION))
            deserializeConstruction(builder,jsonObj.getAsJsonObject(CONSTRUCTION));

        if (jsonObj.has(SIZE))
            deserializeSize(builder, jsonObj.getAsJsonObject(SIZE));

        if (jsonObj.has(IO))
            deserializeIO(builder, jsonObj.getAsJsonObject(IO));

        if (jsonObj.has(COLLISIONBOXES))
            deserializeCollisionBoxes(builder, jsonObj.getAsJsonObject(COLLISIONBOXES));

        return builder.build();
    }

    //=========================================================
    //  D e s e r i a l i z e   C o l l i s i o n   B o x e s
    //=========================================================

    private static void deserializeCollisionBoxes(StructureDefinitionBuilder sdb, JsonObject json) throws JsonParseException
    {
        if (json.isJsonArray())
        {
            final JsonArray jsonCollisionBoxes = json.getAsJsonArray();
            final float[][] collisionBoxes = new float[jsonCollisionBoxes.size()][];
            //todo shift offset into build.
            final float offsetX = sdb.totalSize.getLeft()/2.0f - sdb.mps.getLeft();
            final float offsetZ = sdb.totalSize.getRight()/2.0f - sdb.mps.getRight();

            for (int i = 0; i < collisionBoxes.length; ++i)
            {
                final JsonArray jsonBox = jsonCollisionBoxes.get(i).getAsJsonArray();

                collisionBoxes[i] = new float[]{
                        jsonBox.get(0).getAsFloat() - offsetX,
                        jsonBox.get(1).getAsFloat(),
                        jsonBox.get(2).getAsFloat() - offsetZ,

                        jsonBox.get(3).getAsFloat() - offsetX,
                        jsonBox.get(4).getAsFloat(),
                        jsonBox.get(5).getAsFloat() - offsetZ};
            }

        } else
            throw new JsonParseException(ERRORMSG + ": Collision Boxes not a list");
    }

    //=========================================================
    //     D e s e r i a l i z e   C o n s t r u c t i o n
    //=========================================================

    private static final String CONSTRUCTION_DEFINITION = "definition";
    private static final String CONSTRUCTION_DEFINITION_CHAR = "char";
    private static final String CONSTRUCTION_DEFINITION_BLOCK = "block";

    private static final String CONSTRUCTION_PATTERN = "pattern";
    private static final String CONSTRUCTION_METADATA = "metadata";
    private static final String CONSTRUCTION_ADJUSTMENT = "adjustment";
    private static final String CONSTRUCTION_CLEANUPONBUILD = "cleanUpOnBuild";

    private static void deserializeConstruction(StructureDefinitionBuilder sdb, JsonObject json) throws JsonParseException
    {
        if (!deserializeConstructionHasAllComponents(json))
            throw new JsonParseException(ERRORMSG + ": Missing Tag under " + CONSTRUCTION);

        final ImmutableMap<Character, Block> blockDefinitions = deserializeConstructionDefinition(json.get(CONSTRUCTION_DEFINITION));


        sdb.blocks = deserializeConstructionPattern(json.get(CONSTRUCTION_PATTERN), blockDefinitions);
        if (json.has(CONSTRUCTION_METADATA))
            sdb.metadata = deserializeConstructionMeta(json.get(CONSTRUCTION_METADATA));

        sdb.adjustmentCtS = deserializeConstructionAdjustment(json.get(CONSTRUCTION_ADJUSTMENT));
        sdb.cleanUpOnBuild = deserializeConstructionCleanUpOnBuild(json.get(CONSTRUCTION_CLEANUPONBUILD));

    }

    private static boolean deserializeConstructionHasAllComponents(JsonObject json)
    {
        return json.has(CONSTRUCTION_DEFINITION) &&
                json.has(CONSTRUCTION_PATTERN) &&
                json.has(CONSTRUCTION_ADJUSTMENT) &&
                json.has(CONSTRUCTION_CLEANUPONBUILD);
    }

    private static Block[][][] deserializeConstructionPattern(JsonElement json, ImmutableMap<Character, Block> blockDefinitions)
    {
        final ItrJsonStructure itr = new ItrJsonStructure(json);
        final Block[][][] blocks = new Block[itr.getOuterSize()][][];

        while (itr.hasNext())
        {
            final JsonReadState<JsonElement> js = itr.next();

            if (blocks[js.getOuter()] == null)
                blocks[js.getOuter()] = new Block[itr.getInnerSize()][];

            final char[] xLine = js.getValue().getAsString().toCharArray();
            final Block[] xla = new Block[xLine.length];

            blocks[js.getOuter()][js.getInner()] = xla;

            for (int x=0; x < xLine.length; ++x)
            {
                final char c = xLine[x];
                final Block b;

                if (c == '.') // Null/IgnoreBlock
                    b = null;
                else
                    if (blockDefinitions.containsKey(c))
                        b = blockDefinitions.get(c);
                    else
                        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_PATTERN + " Missing block definition for " + c);

                xla[x] = b;
            }
        }

        return jagedCheck(blocks, CONSTRUCTION + "-" + CONSTRUCTION_PATTERN);
    }

    private static byte[][][] deserializeConstructionMeta(JsonElement json)
    {
        if (json.isJsonArray())
        {
            final ItrJsonStructure itr = new ItrJsonStructure(json);
            final byte[][][] meta = new byte[itr.getOuterSize()][][];

            while (itr.hasNext())
            {
                final JsonReadState<JsonElement> js = itr.next();

                if (meta[js.getOuter()] == null)
                    meta[js.getOuter()] = new byte[itr.getInnerSize()][];

                final char[] xLine = js.getValue().getAsString().toCharArray();
                final byte[] xla = new byte[xLine.length];

                meta[js.getOuter()][js.getInner()] = xla;

                for (int x=0; x<xLine.length; ++x)
                    xla[x] = Byte.parseByte(String.valueOf(xLine[x]), 16);
            }

            return jagedCheck(meta);
        }

        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_METADATA);
    }

    private static ImmutableMap<Character, Block> deserializeConstructionDefinition(JsonElement json)
    {
        if (json.isJsonArray())
        {
            final JsonArray blockDefintions = json.getAsJsonArray();
            final Builder<Character, Block> builder = ImmutableMap.builder();

            for (JsonElement jsonObjBlock: blockDefintions)
            {
                if (jsonObjBlock.isJsonObject())
                {
                    final JsonObject jsonBlock = jsonObjBlock.getAsJsonObject();

                    if (jsonBlock.has(CONSTRUCTION_DEFINITION_CHAR))
                    {
                        if (jsonBlock.has(CONSTRUCTION_DEFINITION_BLOCK))
                        {
                            builder.put(
                                    jsonBlock.get(CONSTRUCTION_DEFINITION_CHAR).getAsCharacter(),
                                    gson.fromJson(jsonBlock.get(CONSTRUCTION_DEFINITION_BLOCK), Block.class)
                            );
                        } else
                            throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_DEFINITION + " missing \"" + CONSTRUCTION_DEFINITION_BLOCK + "\" tag");
                    } else
                        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_DEFINITION +" missing \""+ CONSTRUCTION_DEFINITION_CHAR + "\" tag");
                } else
                    throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_DEFINITION + " not in correct form");
            }

            //Implicit blocks
            builder.put(' ', Blocks.air);   //Air

            return builder.build();
        }
        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_DEFINITION + " not a list");
    }

    private static boolean deserializeConstructionCleanUpOnBuild(JsonElement json)
    {
        if (json.isJsonPrimitive())
        {
            return json.getAsBoolean();
        }
        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + " " + CONSTRUCTION_CLEANUPONBUILD + " is incorrect type");
    }

    private static ImmutableTriple<Integer, Integer, Integer> deserializeConstructionAdjustment(JsonElement json)
    {
        if (json.isJsonArray())
        {
            final JsonArray ja = json.getAsJsonArray();

            if (ja.size() == 3)
                return ImmutableTriple.of(
                        ja.get(0).getAsInt(),
                        ja.get(1).getAsInt(),
                        ja.get(2).getAsInt()
                );

            throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + " " + CONSTRUCTION_ADJUSTMENT + " is wrong length (not len of 3)");
        }
        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + " " + CONSTRUCTION_ADJUSTMENT + " is incorrect type");
    }


    //=========================================================
    //              D e s e r i a l i z e   S i z e
    //=========================================================

    private static final String SIZE_CONFIGURATION = "configuration";

    private static void deserializeSize(StructureDefinitionBuilder sdb, JsonObject json)
    {
        if (json.has(SIZE_CONFIGURATION))
        {
            final JsonArray jsonConfiguration = json.getAsJsonArray(SIZE_CONFIGURATION);
            final ItrJsonStructure itjs = new ItrJsonStructure(jsonConfiguration);
            final BitSet[][] blockLocation = new BitSet[itjs.getOuterSize()][];

            while (itjs.hasNext())
            {
                final JsonReadState<JsonElement> jrs = itjs.next();

                final char[] xLine = jrs.getValue().getAsString().toUpperCase().toCharArray();
                final BitSet bs = new BitSet(xLine.length);

                if (blockLocation[jrs.getOuter()] == null)
                    blockLocation[jrs.getOuter()] = new BitSet[itjs.getInnerSize()];

                blockLocation[jrs.getOuter()][jrs.getInner()] = bs;

                for (int x=0; x<xLine.length; ++x)
                    sizeConfigurationIOWrite(xLine[x], bs, x, jrs, sdb);
            }

            sdb.sbLayout = jagedCheck(blockLocation);
        }
    }

    private static void sizeConfigurationIOWrite(char c, BitSet bs, int x, JsonReadState jrs, StructureDefinitionBuilder sdb)
    {
        //flip bit to represent block
        if (!Character.isSpaceChar(c))
        {
            bs.set(x);

            //need to know location of master as it may not be in the corner.
            if (Character.toUpperCase(c) == 'M')
            {
                if (sdb.mps == null)
                    sdb.mps = ImmutableTriple.of(x, jrs.getOuter(), jrs.getInner());
                else
                    throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " Master specified more then one");
            }
        }
    }

    //=========================================================
    //               D e s e r i a l i z e   I O
    //=========================================================

    private static final String IO_DEFINITION = "definition";
    private static final String IO_DEFINITION_CHAR = "char";

    private static final String IO_CONFIGURATION = "configuration";

    private static void deserializeIO(StructureDefinitionBuilder sdb, JsonObject json)
    {
        if (!deserializeIOHasAllComponents(json))
            throw new JsonParseException(ERRORMSG + ": " + IO + " missing tag");

        final ImmutableListMultimap<Character, StructureBlockSideAccess> accessMap
                = deserializeIODefinition(json.getAsJsonObject(IO_DEFINITION));

        final Builder<Integer, ImmutableList<StructureBlockSideAccess>> ioConfig
                = new Builder<Integer, ImmutableList<StructureBlockSideAccess>>();

        final ItrJsonStructure itjs = new ItrJsonStructure(json.get(IO_CONFIGURATION));

        while (itjs.hasNext())
        {
            final JsonReadState<JsonElement> jrs = itjs.next();

            final char[] xLine = jrs.getValue().getAsString().toCharArray();

            for (int x=0; x<xLine.length; ++x)
            {
                final char c = xLine[x];

                if (!Character.isSpaceChar(c))
                    ioConfig.put(
                            StructureDefinition.getPosHash(x, jrs.getOuter(), jrs.getInner()),
                            accessMap.get(c));
            }
        }

        sdb.blockSideAccess = ioConfig.build();
    }

    private static boolean deserializeIOHasAllComponents(JsonObject json)
    {
        return json.has(IO_DEFINITION) &&
                json.has(IO_CONFIGURATION);
    }

    private static ImmutableListMultimap<Character, StructureBlockSideAccess> deserializeIODefinition(JsonObject obj)
    {
        final JsonArray ioDefinition = obj.getAsJsonArray();
        final ImmutableListMultimap.Builder<Character, StructureBlockSideAccess> builderSideInputDef
                = ImmutableListMultimap.builder();

        for(JsonElement jsonElementSideDef:ioDefinition)
        {
            final JsonObject jsonIODefinition = jsonElementSideDef.getAsJsonObject();

            //IdentityChar
            final char idc = jsonIODefinition.getAsJsonPrimitive(IO_DEFINITION_CHAR).getAsCharacter();

            builderSideInputDef.put(idc, gson.fromJson(jsonElementSideDef, StructureBlockSideAccess.class));
        }

        return builderSideInputDef.build();
    }

    //=========================================================
    //                     U t i l i t y
    //=========================================================

    private static byte[][][] jagedCheck(byte[][][] array)
    {
        for (final byte[][] i:array)
            if (i.length == array[0].length)
            {
                for (final byte[] ii : i)
                    if (ii.length != i[0].length)
                        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_METADATA + " not a square x array");
            } else
                throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_METADATA + " not a square z array");

        return array;
    }

    private static BitSet[][] jagedCheck(BitSet[][] array)
    {
        for (final BitSet[] i:array)
            if (i.length == array[0].length)
            {
                for (final BitSet ii : i)
                    if (ii.length() != i[0].length())
                        throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " not a square x array");
            } else
                throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " not a square z array");

        return array;
    }

    private static <E> E[][][] jagedCheck(E[][][] array, String errorLoc)
    {
        for (final E[][] i:array)
            if (i.length == array[0].length)
            {
                for (final E[] ii : i)
                    if (ii.length != i[0].length)
                        throw new JsonParseException(ERRORMSG + ": " + errorLoc+ " not a square x array");
            } else
                throw new JsonParseException(ERRORMSG + ": " + errorLoc + " not a square z array");

        return array;
    }


    //=========================================================
    // S t r u c t u r e   D e f i n i t i o n   B u i l d e r
    //=========================================================

    private final class StructureDefinitionBuilder
    {
        public BitSet[][] sbLayout;
        public boolean cleanUpOnBuild = true;

        public ImmutableTriple<Integer,Integer,Integer> adjustmentCtS;

        public ImmutableTriple<Integer,Integer,Integer> totalSize;
        public ImmutableTriple<Integer,Integer,Integer> mps;

        public Block[][][] blocks;
        public byte[][][] metadata;
        public ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> blockSideAccess;
        public float[][] collisionBoxes;

        public StructureDefinition build()
        {
            //TODO Finish
            return StructureDefinition.MISSING_STRUCTURE;
        }
    }

}
