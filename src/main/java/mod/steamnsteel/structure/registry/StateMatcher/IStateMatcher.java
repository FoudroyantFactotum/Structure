package mod.steamnsteel.structure.registry.StateMatcher;

import net.minecraft.block.state.IBlockState;

/**
 * Due to relying completely on Block States instead of metadata
 * addition check need to be done to fix issue as world.getBlockState(coord)
 * returns back a state which contains only the metadata equivalent not
 * the full state of the block
 */
public interface IStateMatcher
{
    boolean matchBlockState(IBlockState b1, IBlockState b2);
}
