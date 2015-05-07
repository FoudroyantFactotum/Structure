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

package mod.steamnsteel.client.renderer.item;

import com.google.common.base.Objects;
import cpw.mods.fml.client.FMLClientHandler;
import mod.steamnsteel.client.renderer.model.BlastFurnaceModel;
import mod.steamnsteel.client.renderer.tileentity.BlastFurnaceTESR;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.lwjgl.opengl.GL11;

public class BlastFurnaceItemRenderer implements IItemRenderer
{
    private static final ImmutableTriple<Float, Float, Float> OFFSET = ImmutableTriple.of(1.0f, 0.0f, 1.2f);
    private static final ImmutableTriple<Float, Float, Float> ENTITY_OFFSET = ImmutableTriple.of(0.0f, -1.0f, 0.0f);
    private static final ImmutableTriple<Float, Float, Float> INVENTORY_OFFSET = ImmutableTriple.of(1.0f, -0.5f, 1.2f);

    private static final ImmutableTriple<Float, Float, Float> SCALE = ImmutableTriple.of(0.5f, 0.5f, 0.5f);
    private static final ImmutableTriple<Float, Float, Float> SCALE_INVENTORY = ImmutableTriple.of(0.42f, 0.42f, 0.42f);

    private final BlastFurnaceModel model;

    public BlastFurnaceItemRenderer()
    {
        model = new BlastFurnaceModel();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        switch (type)
        {
            case ENTITY:
                renderBlastFurnace(ENTITY_OFFSET, SCALE);
                break;
            case EQUIPPED:
                renderBlastFurnace(OFFSET, SCALE);
                break;
            case EQUIPPED_FIRST_PERSON:
                renderBlastFurnace(OFFSET, SCALE);
                break;
            case INVENTORY:
                renderBlastFurnace(INVENTORY_OFFSET, SCALE_INVENTORY);
                break;
            default:
        }
    }

    private void renderBlastFurnace(ImmutableTriple<Float, Float, Float> offset, ImmutableTriple<Float, Float, Float> scale)
    {
        GL11.glPushMatrix();
        GL11.glScalef(scale.left, scale.middle, scale.right);
        GL11.glTranslatef(offset.left, offset.middle, offset.right);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(BlastFurnaceTESR.TEXTURE);

        model.render();

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
