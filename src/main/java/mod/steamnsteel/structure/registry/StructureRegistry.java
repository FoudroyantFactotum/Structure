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
package mod.steamnsteel.structure.registry;

import mod.steamnsteel.TheMod;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StructureRegistry
{
    private static String STRUCTURE_LOCATION = "structure/";
    private static String STRUCTURE_FILE_EXTENSION = ".structure.json";

    private static Map<Integer, SteamNSteelStructureBlock> structures = new HashMap<Integer, SteamNSteelStructureBlock>();

    private static List<SteamNSteelStructureBlock> registeredStructures = new LinkedList<SteamNSteelStructureBlock>();

    public static void registerBlockForDataLoad(SteamNSteelStructureBlock block)
    {
        registeredStructures.add(block);
    }

    public static void loadRegisteredPatterns()
    {
        try
        {
            final Field structurePattern = SteamNSteelStructureBlock.class.getDeclaredField("structurePattern");
            structurePattern.setAccessible(true);

            for (SteamNSteelStructureBlock block: registeredStructures)
                structurePattern.set(block, registerPattern(block));

            Logger.info("Loaded all " + registeredStructures.size() + " Registered Patterns");

        } catch (NoSuchFieldException e)
        {
            Logger.info("\n\n\nNoSuchFieldException: " + e.getLocalizedMessage() + "\n\n\n");
        } catch (IllegalAccessException e)
        {
            Logger.info("\n\n\nIllegalAccessException: " + e.getLocalizedMessage() + "\n\n\n");
        }

        registeredStructures = null;
    }

    private StructureRegistry()
    {
        //no op
    }

    private static StructurePattern registerPattern(SteamNSteelStructureBlock block)
    {
        final String unlocName = block.getUnlocalizedName();
        final String blockName = getBlockName(unlocName);
        final ResourceLocation jsonStructure = getResourceLocation(getStructurePath(blockName));
        StructurePattern blockPattern = null;

        try
        {
            final IResource res = Minecraft.getMinecraft().getResourceManager().getResource(jsonStructure);
            final InputStreamReader inpStream = new InputStreamReader(res.getInputStream());
            final BufferedReader buffRead = new BufferedReader(inpStream);

            blockPattern = JSONStructurePattern.gson.fromJson(buffRead, StructurePattern.class);

            buffRead.close();
            inpStream.close();
        } catch (IOException e)
        {
            Logger.info("file does not exist : " + blockName + " : " + e.getMessage());
        }

        structures.put(unlocName.hashCode(), block);

        return blockPattern;
    }

    public static SteamNSteelStructureBlock getBlock(int hash)
    {
        return structures.get(hash);
    }

    private static String getBlockName(String unlocName)
    {
        return unlocName.substring(unlocName.indexOf(':')+1);
    }

    private static ResourceLocation getResourceLocation(String path)
    {
        return new ResourceLocation(TheMod.MOD_ID.toLowerCase(), path);
    }

    private static String getStructurePath(String name)
    {
        return STRUCTURE_LOCATION + name + STRUCTURE_FILE_EXTENSION;
    }

}
