package mod.steamnsteel.world;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import mod.steamnsteel.configuration.Settings;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.world.ore.NiterOreGenerator;
import mod.steamnsteel.world.ore.OreGenerator;
import mod.steamnsteel.world.ore.RetroGenHandler;
import mod.steamnsteel.world.ore.SulfurOreGenerator;
import mod.steamnsteel.world.paintbrush.BrushCube;
import mod.steamnsteel.world.paintbrush.IBrush;
import mod.steamnsteel.world.paintbrush.IDrawWithBrush;
import mod.steamnsteel.world.paintbrush.Line;
import mod.steamnsteel.world.structure.RemnantRuinsGenerator;
import mod.steamnsteel.world.structure.StructureChunkGenerator;
import mod.steamnsteel.world.structure.StructureGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
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

    private static final List<OreGenerator> oreGens = Lists.newArrayList();
    private static final List<StructureGenerator> structureGens = Lists.newArrayList();
    public static final SchematicLoader schematicLoader = new SchematicLoader();

    private static Optional<ImmutableList<Block>> minecraftSeamOres = Optional.absent();

    public static void init()
    {
        createOreGenerators();
        register();
        RetroGenHandler.register();
    }

    private static void register()
    {
        MinecraftForge.TERRAIN_GEN_BUS.register(INSTANCE);
        MinecraftForge.ORE_GEN_BUS.register(INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    private static void createOreGenerators()
    {
        //For reference:
        //       ironConfiguration = new OreConfiguration(Blocks.Iron, 20, 8, 0, 64);
        final OreGenerator copperGen = new OreGenerator(ModBlock.oreCopper, 20, 6, 64);
        oreGens.add(copperGen);

        final NiterOreGenerator niterGen = new NiterOreGenerator();
        oreGens.add(niterGen);

        final SulfurOreGenerator sulfurGen = new SulfurOreGenerator();
        oreGens.add(sulfurGen);

        final OreGenerator tinGen = new OreGenerator(ModBlock.oreTin, 20, 3, 64);
        oreGens.add(tinGen);

        final OreGenerator zincGen = new OreGenerator(ModBlock.oreZinc, 20, 6, 64);
        oreGens.add(zincGen);

        if (Settings.World.doRetroOreGen())
        {
            RetroGenHandler.INSTANCE.register(copperGen);
            RetroGenHandler.INSTANCE.register(niterGen);
            RetroGenHandler.INSTANCE.register(sulfurGen);
            RetroGenHandler.INSTANCE.register(tinGen);
            RetroGenHandler.INSTANCE.register(zincGen);
        }
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
    public void OnPostOreGenerated(OreGenEvent.Post event)
    {
        for (final OreGenerator oreGen : oreGens)
            if (TerrainGen.generateOre(event.world, event.rand, oreGen, event.pos, OreGenEvent.GenerateMinable.EventType.CUSTOM))
                oreGen.generate(event.world, event.rand, event.pos);
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

    public void genSnSOres(PopulateChunkEvent.Post event)
    {
        //only generate ore every 3 chunks. So 3x3 chunks the ore will generate.(also called a swatch)
        //also checks if chunks exist on each side, and allows a seam across the board if it exists.
        final float overGen = 1.2f;

        //if (event.world.getWorldType() != worldType) return;
        if (event.chunkX % 3 != 0 || event.chunkZ % 3 != 0) return;

        final Random randomGenerator = event.rand;

        final IBrush brush = new BrushCube(randomGenerator, 0.25f);
        final Set<BlockPos> blocks = new HashSet<BlockPos>(2000);

        final Builder<OreRequirements> oreReq = new Builder<OreRequirements>();

        //oreReq.addAll(getChunkProviderOreRates(event.world.getWorldInfo().getGeneratorOptions()));
        oreReq.addAll(getSnsOreRates());

        for (final OreRequirements ore : oreReq.build())
        {
            final int oreGenTotal = (int) (ore.oreCount * ore.oreSize * 9 * overGen / 2);
            int genOreCount = 0;

            while (genOreCount < oreGenTotal * 0.9f)
            {
                while (blocks.size() < oreGenTotal - genOreCount)
                {
                    final BlockPos blockpos = new BlockPos(
                            randomGenerator.nextInt(48) + (event.chunkX << 4),
                            randomGenerator.nextInt(ore.maxHeight - ore.minHeight) + ore.minHeight,
                            randomGenerator.nextInt(48) + (event.chunkZ << 4)
                    );

                    final IDrawWithBrush line = new Line(
                            Math.toRadians(180) * randomGenerator.nextDouble(),
                            Math.toRadians(180) * randomGenerator.nextDouble(),
                            (int) ((ore.oreCount - 2) * randomGenerator.nextDouble()) + 2
                    );

                    line.drawWithBrush(brush, blocks, blockpos);
                }

                genOreCount +=
                        genGenericSnSOreSeam(
                                ore.block.getDefaultState(),
                                blocks,
                                event.chunkProvider,
                                oreGenTotal - genOreCount,
                                event.chunkX, event.chunkZ,
                                randomGenerator);
                blocks.clear();
            }
        }
    }

    private static ImmutableList<OreRequirements> getSnsOreRates()
    {
        final Builder<OreRequirements> builder = new Builder<OreRequirements>();
        try
        {
            final Field fNoOfBlocks = WorldGenMinable.class.getDeclaredField("numberOfBlocks");
            fNoOfBlocks.setAccessible(true);

            for (final OreGenerator g : oreGens)
            {
                try
                {
                    builder.add(new OreRequirements(g.getBlock(), g.getMaxHeight(), g.getMinHeight(), g.getClusterCount(), fNoOfBlocks.getInt(g)));
                } catch (IllegalAccessException e)
                {
                    Logger.severe(String.format("%s does not contain field 'numberOfBlocks'", g.getBlock().getUnlocalizedName()));
                }
            }
        } catch (NoSuchFieldException e)
        {
            Logger.severe(e.toString());
        }

        return builder.build();
    }

    /*public static ImmutableList<Block> getMinecraftSeamOre()
    {
        if (!minecraftSeamOres.isPresent())
        {
            final Builder<Block> builder = new Builder<Block>();

            for (final String ore : minecraftOres)
            {

                for (final Field f : Blocks.class.getFields())
                {
                    try
                    {
                        final String fName = f.getName();

                        if (fName.contains("ore") && fName.contains(ore))
                        {
                            final Block oreBlock = (Block) f.get(null);

                            if (oreBlock != null)
                            {
                                builder.add(oreBlock);
                            }
                        }
                    } catch (IllegalAccessException e)
                    {
                        Logger.severe(e.toString());
                    }
                }
            }

            minecraftSeamOres = Optional.of(builder.build());
        }

        return minecraftSeamOres.get();
    }*/

    /*private static ImmutableList<OreRequirements> getChunkProviderOreRates(String genOptions)
    {
        final ChunkProviderSettings.Factory cps = ChunkProviderSettings.Factory.jsonToFactory(genOptions == null ? "" : genOptions);
        final Builder<OreRequirements> builder = ImmutableList.builder();

        for (final String ore : minecraftOres)
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

                final int oreCount = ChunkProviderSettings.Factory.class.getField(ore + "Count").getInt(cps);
                final int oreSize = ChunkProviderSettings.Factory.class.getField(ore + "Size").getInt(cps);
                final int oreMaxHeight = ChunkProviderSettings.Factory.class.getField(ore + "MaxHeight").getInt(cps);
                final int oreMinHeight = ChunkProviderSettings.Factory.class.getField(ore + "MinHeight").getInt(cps);

                builder.add(new OreRequirements(oreBlock, oreMaxHeight, oreMinHeight, oreCount, oreSize));
            } catch (NoSuchFieldException e)
            {
                Logger.severe(e.toString());
            } catch (IllegalAccessException e)
            {
                Logger.severe(e.toString());
            }
        }

        return builder.build();
    }*/

    private static int genGenericSnSOreSeam(IBlockState ore, Set<BlockPos> blocks, IChunkProvider provider, int orePerSwatch, int originChunkX, int originChunkZ, Random rnd)
    {
        final double count = orePerSwatch / (double) blocks.size();
        int genOreCount = 0;

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
                        ++genOreCount;
                    }
                }
            }
        }

        return genOreCount;
    }

    private static class OreRequirements
    {
        public final Block block;
        public final int maxHeight;
        public final int minHeight;
        public final int oreCount;
        public final int oreSize;

        OreRequirements(Block block, int maxHeight, int minHeight, int oreCount, int oreSize)
        {
            this.block = block;
            this.maxHeight = maxHeight;
            this.minHeight = minHeight;
            this.oreCount = oreCount;
            this.oreSize = oreSize;
        }
    }
}
