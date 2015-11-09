package mod.steamnsteel.utility;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.block.properties.PropertyEnum;

import java.util.Collection;

public class PropertyOrientation extends PropertyEnum
{
    protected PropertyOrientation(String name, Collection values)
    {
        super(name, Orientation.class, values);
    }

    public static PropertyOrientation create(String name)
    {
        return create(name, Predicates.alwaysTrue());
    }

    public static PropertyOrientation create(String name, Predicate filter)
    {
        return create(name, Collections2.filter(Lists.newArrayList(Orientation.values()), filter));
    }

    public static PropertyOrientation create(String name, Collection values)
    {
        return new PropertyOrientation(name, values);
    }
}
