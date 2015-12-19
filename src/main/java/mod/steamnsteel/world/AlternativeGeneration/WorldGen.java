package mod.steamnsteel.world.AlternativeGeneration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.BrushCube;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.IBrush;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.IDrawWithBrush;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.Line;
import mod.steamnsteel.world.SchematicLoader;
import mod.steamnsteel.world.ore.RetroGenHandler;
import mod.steamnsteel.world.structure.RemnantRuinsGenerator;
import mod.steamnsteel.world.structure.StructureChunkGenerator;
import mod.steamnsteel.world.structure.StructureGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public enum WorldGen
{
    INSTANCE;

    private static final List<StructureGenerator> structureGens = Lists.newArrayList();
    public static final SchematicLoader schematicLoader = new SchematicLoader();

    private static final WorldType worldType = new SteamNSteelWorldType();

    public static final String[] ores = {"iron", "coal", "gold", "diamond"};

    public static void init()
    {
        register();
        RetroGenHandler.register();
    }

    private static void register()
    {
        MinecraftForge.TERRAIN_GEN_BUS.register(INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void addBiomeDecorator(BiomeEvent.CreateDecorator event)
    {
        event.newBiomeDecorator = new SteamNSteelBiomeDecorator();
    }


    @SubscribeEvent
    public void OnWorldStarted(WorldEvent.Load worldLoadEvent)
    {
        structureGens.clear();
        final RemnantRuinsGenerator ruinsGenerator = new RemnantRuinsGenerator();
        structureGens.add(ruinsGenerator);
    }

    @SubscribeEvent
    public void OnPostPopulateChunkEvent(PopulateChunkEvent.Post event)
    {
        if (event.hasVillageGenerated)
        {
            return;
        }
        for (final StructureGenerator structureGen : structureGens)
        {
            StructureChunkGenerator structureToGenerate = structureGen.getStructureChunkToGenerate(event.world, event.chunkX, event.chunkZ);
            if (structureToGenerate != null)
            {
                structureToGenerate.generate();
            }
        }
    }

    @SubscribeEvent
    public void genSnSOres(PopulateChunkEvent.Post event)
    {
        //only generate ore every 3 chunks. so 3x3 chunks the ore will generate.
        //also checks if chunks exist on each side, and allows a seam across the board if it exists.
        final float overGen = 1.1f;

        if (event.chunkX % 3 != 0 || event.chunkZ % 3 != 0) return;

        final Random randomGenerator = event.rand;

        final IBrush brush = new BrushCube(randomGenerator, 0.1f);
        final Set<BlockPos> blocks = new HashSet<BlockPos>(2000);

        for (final OreRequirements ore : getChunkProviderOreRates(event.world.getWorldInfo().getGeneratorOptions()))
        {
            while (blocks.size() < ore.oreCount*ore.oreSize*9*overGen/2)
            {
                final IDrawWithBrush line = new Line(
                    Math.toRadians(180) * randomGenerator.nextDouble(),
                    Math.toRadians(180) * randomGenerator.nextDouble(),
                    (int) ((ore.oreCount-2) * randomGenerator.nextDouble()) + 2
                );

                final BlockPos blockpos = new BlockPos(
                        randomGenerator.nextInt(48) + (event.chunkX << 4),
                        randomGenerator.nextInt(ore.maxHeight - ore.minHeight) + ore.minHeight,
                        randomGenerator.nextInt(48) + (event.chunkZ << 4)
                );

                line.drawWithBrush(brush, blocks, blockpos);
            }

            genGenericSnSOreSeam(ore.block.getDefaultState(), blocks, event.chunkProvider, ore.oreCount*ore.oreSize*9/2, event.chunkX, event.chunkZ, randomGenerator);
            blocks.clear();
        }

    }

    private static ImmutableList<OreRequirements> getChunkProviderOreRates(String genOptions)
    {
        final ChunkProviderSettings.Factory cps = ChunkProviderSettings.Factory.jsonToFactory(genOptions == null ? "" : genOptions);
        final Builder<OreRequirements> builder = ImmutableList.builder();

        for (final String ore : ores)
        {
            try
            {
                Block oreBlock = null;

                for (final Field f : Blocks.class.getFields())
                {
                    final String fName = f.getName();

                    if (fName.contains("ore") && fName.contains(ore))
                    {
                        oreBlock = (Block) f.get(null);
                        break;
                    }
                }

                if (oreBlock == null)
                {
                    Logger.severe(String.format("ore: %s nonexistent in class<Blocks>", ore));
                    break;
                }

                final int oreCount      = ChunkProviderSettings.Factory.class.getField(ore + "Count").getInt(cps);
                final int oreSize       = ChunkProviderSettings.Factory.class.getField(ore + "Size").getInt(cps);
                final int oreMaxHeight  = ChunkProviderSettings.Factory.class.getField(ore + "MaxHeight").getInt(cps);
                final int oreMinHeight  = ChunkProviderSettings.Factory.class.getField(ore + "MinHeight").getInt(cps);

                builder.add(new OreRequirements(oreBlock, oreMaxHeight, oreMinHeight, oreCount, oreSize));
            } catch (NoSuchFieldException e)
            {
                Logger.severe(e.getLocalizedMessage());
            } catch (IllegalAccessException e)
            {
                Logger.severe(e.getLocalizedMessage());
            }
        }

        return builder.build();
    }

    private static void genGenericSnSOreSeam(IBlockState ore, Set<BlockPos> blocks, IChunkProvider provider,int orePerSwatch, int originChunkX, int originChunkZ, Random rnd)
    {
        final double count = orePerSwatch / (double) blocks.size();

        //Logger.info(String.format(
        //        "Swatch (%s,%s) keeping %s/%s=%.4f of %s", originChunkX, originChunkZ, orePerSwatch, blocks.size(), count, ore.getBlock().getLocalizedName()));

        for (final BlockPos pos : blocks)
        {
            final int normChunkX = (pos.getX() >> 4) - originChunkX;
            final int normChunkZ = (pos.getZ() >> 4) - originChunkZ;

            if ((Math.abs(normChunkX) < 3 && Math.abs(normChunkZ) < 3) || provider.chunkExists(pos.getX() >> 4, pos.getZ() >> 4))
            {
                if (rnd.nextDouble() < count)
                {
                    if (provider.provideChunk(pos).getBlock(pos) == Blocks.stone)
                    {
                        provider.provideChunk(pos).setBlockState(pos, ore);
                    }
                }
            }
        }
    }

    private static class OreRequirements
    {
        public final Block block;
        public final int maxHeight;
        public final int minHeight;
        public final int oreCount;
        public final int oreSize;

        public OreRequirements(Block block, int maxHeight, int minHeight, int oreCount, int oreSize)
        {
            this.block = block;
            this.maxHeight = maxHeight;
            this.minHeight = minHeight;
            this.oreCount = oreCount;
            this.oreSize = oreSize;
        }
    }
}
