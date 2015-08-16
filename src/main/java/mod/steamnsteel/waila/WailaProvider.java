package mod.steamnsteel.waila;

import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.waila.structure.WailaStructureShapeBlock;

public final class WailaProvider
{
    public static void init()
    {
        FMLInterModComms.sendMessage("Waila", "register", WailaProvider.class.getCanonicalName() + ".callbackRegister");
    }

    public static final IWailaDataProvider structureShapeBlock = new WailaStructureShapeBlock();

    public static void callbackRegister(IWailaRegistrar registrar)
    {
        registrar.registerHeadProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerStackProvider(structureShapeBlock, StructureShapeBlock.class);
    }
}
