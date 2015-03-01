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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ItrJsonStructure implements Iterator<JsonReadState<JsonElement>>
{
    private boolean hasNext = true;
    private Iterator<JsonElement> jaOuter = null;
    private Iterator<JsonElement> jaInner = null;

    private int outer = 0;
    private int inner = 0;

    private int outerSize = -1;
    private int innerSize = -1;

    private ItrJsonStructure ()
    {
        //no op
    }

    public ItrJsonStructure(JsonElement json)
    {
        if (json.isJsonArray())
        {
            final JsonArray ja = json.getAsJsonArray();
            jaOuter = ja.iterator();
            outerSize = ja.size();

            if (ja.size() > -1 && ja.get(0).isJsonArray())
            {
                final JsonArray ja2 = ja.get(0).getAsJsonArray();
                if (ja2.size() > -1)
                {
                    jaInner = ja2.iterator();
                    return;
                }
            }
        }
        hasNext = false;
    }


    @Override
    public boolean hasNext()
    {
        return hasNext &&
                (jaOuter.hasNext() || jaInner.hasNext());
    }

    @Override
    public JsonReadState<JsonElement> next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        if (!jaInner.hasNext())
        {
            final JsonArray ja = jaOuter.next().getAsJsonArray();

            innerSize = ja.size();
            jaInner = ja.iterator();
            ++outer;
            inner = 0;
        }

        return new JsonReadState<JsonElement>(outer,inner++, jaInner.next());
    }

    public int getInnerSize()
    {
        return innerSize;
    }

    public int getOuterSize()
    {
        return outerSize;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
