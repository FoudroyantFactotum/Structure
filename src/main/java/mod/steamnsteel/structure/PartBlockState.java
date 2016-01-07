package mod.steamnsteel.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mod.steamnsteel.structure.IStructure.IPartBlockState;
import mod.steamnsteel.structure.StructureDefinitionBuilder.StructureDefinitionError;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartBlockState implements IPartBlockState
{
    public static final PartBlockState NO_BLOCK = new PartBlockState(ImmutableMap.<IProperty, Comparable>of(), ImmutableList.<IProperty>of(), null);

    final ImmutableMap<IProperty, Comparable> definitive;
    final ImmutableList<IProperty> indefinitive;

    final IBlockState block;

    private PartBlockState(ImmutableMap<IProperty, Comparable> definitive, ImmutableList<IProperty> indefinitive, IBlockState block)
    {
        this.definitive = definitive;
        this.indefinitive = indefinitive;
        this.block = block;
    }

    public static PartBlockState of(IBlockState b, String s)
    {
        final Collection<IProperty> defaultProp = b.getPropertyNames();
        final ImmutableMap.Builder<IProperty, Comparable> builderDef = ImmutableMap.builder();
        final Set<IProperty> properties = new HashSet<>(defaultProp);

        for (final String singleFullState : s.split(","))
        {
            if (!singleFullState.contains(":"))
            {
                throw new StructureDefinitionError("Missing property divider");
            }

            final String propName = singleFullState.split(":")[0];
            final String propVal = singleFullState.split(":")[1];

            boolean hasFoundProp = false;

            for (final IProperty prop : defaultProp)
            {
                if (prop.getName().equalsIgnoreCase(propName))
                {
                    boolean hasFoundVal = false;

                    for (final Comparable val : (Collection<Comparable>) prop.getAllowedValues())
                    {
                        if (val.toString().equalsIgnoreCase(propVal))
                        {
                            builderDef.put(prop, val);
                            properties.remove(prop);

                            hasFoundVal = true;
                            break;
                        }
                    }

                    if (!hasFoundVal)
                    {
                        throw new StructureDefinitionError(
                                "Property value missing: '" + prop.getName() +
                                        "' value missing: '" + propVal +
                                        "' in '" + prop.getAllowedValues() +
                                        "' on '" + b.getBlock().getUnlocalizedName() +
                                        "' with property: '" + b.getPropertyNames()
                        );
                    }

                    hasFoundProp = true;
                    break;
                }
            }

            if (!hasFoundProp)
            {
                throw new StructureDefinitionError(
                        "Missing property: '" + propName +
                                "' value: '" + propVal +
                                "' on block: '" + b.getBlock().getUnlocalizedName() +
                                "' with property: '" + b.getPropertyNames()
                );
            }
        }

        final ImmutableMap<IProperty, Comparable> definitive = builderDef.build();

        for (Map.Entry<IProperty, Comparable> entry : definitive.entrySet())
        {
            b = b.withProperty(entry.getKey(), entry.getValue());
        }

        return new PartBlockState(
                builderDef.build(),
                ImmutableList.copyOf(properties),
                b
        );

    }

    public static PartBlockState of(IBlockState b)
    {
        return new PartBlockState(
                ImmutableMap.<IProperty, Comparable>of(),
                ImmutableList.copyOf(b.getPropertyNames()),
                b
        );
    }

    public static PartBlockState of()
    {
        return NO_BLOCK;
    }

    @Override
    public ImmutableMap<IProperty, Comparable> getDefinitive()
    {
        return definitive;
    }

    @Override
    public ImmutableList<IProperty> getIndefinitive()
    {
        return indefinitive;
    }

    @Override
    public IBlockState getBlockState()
    {
        return block;
    }
}
