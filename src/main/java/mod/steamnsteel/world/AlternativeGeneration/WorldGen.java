package mod.steamnsteel.world.AlternativeGeneration;

import com.google.common.collect.Lists;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.BrushCube;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.IBrush;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.IDrawWithBrush;
import mod.steamnsteel.world.AlternativeGeneration.PaintBrush.Line;
import mod.steamnsteel.world.SchematicLoader;
import mod.steamnsteel.world.ore.RetroGenHandler;
import mod.steamnsteel.world.structure.RemnantRuinsGenerator;
import mod.steamnsteel.world.structure.StructureChunkGenerator;
import mod.steamnsteel.world.structure.StructureGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
        //event.newBiomeDecorator = new SteamNSteelBiomeDecorator();
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
        final Random randomGenerator = event.rand;
        final ChunkProviderSettings.Factory cps = new ChunkProviderSettings.Factory();

        final IBrush brush = new BrushCube(2, 1, 2);
        final IDrawWithBrush line = new Line(
                Math.toRadians(180) * randomGenerator.nextDouble(),
                Math.toRadians(180) * randomGenerator.nextDouble(),
                (int) (1000 * randomGenerator.nextDouble())
        );

        final IBlockState oreBlock = Blocks.iron_ore.getDefaultState();

        final Set<BlockPos> blocks = new HashSet<BlockPos>(100);
        final BlockPos blockpos = new BlockPos(
                randomGenerator.nextInt(16) + (event.chunkX << 4),
                randomGenerator.nextInt(cps.ironMaxHeight - cps.ironMinHeight) + cps.ironMinHeight,
                randomGenerator.nextInt(16) + (event.chunkZ << 4)
        );

        line.drawWithBrush(brush, blocks, blockpos);
        final float oreDropRate = cps.ironCount;

        for (final BlockPos pos : blocks)
        {
            if (event.chunkProvider.chunkExists(pos.getX() >> 4, pos.getZ() >> 4))
                if (event.chunkProvider.provideChunk(pos).getBlock(pos) == Blocks.stone)
                    event.chunkProvider.provideChunk(pos).setBlockState(pos, oreBlock);
        }
    }
}
