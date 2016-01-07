package mod.steamnsteel.structure.registry.StateCorrecter;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockStairs.EnumShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import static mod.steamnsteel.structure.coordinates.TransformLAG.localToGlobal;
import static net.minecraft.block.BlockStairs.*;
import static net.minecraft.block.BlockStairs.EnumShape.*;

public class StairsMinecraftRotation implements IStructurePatternStateCorrecter
{
    private static EnumShape[] opp = {
            STRAIGHT,
            INNER_RIGHT,
            INNER_LEFT,
            OUTER_RIGHT,
            OUTER_LEFT
    };

    @Override
    public IBlockState alterBlockState(IBlockState state, EnumFacing orientation, boolean mirror)
    {
        final EnumFacing facing = state.getValue(BlockDirectional.FACING);
        final EnumShape shape = state.getValue(SHAPE);

        return state
                .withProperty(BlockDirectional.FACING, localToGlobal(facing, orientation, mirror))
                .withProperty(SHAPE, mirror ? opp[shape.ordinal()] : shape);
    }
}
