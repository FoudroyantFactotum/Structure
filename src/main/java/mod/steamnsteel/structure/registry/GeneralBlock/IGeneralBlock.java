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

import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.block.Block;

/**
 * Blocks implementing this interface should never be registered with the ForgeBlock System. These are not blocks, but
 * ways of defining general blocks within the structures. eg. GeneralStone is not a battle field general but a way of
 * grouping all stone so that any "stone" blocks can be used in the structure.
 * <p/>
 * Any blocks defined as...
 * <i>~General:<b>BLOCK</b></i>
 * where BLOCK is the general block used and don't forget the tilde out the front.
 * <p/>
 * Additional Blocks can be defined using a states. eg. blocks must all be of the same type of stone, but any stone
 * could be used. Hence the addition of a reset function.
 */
public interface IGeneralBlock
{
    /**
     * Test if the block can be used.
     *
     * @param b   Block to test
     * @param pos Local space location
     * @return true if specified in Block System
     */
    boolean canBlockBeUsed(Block b, int meta, TripleCoord pos);


    /**
     * resets the state of the General Block after series of block tests have been done.
     */
    void resetGeneralBlock();
}
