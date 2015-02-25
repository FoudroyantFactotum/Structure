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
package mod.steamnsteel.structure;

import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.registry.StructureDefinition;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

public interface IStructureTE
{
    StructureDefinition getPattern();

    boolean hasNeighbour(ForgeDirection d);
    Block getTransmutedBlock();
    int getTransmutedMeta();


    void setBlockPattern(String name);
    void configureBlock(StructureBlockCoord sBlock);
}
