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
package mod.steamnsteel.tileentity;

import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.StructurePattern;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class SteamNSteelStructureTE extends SteamNSteelTE
{
    private Optional<AxisAlignedBB> renderBounds = Optional.absent();

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        if (!renderBounds.isPresent())
        {
            final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) getBlockType();
            final StructurePattern pattern = block.getPattern();

            if (pattern == StructurePattern.MISSING_STRUCTURE)
            {
                Logger.info("Missing Pattern for : " + block.getUnlocalizedName());
                renderBounds = Optional.of(MAX_BOUNDS());
            } else
            {
                final Vec3 size = block.getPattern().getSize();

                renderBounds = Optional.of(AxisAlignedBB.getBoundingBox(
                        xCoord - (int)size.xCoord/2,
                        yCoord,
                        zCoord - (int)size.zCoord/2,

                        xCoord + (int)size.xCoord/2+1,
                        yCoord + size.yCoord,
                        zCoord + (int)size.zCoord/2));
            }
        }

        return renderBounds.get();
    }

    private static AxisAlignedBB MAX_BOUNDS()
    {
        return AxisAlignedBB.getBoundingBox(
                Double.MIN_VALUE,
                Double.MIN_VALUE,
                Double.MIN_VALUE,
                Double.MAX_VALUE,
                Double.MAX_VALUE,
                Double.MAX_VALUE
        );
    }
}
