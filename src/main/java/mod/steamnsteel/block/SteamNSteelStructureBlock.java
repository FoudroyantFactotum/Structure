/*
 * Copyright (c) 2014 Rosie Alexander and Scott Killen.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */
package mod.steamnsteel.block;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mod.steamnsteel.TheMod;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.IStructurePatternBlock;
import mod.steamnsteel.utility.structure.JSONStructurePattern;
import mod.steamnsteel.utility.structure.StructurePattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IStructurePatternBlock
{
    private static String STRUCTURE_LOCATION = "structure/";
    private static String STRUCTURE_FILE_EXTENSION = ".structure.json";

    protected Optional<StructurePattern> blockPattern = Optional.absent();

    public static final int flagMirrored = 1<<2;

    @Override
    public StructurePattern getPattern()
    {
        if (!blockPattern.isPresent())
        {
            final ResourceLocation jsonStructure =
                    getResourceLocation(
                            getStructurePath(
                                    getUnwrappedUnlocalizedName(
                                            getBlockName(
                                                    getUnlocalizedName()))));

            try
            {
                final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(StructurePattern.class, new JSONStructurePattern()).create();
                final IResource res = Minecraft.getMinecraft().getResourceManager().getResource(jsonStructure);
                final BufferedReader buffRead = new BufferedReader(new InputStreamReader(res.getInputStream()));

                blockPattern = Optional.of(gson.fromJson(buffRead, StructurePattern.class));

            } catch (IOException e)
            {
                Logger.info("file does not exist : " + e.getMessage());
                blockPattern = Optional.of(StructurePattern.MISSING_STRUCTURE);
            }
        }

        return blockPattern.get();
    }

    private static ResourceLocation getResourceLocation(String path)
    {
        return new ResourceLocation(TheMod.MOD_ID.toLowerCase(), path);
    }

    private static String getStructurePath(String name)
    {
        return STRUCTURE_LOCATION + name + STRUCTURE_FILE_EXTENSION;
    }

    protected String getBlockName(String s)
    {
        final int p = s.indexOf(":");
        return s.substring(p+1, s.length());
    }
}
