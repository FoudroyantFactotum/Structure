package mod.steamnsteel.structure.registry.StateCorrecter;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import static mod.steamnsteel.block.machine.PipeBlock.PIPE_STATE;
import static mod.steamnsteel.block.machine.PipeBlock.PipeStates;

public class PipeRotation implements IStructurePatternStateCorrecter
{
    @Override
    public IBlockState alterBlockState(IBlockState state, EnumFacing orientation, boolean mirror)
    {
        PipeStates pipeState = state.getValue(PIPE_STATE);

        switch (orientation)
        {
            case EAST:
                pipeState = mirror ? pipeState.rotateAnticlockwise() : pipeState.rotateClockwise();
                break;
            case SOUTH:
                pipeState = pipeState.rotateClockwise().rotateClockwise();
                break;
            case WEST:
                pipeState = mirror ? pipeState.rotateClockwise() : pipeState.rotateAnticlockwise();
                break;
            default: //North, Up, Down
        }

        return state.withProperty(PIPE_STATE, pipeState);
    }
}
