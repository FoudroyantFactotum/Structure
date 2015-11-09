package mod.steamnsteel.utility;

import mod.steamnsteel.TheMod;
import mod.steamnsteel.structure.net.StructurePacket;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ModNetwork
{
    public static SimpleNetworkWrapper network;

    public static void init()
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(TheMod.MOD_ID);

        network.registerMessage(StructurePacket.Handler.class, StructurePacket.class, 1, Side.CLIENT);
    }
}
