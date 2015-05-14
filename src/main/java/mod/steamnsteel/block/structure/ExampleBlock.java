package mod.steamnsteel.block.structure;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.structure.coordinates.StructureBlockCoord;
import mod.steamnsteel.structure.registry.StructureDefinition;
import mod.steamnsteel.tileentity.ExampleTE;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public class ExampleBlock extends SteamNSteelStructureBlock
{
    public static final String NAME = "example";

    public ExampleBlock()
    {
        setBlockName(NAME);
    }

    @SideOnly(Side.CLIENT)
    private static float rndRC()
    {
        final float max = 1;
        return ((float)Math.random())*max-(max/2.0f);
    }

    @Override
    public void spawnBreakParticle(World world, SteamNSteelStructureTE te, StructureBlockCoord coord, float sx, float sy, float sz)
    {
        final int x = coord.getX();
        final int y = coord.getY();
        final int z = coord.getZ();

        if (!coord.isEdge()) return;

        StructureDefinition pattern = te.getPattern();

        if (pattern != null) {
            final Block block = pattern.getBlock(coord.getLX(), coord.getLY(), coord.getLZ());

            if (block != null)
            {
                for (int i = 0; i < 20; ++i)
                {
                    world.spawnParticle("slime", x + rndRC(), y + 1, z + rndRC(), sx, sy, sz);
                    world.spawnParticle("slime", x, y + 0.5, z, sx, sy, sz);
                    world.spawnParticle("slime", x + rndRC(), y, z + rndRC(), sx, sy, sz);
                }
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new ExampleTE();
    }

    @Override
    public boolean onStructureBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz, ImmutableTriple<Byte, Byte, Byte> sbID, int sbx, int sby, int sbz)
    {
        final ExampleTE te = (ExampleTE) world.getTileEntity(x, y, z);

        if (te != null)
        {
            te.ISACTIVATE ^= true;
        }

        return super.onStructureBlockActivated(world, x, y, z, player, side, sx, sy, sz, sbID, sbx, sby, sbz);
    }
}
