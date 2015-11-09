package mod.steamnsteel.waila;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.block.structure.StructureShapeBlock;
import mod.steamnsteel.waila.structure.WailaStructureBlock;
import mod.steamnsteel.waila.structure.WailaStructureShapeBlock;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public final class WailaProvider
{
    public static final String WAILA = "Waila";

    public static void init()
    {
        FMLInterModComms.sendMessage(WAILA, "register", WailaProvider.class.getCanonicalName() + ".callbackRegister");
    }


    @Optional.Method(modid = WAILA)
    public static void callbackRegister(IWailaRegistrar registrar)
    {
        final IWailaDataProvider structureShapeBlock = new WailaStructureShapeBlock();
        final IWailaDataProvider structureBlock = new WailaStructureBlock();

        registrar.registerStackProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerHeadProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerBodyProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerTailProvider(structureShapeBlock, StructureShapeBlock.class);

        registrar.registerHeadProvider(structureBlock, SteamNSteelStructureBlock.class);
        registrar.registerBodyProvider(structureBlock, SteamNSteelStructureBlock.class);
        registrar.registerTailProvider(structureBlock, SteamNSteelStructureBlock.class);
    }
}
