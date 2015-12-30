package mod.steamnsteel.block;

import net.minecraft.block.material.Material;

public class SteamNSteelDirectionalStorageBlock extends SteamNSteelDirectionalBlock
{
    public static final String STORAGE_BRASS_BLOCK = "storageBrass";
    public static final String STORAGE_BRONZE_BLOCK = "storageBronze";
    public static final String STORAGE_COPPER_BLOCK = "storageCopper";
    public static final String STORAGE_PLOTONIUM_BLOCK = "storagePlotonium";
    public static final String STORAGE_STEEL_BLOCK = "storageSteel";
    public static final String STORAGE_TIN_BLOCK = "storageTin";
    public static final String STORAGE_ZINC_BLOCK = "storageZinc";

    public SteamNSteelDirectionalStorageBlock(String name)
    {
        super(Material.iron);
        setUnlocalizedName(name);
        setHardness(5.0f);
        setResistance(10.0f);
        setStepSound(soundTypeMetal);
    }
}
