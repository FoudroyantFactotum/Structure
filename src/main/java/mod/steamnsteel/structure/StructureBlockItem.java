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

import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.coordinates.StructureBlockIterator;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.List;

import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;

public class StructureBlockItem extends ItemBlock
{
    public StructureBlockItem(Block block)
    {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
        final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock) field_150939_a;

        if (player == null) return false;
        final Orientation o = Orientation.getdecodedOrientation(BlockDirectional.getDirection(MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5)));
        final boolean isMirrored = false; //player.isSneaking(); Disabled until fix :p todo fix structure mirroring

            //find master block location
            final ImmutableTriple<Integer, Integer, Integer> hSize = block.getPattern().getHalfBlockBounds();
        final ImmutableTriple<Integer, Integer, Integer> ml = block.getPattern().getMasterLocation();

        ImmutableTriple<Integer, Integer, Integer> mLoc
                = localToGlobal(
                hSize.getLeft() - ml.getLeft(), -ml.getMiddle(), hSize.getRight() - ml.getRight(),
                x, y, z,
                o, isMirrored, hSize.getRight());

        /*final List<Entity> entitysWithinBounds = world.getEntitiesWithinAABBExcludingEntity(null,
            SteamNSteelStructureBlock.getBoundingBoxUsingPattern(mx, y, mz, block.getPattern(), o));

        if (entitysWithinBounds.contains(player))
        {
            Logger.info("placeBlockAt: Collision intersects player");
            return false;
        }*/

        //check block locations
        final StructureBlockIterator itr = new StructureBlockIterator(block.getPattern(), Vec3.createVectorHelper(mLoc.getLeft(),mLoc.getMiddle(), mLoc.getRight()), o, isMirrored);

        Logger.info("Height: " + world.getActualHeight());
        while (itr.hasNext())
            if (!itr.next().isReplaceable(world)) return false;

        //check and then shift entitys within the region todo fix motions.
        /*for (final Entity entity : entitysWithinBounds)
        {
            final List<AxisAlignedBB> collisionBoxes = new ArrayList<AxisAlignedBB>(3);
            final AxisAlignedBB entityBounds = entity.boundingBox;

            if (entityBounds != null) {
                SteamNSteelStructureBlock.addCollisionBoxesToListUsingPattern(world, mx, y, mz, entityBounds, collisionBoxes, entity, block.getPattern(), o, isMirrored);

                final Vec3 averageLoc = getCenterOfBounds(collisionBoxes);

                if (averageLoc != null) {

                    Logger.info("placeBlockAt: " + averageLoc + " : " + mLoc);

                    averageLoc.xCoord = mx - averageLoc.xCoord;
                    averageLoc.yCoord = my - averageLoc.yCoord;
                    averageLoc.zCoord = mz - averageLoc.zCoord;

                    Logger.info("placeBlockAt2: " + averageLoc);

                    capVector(averageLoc, 1.0);

                    averageLoc.rotateAroundY((float)o.getRotationValue());

                    entity.addVelocity(averageLoc.xCoord, averageLoc.yCoord, averageLoc.zCoord);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }*/

        Logger.info("placeBlockAt: " + mLoc + " : " + world.getBlock(mLoc.getLeft(), mLoc.getMiddle(), mLoc.getRight()));

        world.setBlock(mLoc.getLeft(), mLoc.getMiddle(), mLoc.getRight(), block, metadata, 0x3);
        block.onBlockPlacedBy(world, mLoc.getLeft(), mLoc.getMiddle(), mLoc.getRight(), player, stack);
        block.onPostBlockPlaced(world, mLoc.getLeft(), mLoc.getMiddle(), mLoc.getRight(), world.getBlockMetadata(x,y,z));

        return true;
    }

    private static Vec3 getCenterOfBounds(List<AxisAlignedBB> boxes)
    {
        if (!boxes.isEmpty())
        {
            final AxisAlignedBB fstBox = boxes.get(0);
            final Vec3 center = Vec3.createVectorHelper(
                    (fstBox.maxX + fstBox.minX)/2.0,
                    (fstBox.maxY + fstBox.minY)/2.0,
                    (fstBox.maxZ + fstBox.minZ)/2.0
            );

            for (int i = 1; i < boxes.size(); ++i)
            {
                final AxisAlignedBB box = boxes.get(i);

                center.xCoord = (center.xCoord + (box.maxX + box.minX) /2.0)/2.0;
                center.yCoord = (center.yCoord + (box.maxY + box.minY) /2.0)/2.0;
                center.zCoord = (center.zCoord + (box.maxZ + box.minZ) /2.0)/2.0;
            }

            return center;
        }

        return null;
    }

    private static void capVector(Vec3 v, double cs)
    {
        v.xCoord = capSize(v.xCoord, cs);
        v.yCoord = capSize(v.yCoord, cs);
        v.zCoord = capSize(v.zCoord, cs);
    }

    private static double capSize(double s, double cs)
    {
        if (s > cs) s = cs;
        if (s < -cs) s = -cs;

        return s;
    }
}
