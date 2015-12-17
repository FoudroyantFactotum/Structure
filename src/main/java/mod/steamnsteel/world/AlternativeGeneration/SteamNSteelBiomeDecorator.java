package mod.steamnsteel.world.AlternativeGeneration;

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
        super.genStandardOre1(blockCount, generator, minHeight, maxHeight);
        return;

        /*if (generator instanceof WorldGenMinable)
        {
            final IBrush brush = new BrushCube(1, 2, 1);
            final IDrawWithBrush line = new Line(
                    Math.toRadians(180) * randomGenerator.nextDouble(),
                    Math.toRadians(180) * randomGenerator.nextDouble(),
                    (int) (1000*randomGenerator.nextDouble())
            );

            try
            {
                final IBlockState oreBlock = (IBlockState) fOreBlock.get(generator);

                if (oreBlock.getBlock() != Blocks.iron_ore)
                {
                    super.genStandardOre1(blockCount, generator, minHeight, maxHeight);
                    return;
                }

                final Set<BlockPos> blocks = new HashSet<BlockPos>(100);
                final BlockPos blockpos = field_180294_c.add(
                        randomGenerator.nextInt(16),
                        randomGenerator.nextInt(maxHeight - minHeight) + minHeight,
                        randomGenerator.nextInt(16)
                );

                line.drawWithBrush(brush, blocks, blockpos);

                for (final BlockPos pos : blocks)
                {
                    if (blockpos.getX() >> 4 == pos.getX() >> 4 && blockpos.getZ() >> 4 == pos.getZ() >> 4)
                    {
                        if (currentWorld.getBlockState(pos).getBlock().isReplaceableOreGen(currentWorld, pos, BlockHelper.forBlock(Blocks.stone)))
                            currentWorld.setBlockState(pos, oreBlock, 0x2);
                    }
                }

            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }*/
    }
}
