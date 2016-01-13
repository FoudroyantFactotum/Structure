package mod.steamnsteel.world;

import mod.steamnsteel.utility.log.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.lang.reflect.Field;

public class SteamNSteelBiomeDecorator extends BiomeDecorator
{
    Field fOreBlock;

    public SteamNSteelBiomeDecorator()
    {
        try
        {
            fOreBlock = WorldGenMinable.class.getDeclaredField("oreBlock");
            fOreBlock.setAccessible(true);
        } catch (NoSuchFieldException e)
        {
            Logger.severe(e.toString());
            Minecraft.getMinecraft().shutdown();
        }

    }

    @Override
    protected void genStandardOre1(int blockCount, WorldGenerator generator, int minHeight, int maxHeight)
    {
        //don't gen ores that are being generated through alternative means e.g. ore seams

        /*if (currentWorld.getWorldType() != WorldGen.worldType)
        {
            super.genStandardOre1(blockCount, generator, minHeight, maxHeight);
            return;
        }

        if (generator instanceof WorldGenMinable)
        {
            try
            {
                final IBlockState oreBlock = (IBlockState) fOreBlock.get(generator);

                for (final Block sOre : WorldGen.getMinecraftSeamOre())
                {
                    if (sOre == oreBlock.getBlock())
                    {
                        return;
                    }
                }

                super.genStandardOre1(blockCount, generator, minHeight, maxHeight);
            } catch (IllegalAccessException e)
            {
                Logger.severe(e.toString());
            }
        }*/
    }
}
