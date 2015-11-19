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
package mod.steamnsteel.structure.registry.GeneralBlock;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Collection;

/**
 * A base class for all GeneralBlocks to extend.
 * used only to deal with type constraints.
 * todo work out better way of doing GeneralBlocks
 */
public abstract class GeneralBase implements IBlockState, IGeneralBlock
{
    @Override
    public Collection getPropertyNames()
    {
        throw new GeneralBlockError("Method not implemented in General Blocks");
    }

    @Override
    public Comparable getValue(IProperty property)
    {
        throw new GeneralBlockError("Method not implemented in General Blocks");
    }

    @Override
    public IBlockState withProperty(IProperty property, Comparable value)
    {
        throw new GeneralBlockError("Method not implemented in General Blocks");
    }

    @Override
    public IBlockState cycleProperty(IProperty property)
    {
        throw new GeneralBlockError("Method not implemented in General Blocks");
    }

    @Override
    public ImmutableMap getProperties()
    {
        throw new GeneralBlockError("Method not implemented in General Blocks");
    }

    @Override
    public Block getBlock()
    {
        throw new GeneralBlockError("Method not implemented in General Blocks");
    }
}
