package mod.steamnsteel.utility;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import mod.steamnsteel.TheMod;
import mod.steamnsteel.structure.net.StructureParticlePacket;

public final class ModNetwork
{
    public static SimpleNetworkWrapper network;

    public static void init()
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(TheMod.MOD_ID);

        network.registerMessage(StructureParticlePacket.Handler.class, StructureParticlePacket.class, 1, Side.CLIENT);
    }
}
