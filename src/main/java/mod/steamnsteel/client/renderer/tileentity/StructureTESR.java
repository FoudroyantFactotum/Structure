package mod.steamnsteel.client.renderer.tileentity;

import mod.steamnsteel.tileentity.structure.SteamNSteelStructureTE;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

public class StructureTESR extends TileEntitySpecialRenderer
{
    @Override
    public void renderTileEntityAt(TileEntity ute, double x, double y, double z, float partialTicks, int destroyStage)
    {
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) ute;
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        final BlockPos pos = te.getPos();
        final IBlockState state = te.getWorld().getBlockState(pos);
        final IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

        bindTexture(TextureMap.locationBlocksTexture);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(Minecraft.isAmbientOcclusionEnabled()? 7425 : 7424);

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        worldRenderer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        worldRenderer.color(255, 255, 255, 255);

        blockRenderer.getBlockModelRenderer().renderModelStandard(te.getWorld(), model, te.getBlockType(), pos, worldRenderer, true);

        worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
    }
}
