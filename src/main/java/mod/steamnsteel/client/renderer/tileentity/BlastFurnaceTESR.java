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
package mod.steamnsteel.client.renderer.tileentity;

import com.google.common.base.Objects;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.block.structure.BlastFurnaceBlock;
import mod.steamnsteel.client.renderer.model.BlastFurnaceModel;
import mod.steamnsteel.tileentity.BlastFurnaceTE;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.lwjgl.opengl.GL11;

public class BlastFurnaceTESR extends SteamNSteelTESR
{
    private static final ResourceLocation TEXTURE = getResourceLocation(BlastFurnaceBlock.NAME);
    private static final ImmutableTriple<Float, Float, Float> SCALE = ImmutableTriple.of(1.0f, 1.0f, 1.0f);
    private static final ImmutableTriple<Float, Float, Float> OFFSET = ImmutableTriple.of(1.0f, 0.0f, 1.0f);

    private final BlastFurnaceModel model = new BlastFurnaceModel();

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
    {
        if (tileEntity instanceof BlastFurnaceTE)
        {
            final BlastFurnaceTE te = (BlastFurnaceTE) tileEntity;

            // Open Render buffer
            GL11.glPushMatrix();

            // Position Renderer
            GL11.glTranslatef((float) x, (float) y, (float) z);

            renderBallMill(te);

            // Close Render Buffer
            GL11.glPopMatrix();
        }
    }

    private void renderBallMill(BlastFurnaceTE te)
    {
        final int x = te.xCoord;
        final int y = te.yCoord;
        final int z = te.zCoord;
        final World world = te.getWorldObj();

        GL11.glPushMatrix();

        // Inherent adjustments to model
        GL11.glScalef(SCALE.left, SCALE.middle, SCALE.right);
        GL11.glTranslatef(0.5f,0,0.5f);

        // Orient the model to match the placement
        final int metadata = world.getBlockMetadata(x, y, z);
        final Orientation orientation = Orientation.getdecodedOrientation(metadata);

        GL11.glRotatef(getAngleFromOrientation(orientation), 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(OFFSET.left, OFFSET.middle, OFFSET.right);

        // If block is mirrored, flip faces and scale along -Z
        if ((metadata & SteamNSteelStructureBlock.flagMirrored) != 0) {
            GL11.glFrontFace(GL11.GL_CW);
            GL11.glScalef(1, 1, -1);
        }

        // Bind the texture
        bindTexture(TEXTURE);

        // Render
        model.render();

        // Flip faces back to default
        GL11.glFrontFace(GL11.GL_CCW);

        // Close Render Buffer
        GL11.glPopMatrix();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("model", model)
                .toString();
    }
}