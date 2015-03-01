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

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonReadState<E>
{
    private final int inner;
    private final int outer;

    private E value;

    public JsonReadState(int outer,int inner, E value)
    {
        this.inner = inner;
        this.outer = outer;

        checkNotNull(value);
        this.value = value;
    }

    public int getInner()
    {
        return inner;
    }

    public int getOuter()
    {
        return outer;
    }

    public E getValue()
    {
        return value;
    }
}
