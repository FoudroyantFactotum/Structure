package mod.steamnsteel.client.codemodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import mod.steamnsteel.block.machine.PipeBlock;
import mod.steamnsteel.block.machine.PipeBlock.PipeStates;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.MultiModel;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Vector3f;
import java.lang.reflect.Field;

import static mod.steamnsteel.block.machine.PipeBlock.PipeStates.*;

/*
* Generates the pipe caps on the models. Saves having to copy and paste a ton of json
*/
public class PipeModel extends BaseCodeModel
{
    private final ImmutableSet<PipeStates> capFlip = ImmutableSet.copyOf(new PipeStates[]{DS, DE, SW, DW, UW});
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

        for (final IBlockState si : ModBlock.pipe.getBlockState().getValidStates())
        {
            final boolean capA = si.getValue(PipeBlock.END_A_CAP);
            final boolean capB = si.getValue(PipeBlock.END_B_CAP);

            if (capA || capB)
            {
                final Builder<String, Pair<IModel, IModelState>> builder = new Builder<>();
                final ModelResourceLocation mrl = sdm.getModelResourceLocation(si);
                final PipeStates pipeState = si.getValue(PipeBlock.PIPE_STATE);

                final IModel modelCap = procsessModel(loadModel(event.modelLoader, new ResourceLocation("steamnsteel", "block/SSPipesCap.obj")), flipData);
                final OBJBakedModel bakedModel = (OBJBakedModel) event.modelRegistry.getObject(mrl);

                IModel model = null;
                try
                {
                    model = (IModel) fobjModel.get(bakedModel);
                } catch (IllegalAccessException e) {}

                if (capA) builder.put("capa", Pair.of(modelCap, getTransformForCap(getCorrrectCapA(pipeState))));
                if (capB) builder.put("capb", Pair.of(modelCap, getTransformForCap(getCorrrectCapB(pipeState))));


                event.modelRegistry.putObject(mrl,
                        new MultiModel(mrl, model, bakedModel.getState(), builder.build())
                                .bake(bakedModel.getState(), DefaultVertexFormats.ITEM, textureGetter)
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

    private EnumFacing getCorrrectCapA(PipeStates states)
    {
        return capFlip.contains(states) ?
                states.getEndB() :
                states.getEndA();
    }

    private EnumFacing getCorrrectCapB(PipeStates states)
    {
        return capFlip.contains(states) ?
                states.getEndA() :
                states.getEndB();
    }

    private IModelState getTransformForCap(EnumFacing e)
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

