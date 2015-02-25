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
package mod.steamnsteel.structure.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.lang.reflect.Type;
import java.util.BitSet;

class JSONStructureDefinition implements JsonDeserializer<StructureDefinition>
{
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(StructureDefinition.class, StructureDefinition.getJsonDeserializer())
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

        //todo finish

        sdb.cleanUpOnBuild = deserializeConstructionCleanUpOnBuild(json.get(CONSTRUCTION_CLEANUPONBUILD));

    }

    private static boolean deserializeConstructionHasAllComponents(JsonObject json)
    {
        return json.has(CONSTRUCTION_DEFINITION) &&
                json.has(CONSTRUCTION_PATTERN) &&
                json.has(CONSTRUCTION_METADATA) &&
                json.has(CONSTRUCTION_ADJUSTMENT) &&
                json.has(CONSTRUCTION_CLEANUPONBUILD);
    }

    private static ImmutableMap<Character, Block> deserializeConstructionDefinition(JsonElement json) throws JsonParseException
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

            return builder.build();
        }
        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + "-" + CONSTRUCTION_DEFINITION + " not a list");
    }

    private static boolean deserializeConstructionCleanUpOnBuild(JsonElement json) throws JsonParseException
    {
        if (json.isJsonPrimitive())
        {
            return json.getAsBoolean();
        }
        throw new JsonParseException(ERRORMSG + ": " + CONSTRUCTION + " " + CONSTRUCTION_CLEANUPONBUILD + " is incorrect type");
    }


    //=========================================================
    //              D e s e r i a l i z e   S i z e
    //=========================================================

    private static final String SIZE_CONFIGURATION = "configuration";

    private static void deserializeSize(StructureDefinitionBuilder sdb, JsonObject json) throws JsonParseException
    {
        if (json.has(SIZE_CONFIGURATION))
        {
            final JsonArray jsonConfiguration = json.getAsJsonArray(SIZE_CONFIGURATION);
            final BitSet[][] blockLocation = new BitSet[jsonConfiguration.size()][];
            int zSize = -1;

            for (int y=0; y<blockLocation.length; ++y)
            {
                final JsonArray zbLoc = jsonConfiguration.get(y).getAsJsonArray();
                blockLocation[y] = new BitSet[zbLoc.size()];
                int xSize = -1;

                if (zSize == -1)
                    zSize = zbLoc.size();
                else
                if (zSize != zbLoc.size())
                    throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " z size variation");

                for (int z=0; z<zbLoc.size(); ++z)
                {
                    final String sxLine = zbLoc.get(z).getAsString();
                    final BitSet xLine = new BitSet(sxLine.length());
                    final char[] xcLine =  sxLine.toCharArray();

                    if (xSize == -1)
                        xSize = xcLine.length;
                    else
                    if (xSize != xcLine.length)
                        throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " x size variation");

                    for (int x=0; x<xcLine.length; ++x)
                    {
                        final char c = xcLine[x];
                        if (!Character.isSpaceChar(c))
                        {
                            if (Character.toUpperCase(c) == 'M')
                            {
                                if (sdb.mps == null)
                                    sdb.mps = ImmutableTriple.of(x, y, z);
                                else
                                    throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " Master specified more then one");
                            }

                            xLine.set(x);
                        }

                        blockLocation[y][z] = xLine;
                    }
                }
            }

            if (sdb.mps == null)
                throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " Master not specified");
        } else
            throw new JsonParseException(ERRORMSG + ": " + SIZE + "-" + SIZE_CONFIGURATION + " Size is missing configuration");
    }

    //=========================================================
    //               D e s e r i a l i z e   I O
    //=========================================================

    private static final String IO_DEFINITION = "definition";
    private static final String IO_DEFINITION_CHAR = "char";
    private static final String IO_DEFINITION_SIDES = "sides";
    private static final String IO_DEFINITION_ACCESSIBLESLOTS = "accessibleSlots";
    private static final String IO_DEFINITION_CANITEM = "can-Item";

    private static final String IO_CONFIGURATION = "configuration";

    private static void deserializeIO(StructureDefinitionBuilder sdb, JsonObject json) throws JsonParseException
    {
        //todo finish
    }

    private static ImmutableListMultimap<Character, StructureBlockSideAccess> deserializeIODefinition(JsonObject obj) throws JsonParseException
    {
        final JsonArray ioDefinition = obj.getAsJsonArray();
        final ImmutableListMultimap.Builder<Character, StructureBlockSideAccess> builderSideInputDef
                = ImmutableListMultimap.builder();

        for(JsonElement jsonElementSideDef:ioDefinition)
        {
            final JsonObject jsonIODefinition = jsonElementSideDef.getAsJsonObject();

            if (!deserializeIODefinitionHasAllComponents(jsonIODefinition))
                throw new JsonParseException(ERRORMSG + ": " + IO + "-" + IO_DEFINITION + " missing tag");

            //IdentityChar
            final char idc = jsonIODefinition.getAsJsonPrimitive(IO_DEFINITION_CHAR).getAsCharacter();

            //InputSides
            final String inputSides = jsonIODefinition.get(IO_DEFINITION_SIDES).getAsString();
            byte sideFlags = 0;

            for (char c: inputSides.toUpperCase().toCharArray())
                sideFlags |= (byte) getFlagFromChar(c);

            //AccessibleSlots
            final JsonArray jsonAccessibleSlots = jsonIODefinition.getAsJsonArray(IO_DEFINITION_ACCESSIBLESLOTS);
            final int[] accessibleSlots = new int[jsonAccessibleSlots.size()];

            for (int i=0; i< jsonAccessibleSlots.size(); ++i)
                accessibleSlots[i] = jsonAccessibleSlots.get(i).getAsInt();

            //Can Input/Extract Item
            final String stringCanItem = jsonIODefinition.getAsJsonPrimitive(IO_DEFINITION_CANITEM).getAsString();
            final boolean canInsertItem = stringCanItem.toUpperCase().indexOf('I') != -1;
            final boolean canExtractItem = stringCanItem.toUpperCase().indexOf('E') != -1;


            builderSideInputDef.put(
                    idc,
                    new StructureBlockSideAccess(
                            sideFlags,
                            accessibleSlots,
                            canInsertItem,
                            canExtractItem)
            );
        }

        return builderSideInputDef.build();
    }

    private static boolean deserializeIODefinitionHasAllComponents(JsonObject json)
    {
        return json.has(IO_DEFINITION_CHAR) &&
                json.has(IO_DEFINITION_SIDES) &&
                json.has(IO_DEFINITION_ACCESSIBLESLOTS) &&
                json.has(IO_DEFINITION_CANITEM);
    }

    private static int getFlagFromChar(char c)
    {
        for (final ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            if (d.name().charAt(0) == c)
                return d.flag;

        return ForgeDirection.UNKNOWN.flag;
    }

    //=========================================================
    // S t r u c t u r e   D e f i n i t i o n   B u i l d e r
    //=========================================================

    private final class StructureDefinitionBuilder
    {
        public BitSet[][] sbLayout;
        public boolean cleanUpOnBuild = true;

        public ImmutableTriple<Integer,Integer,Integer> adjustmentCtS = ImmutableTriple.of(0,0,0);

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

    /*@Override
    public StructureDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {//TODO do validation on json file
        final JsonObject obj = json.getAsJsonObject();

        final ImmutableTriple<Integer,Integer,Integer> size = deserializeMasterBlock(obj);

        return new StructureDefinition(
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
        builder.put(' ', Blocks.air);   //Air

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
            final Builder<ImmutableList<Byte>> builder = ImmutableList.builder();

            for(JsonElement jsonMetaY:patternMeta)
            {
                for(JsonElement jsonMetaZ:jsonMetaY.getAsJsonArray())
                {
                    final Builder<Byte> builderInner = ImmutableList.builder();
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

    private static ImmutableMap<Integer, ImmutableList<StructureBlockSideAccess>> deserializeSideInput(JsonObject obj)
    {//todo redo. There are better ways of doing this.
        final JsonElement jsonBlockSideInput = obj.get(BLOCK_SIDE_INPUT);
        final JsonElement jsonBlockSideInputDefinition = obj.get(BLOCK_SIDE_INPUT_DEFINITION);

        if (jsonBlockSideInput != null && jsonBlockSideInputDefinition != null)
        {
            final ImmutableListMultimap<Character, StructureBlockSideAccess> sideInputDefinition =
                    deserializeSideInputDefinition(jsonBlockSideInputDefinition);

            final ImmutableMap.Builder<Integer, ImmutableList<StructureBlockSideAccess>> builder = ImmutableMap.builder();
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
                            ImmutableList<StructureBlockSideAccess> sideAccess = sideInputDefinition.get(c);

                            checkNotNull(sideAccess, "sideAccess char not defined -" + c + '-');

                            builder.put(StructureDefinition.getPosHash(locX, locY, locZ), sideAccess);

                        }
                    }
                }
            }

            return builder.build();
        }

        return null;
    }

    private static ImmutableListMultimap<Character, StructureBlockSideAccess> deserializeSideInputDefinition(JsonElement jsonBlockSideInputDefinition)
    {
        final JsonArray blockSideInputDefinition = jsonBlockSideInputDefinition.getAsJsonArray();
        final ImmutableListMultimap.Builder<Character, StructureBlockSideAccess> builderSideInputDef
                = ImmutableListMultimap.builder();

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
                            canExtractItem)
            );
        }

        return builderSideInputDef.build();
    }

   */
}
