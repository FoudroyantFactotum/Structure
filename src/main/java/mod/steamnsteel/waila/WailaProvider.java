package mod.steamnsteel.waila;

import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.waila.structure.WailaStructureBlock;
import mod.steamnsteel.waila.structure.WailaStructureShapeBlock;

public final class WailaProvider
{
    public static void init()
    {
        FMLInterModComms.sendMessage("Waila", "register", WailaProvider.class.getCanonicalName() + ".callbackRegister");
    }

    public static final IWailaDataProvider structureShapeBlock = new WailaStructureShapeBlock();
    public static final IWailaDataProvider structureBlock = new WailaStructureBlock();

    public static void callbackRegister(IWailaRegistrar registrar)
    {
        registrar.registerStackProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerHeadProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerBodyProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerTailProvider(structureShapeBlock, StructureShapeBlock.class);

        registrar.registerHeadProvider(structureBlock, SteamNSteelStructureBlock.class);
        registrar.registerBodyProvider(structureBlock, SteamNSteelStructureBlock.class);
        registrar.registerTailProvider(structureBlock, SteamNSteelStructureBlock.class);
    }
}
