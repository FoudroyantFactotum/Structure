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
package mod.steamnsteel.utility.crafting;

import com.google.gson.*;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.block.Block;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.*;

public class JSONStructurePattern implements JsonSerializer<StructurePattern>, JsonDeserializer<StructurePattern>
{
    private static final String ROWSPERLAYER = "rowsPerLayer";
    private static final String BLOCK_CHAR = "char";
    private static final String BLOCK_NAME = "block";
    private static final String BLOCK_MAP = "blocks";
    private static final String PATTERN = "pattern";

    @Override
    public JsonElement serialize(StructurePattern src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject result = new JsonObject();

        result.add(ROWSPERLAYER, new JsonPrimitive(src.rowsPerLayer));

        final JsonArray blocks = new JsonArray();
        for (Entry<Character, Block> pair:src.blocks.entrySet())
        {
            final UniqueIdentifier blockName = GameRegistry.findUniqueIdentifierFor(pair.getValue());
            checkNotNull(blockName, pair.toString() + " : Block does not exist");

            JsonObject blockData = new JsonObject();
            blockData.add(BLOCK_CHAR, new JsonPrimitive(pair.getKey()));
            blockData.add(BLOCK_NAME, new JsonPrimitive(blockName.toString()));
            blocks.add(blockData);
        }

        result.add(BLOCK_MAP, blocks);

        final JsonArray pattern = new JsonArray();
        for (String s:src.pattern) pattern.add(new JsonPrimitive(s));

        result.add(PATTERN, pattern);

        return result;
    }

    @Override
    public StructurePattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        final JsonObject obj = json.getAsJsonObject();

        final int rowsPerPlayer = obj.getAsJsonPrimitive(ROWSPERLAYER).getAsInt();
        return null;
        //TODO write rest of deserialize for StructurePattern
    }
}
