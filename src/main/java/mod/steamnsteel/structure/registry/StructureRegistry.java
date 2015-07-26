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

import com.google.common.collect.Lists;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.lang.reflect.Field;
import java.util.*;

public final class StructureRegistry
{
    private static Map<Integer, SteamNSteelStructureBlock> structures = new HashMap<Integer, SteamNSteelStructureBlock>();

    private static List<SteamNSteelStructureBlock> registeredStructures = new LinkedList<SteamNSteelStructureBlock>();

    /***
     * Register structures here for loading into system.
     * @param structure structure to be registered.
     */
    public static void registerStructureForLoad(SteamNSteelStructureBlock structure)
    {
        registeredStructures.add(structure);
    }

    /***
     * loadRegisteredPatterns() is called on onFMLInitialization after all blocks have been loaded required so that
     * blocks of other mod can be used within the structure.
     */
    public static void loadRegisteredPatterns()
    {
        try
        {
            final Field structurePattern = SteamNSteelStructureBlock.class.getDeclaredField("structureDefinition");
            structurePattern.setAccessible(true);

            final Field regHash = SteamNSteelStructureBlock.class.getDeclaredField("regHash");
            regHash.setAccessible(true);

            for (SteamNSteelStructureBlock block: registeredStructures)
            {
                structurePattern.set(block, block.getStructureBuild().build());
                regHash.set(block, block.getUnlocalizedName().hashCode());

                structures.put(block.getUnlocalizedName().hashCode(), block);
            }

            Logger.info("Analytical Engine constructed " + structures.size() + " noteworthy contraptions");

        } catch (NoSuchFieldException e)
        {
            Logger.info("\n\n\nNoSuchFieldException: " + e.getLocalizedMessage() + "\n\n\n");
        } catch (IllegalAccessException e)
        {
            Logger.info("\n\n\nIllegalAccessException: " + e.getLocalizedMessage() + "\n\n\n");
        }
    }

    private StructureRegistry()
    {
        //no op
    }

    public static Collection<SteamNSteelStructureBlock> getStructureList()
    {
        return structures.values();
    }

    public static SteamNSteelStructureBlock getStructureBlock(int hash)
    {
        return structures.get(hash);
    }

    /***
     * Command (class) for reloading the structures in-game after jvm hot swap
     */
    public static class CommandReloadStructures implements ICommand
    {
        @Override
        public String getCommandName()
        {
            return "RELOAD_STRUCTURES";
        }

        @Override
        public String getCommandUsage(ICommandSender player)
        {
            return "RELOAD_STRUCTURES (That's all there is, there isn't any more.)";
        }

        @Override
        public List getCommandAliases()
        {
            return Lists.newArrayList("RELOAD_STRUCTURES");
        }

        @Override
        public void processCommand(ICommandSender player, String[] args)
        {
            loadRegisteredPatterns();
            player.addChatMessage(new ChatComponentText("Analytical Engine reconstructed " + structures.size() + " noteworthy contraptions"));
        }

        @Override
        public boolean canCommandSenderUseCommand(ICommandSender player)
        {
            return true;
        }

        @Override
        public List addTabCompletionOptions(ICommandSender player, String[] args)
        {
            return null;
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index)
        {
            return false;
        }

        @Override
        public int compareTo(Object o)
        {
            return 0;
        }
    }
}
