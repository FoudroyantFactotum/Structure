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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

import java.lang.reflect.Type;

public class JSONBlock implements JsonDeserializer<Block>
{
    @Override
    public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive())
        {
            final String blockName = json.getAsString();
            final int blockDividePoint = blockName.indexOf(':');

            if (blockDividePoint != -1)
                return GameRegistry.findBlock(
                        blockName.substring(0, blockDividePoint),
                        blockName.substring(blockDividePoint + 1, blockName.length())
                        );
        }

        throw new JsonParseException("Block not in legible format");
    }
}
