package mod.steamnsteel.client.renderer.model;

import mod.steamnsteel.block.structure.ExampleBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class ExampleModel extends SteamNSteelModel
{
    private static final ResourceLocation MODEL = getResourceLocation(getModelPath(ExampleBlock.NAME));
    private final IModelCustom model;

    public ExampleModel() { model = AdvancedModelLoader.loadModel(MODEL); }

    public void render() { model.renderAll(); }
}
