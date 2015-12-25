package mod.steamnsteel.client.codemodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import mod.steamnsteel.block.machine.PipeBlock;
import mod.steamnsteel.block.machine.PipeBlock.PipeStates;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.block.state.BlockState.StateImplementation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Vector3f;
import java.lang.reflect.Field;

/*
* Generates the pipe caps on the models. Saves having to copy and paste a ton of json
*/
public class PipeModel extends BaseCodeModel
{
    private static final ImmutableMap<String, String> flipData = ImmutableMap.of("flip-v", String.valueOf(true));
    private Field fobjModel = null;

    public PipeModel()
    {
        try
        {
            fobjModel = OBJBakedModel.class.getDeclaredField("model");
            fobjModel.setAccessible(true);
        } catch (NoSuchFieldException e)
        {
            Logger.severe("missing field 'model' in OBJBakedModel. Pipe caps will not be generated");
        }
    }

    @Override
    public void loadModel(ModelBakeEvent event)
    {
        if (fobjModel == null) return;

        for (final StateImplementation si : (ImmutableList<StateImplementation>) ModBlock.pipe.getBlockState().getValidStates())
        {
            final boolean capA = (Boolean) si.getValue(PipeBlock.END_A_CAP);
            final boolean capB = (Boolean) si.getValue(PipeBlock.END_B_CAP);

            if (capA || capB)
            {
                final Builder builder = new Builder<String, Pair<IModel, IModelState>>();
                final ModelResourceLocation mrl = sdm.getModelResourceLocation(si);
                final PipeStates pipeState = (PipeStates) si.getValue(PipeBlock.PIPE_STATE);

                final IModel modelCap = procsessModel(loadModel(event.modelLoader, new ResourceLocation("steamnsteel", "block/SSPipesCap.obj")), flipData);
                final OBJBakedModel bakedModel = (OBJBakedModel) event.modelRegistry.getObject(mrl);

                IModel model = null;
                try
                {
                    model = (IModel) fobjModel.get(bakedModel);
                } catch (IllegalAccessException e) {}

                if (capA) builder.put("capa", Pair.of(modelCap, getTransformForCap(pipeState.getEndA())));
                if (capB) builder.put("capb", Pair.of(modelCap, getTransformForCap(pipeState.getEndB())));


                event.modelRegistry.putObject(mrl,
                        new MultiModel(mrl, model, bakedModel.getState(), builder.build())
                                .bake(bakedModel.getState(), DefaultVertexFormats.BLOCK, textureGetter)
                );
            }
        }
    }

    private static IModel procsessModel(IModel model, ImmutableMap<String, String> data)
    {
        if (model instanceof OBJModel)
        {
            return ((OBJModel) model).process(data);
        }

        return model;
    }

    private TRSRTransformation getTransformForCap(EnumFacing e)
    {
        final float sz = 0.185f;
        switch (e)
        {
            case DOWN:
                return TRSRTransformation
                        .blockCornerToCenter(new TRSRTransformation(ModelRotation.X180_Y0))
                        .compose(new TRSRTransformation(new Vector3f(0.5f, -sz, -0.5f), null, null, null));
            case UP:
                return TRSRTransformation
                        .blockCornerToCenter(new TRSRTransformation(ModelRotation.X0_Y0))
                        .compose(new TRSRTransformation(new Vector3f(0.5f, 1.0f - sz, 0.5f), null, null, null));
            case NORTH:
                return TRSRTransformation
                        .blockCornerToCenter(new TRSRTransformation(ModelRotation.X270_Y180))
                        .compose(new TRSRTransformation(new Vector3f(-0.5f, -sz, -0.5f), null, null, null));
            case SOUTH:
                return TRSRTransformation
                        .blockCornerToCenter(new TRSRTransformation(ModelRotation.X90_Y180))
                        .compose(new TRSRTransformation(new Vector3f(-0.5f, 1.0f - sz, 0.5f), null, null, null));
            case EAST:
                return TRSRTransformation
                        .blockCornerToCenter(new TRSRTransformation(ModelRotation.X90_Y90))
                        .compose(new TRSRTransformation(new Vector3f(0.5f, 1.0f - sz, 0.5f), null, null, null));
            case WEST:
                return TRSRTransformation
                        .blockCornerToCenter(new TRSRTransformation(ModelRotation.X270_Y90))
                        .compose(new TRSRTransformation(new Vector3f(0.5f, -sz, -0.5f), null, null, null));
        }

        return TRSRTransformation.identity();
    }
}
