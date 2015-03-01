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


import com.google.gson.*;
import mod.steamnsteel.structure.registry.StructureBlockSideAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.Type;

public class JsonStructureBlockSideAccess implements JsonDeserializer<StructureBlockSideAccess>
{
    private static final String IO_DEFINITION_SIDES = "sides";
    private static final String IO_DEFINITION_ACCESSIBLESLOTS = "accessibleSlots";
    private static final String IO_DEFINITION_CANITEM = "can-Item";

    @Override
    public StructureBlockSideAccess deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        final JsonObject jsonIODefinition = json.getAsJsonObject();

        if (!deserializeIODefinitionHasAllComponents(jsonIODefinition))
            throw new JsonParseException("StructureBlockSideAccess missing tag");

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

        return new StructureBlockSideAccess(sideFlags, accessibleSlots, canInsertItem, canExtractItem);
    }

    private static boolean deserializeIODefinitionHasAllComponents(JsonObject json)
    {
        return  json.has(IO_DEFINITION_SIDES) &&
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
}
