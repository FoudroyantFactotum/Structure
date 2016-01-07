package mod.steamnsteel.structure.IStructure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

public interface IPartBlockState
{
    ImmutableMap<IProperty, Comparable> getDefinitive();
    ImmutableList<IProperty> getIndefinitive();

    IBlockState getBlockState();
}
