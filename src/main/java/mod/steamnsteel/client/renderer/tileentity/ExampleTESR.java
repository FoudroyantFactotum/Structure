package mod.steamnsteel.client.renderer.tileentity;

import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.block.structure.ExampleBlock;
import mod.steamnsteel.client.renderer.model.ExampleModel;
import mod.steamnsteel.tileentity.ExampleTE;
import mod.steamnsteel.utility.Orientation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.lwjgl.opengl.GL11;

public class ExampleTESR extends SteamNSteelTESR
{
    private static ImmutableTriple<Float, Float, Float> SCALE = ImmutableTriple.of(1.0f, 1.0f, 1.0f);
    private static ImmutableTriple<Float, Float, Float> OFFSET = ImmutableTriple.of(0.0f, 0.0f, 3.0f);
    public static final ResourceLocation TEXTURE = getResourceLocation(ExampleBlock.NAME);
    private final ExampleModel model = new ExampleModel();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float tick)
    {
        SCALE = ImmutableTriple.of(1.0f, 1.0f, 1.0f);
        OFFSET = ImmutableTriple.of(-0.5f, 0.0f,2.5f);

        // Open Render buffer
        GL11.glPushMatrix();

        // Position Renderer
        GL11.glTranslatef((float) x, (float) y, (float) z);
        //GL11.glScalef(0.2f, 0.2f, 0.2f);

        renderExample((ExampleTE) te);

        // Close Render Buffer
        GL11.glPopMatrix();
    }

    private void renderExample(ExampleTE te)
    {
        final int x = te.xCoord;
        final int y = te.yCoord;
        final int z = te.zCoord;
        final World world = te.getWorldObj();

        GL11.glPushMatrix();

        // Inherent adjustments to model
        GL11.glScalef(SCALE.left, SCALE.middle, SCALE.right);
        GL11.glTranslatef(0.5f, 0, 0.5f);

        // Orient the model to match the placement
        final int metadata = world.getBlockMetadata(x, y, z);
        final Orientation orientation = Orientation.getdecodedOrientation(metadata);

        // If block is mirrored, flip faces and scale along -Z
        if ((metadata & SteamNSteelStructureBlock.flagMirrored) != 0) {
            GL11.glFrontFace(GL11.GL_CW);
            GL11.glScalef(1, 1, -1);
        }

        GL11.glRotatef(getAngleFromOrientation(orientation), 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(OFFSET.left, OFFSET.middle, OFFSET.right);

        // Bind the texture
        bindTexture(TEXTURE);

        // Render
        model.render();

        // Flip faces back to default
        GL11.glFrontFace(GL11.GL_CCW);

        // Close Render Buffer
        GL11.glPopMatrix();
    }
}
