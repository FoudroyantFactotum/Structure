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
import mod.steamnsteel.client.renderer.model.BallMillModel;
import mod.steamnsteel.client.renderer.tileentity.BallMillTESR;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.lwjgl.opengl.GL11;

public class BallMillItemRenderer implements IItemRenderer
{
    private static final ImmutableTriple<Float, Float, Float> ENTITY_OFFSET = ImmutableTriple.of(1.2f,0.5f,1.0f);
    private static final ImmutableTriple<Float, Float, Float> EQUIPPED_OFFSET = ImmutableTriple.of(1.2f,0.5f,1.0f);
    private static final ImmutableTriple<Float, Float, Float> FIRST_PERSON_OFFSET = ImmutableTriple.of(1.2f,0.5f,1.0f);
    private static final ImmutableTriple<Float, Float, Float> INVENTORY_OFFSET = ImmutableTriple.of(-0.0f, -1.0f, 0.0f);

    private static final ImmutableTriple<Float, Float, Float> SCALE = ImmutableTriple.of(0.5f, 0.5f, 0.5f);
    private static final ImmutableTriple<Float, Float, Float> INVENTORY_SCALE = ImmutableTriple.of(0.35f, 0.35f, 0.35f);

    private final BallMillModel model;

    public BallMillItemRenderer()
    {
        model = new BallMillModel();
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
                renderBallMill(ENTITY_OFFSET, SCALE);
                break;
            case EQUIPPED:
                renderBallMill(EQUIPPED_OFFSET, SCALE);
                break;
            case EQUIPPED_FIRST_PERSON:
                renderBallMill(FIRST_PERSON_OFFSET, SCALE);
                break;
            case INVENTORY:
                renderBallMill(INVENTORY_OFFSET, INVENTORY_SCALE);
                break;
            default:
        }
    }

    private void renderBallMill(ImmutableTriple<Float, Float, Float> offset, ImmutableTriple<Float, Float, Float> scale)
    {
        GL11.glPushMatrix();
        GL11.glScalef(scale.left, scale.middle, scale.right);
        GL11.glTranslatef(offset.left, offset.middle, offset.right);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(BallMillTESR.TEXTURE);

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
