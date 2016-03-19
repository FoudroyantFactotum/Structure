/*
 * Copyright (c) 2016 Foudroyant Factotum
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
package com.foudroyantfactotum.tool.structure;

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.block.StructureShapeBlock;
import com.foudroyantfactotum.tool.structure.utility.StructureDefinitionBuilder.StructureDefinitionError;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.util.Pair;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.ProgressManager;

import java.util.*;

import static net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public final class StructureRegistry
{
    private static Map<Integer, StructureBlock> structures = new HashMap<>();
    private static List<Pair<StructureBlock, StructureShapeBlock>> registeredStructures = new LinkedList<>();

    private static String MOD_ID = null;

    public static String getMOD_ID()
    {
        return MOD_ID;
    }

    public static void setMOD_ID(String modId)
    {
        if (MOD_ID == null)
        {
            MOD_ID = modId;
        }
    }

    /***
     * Register structures here for loading into system.
     *
     * @param structure structure to be registered.
     */
    public static void registerStructureForLoad(StructureBlock structure, StructureShapeBlock shape)
    {
        //todo add test for correct TE configuration
        registeredStructures.add(Pair.of(structure, shape));
    }

    /***
     * loadRegisteredPatterns() is called on onFMLInitialization after all blocks have been loaded. This allows for
     * cross-mod blocks to be used within the structure.
     */
    public static void loadRegisteredPatterns()
    {
        final ProgressBar blockBar = ProgressManager.push("Structure", registeredStructures.size());

        for (final Pair<StructureBlock, StructureShapeBlock> strucPair : registeredStructures)
        {
            final StructureBlock block  = strucPair.first();

            blockBar.step(block.getLocalizedName());

            try
            {
                block.setStructureDefinition(
                        block.getStructureBuild().build(),
                        strucPair.second(),
                        block.getUnlocalizedName().hashCode()
                );

                structures.put(block.getUnlocalizedName().hashCode(), block);
            } catch (StructureDefinitionError e)
            {
                throw new StructureDefinitionError(e.getMessage() + " on '" + block.getUnlocalizedName() + '\'');
            }
        }

        ProgressManager.pop(blockBar);

        //StructureLogger.info("Analytical Engine constructed " + structures.size() + " noteworthy contraptions");
    }

    private StructureRegistry()
    {
        //no op
    }

    public static Collection<StructureBlock> getStructureList()
    {
        return structures.values();
    }

    public static StructureBlock getStructureBlock(int hash)
    {
        return structures.get(hash);
    }

    /***
     * Command (class) for reloading the structures in-game after jvm hot swap
     * Should only be enabled for development purposes.
     */
    public static class CommandReloadStructures implements ICommand
    {
        private static final String COMMAND = "RELOAD_STRUCTURES";
        private static final List<String> aliases = Lists.newArrayList(COMMAND);

        @Override
        public String getCommandName()
        {
            return COMMAND;
        }

        @Override
        public String getCommandUsage(ICommandSender player)
        {
            return COMMAND;
        }

        @Override
        public List<String> getCommandAliases()
        {
            return aliases;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            loadRegisteredPatterns();
            sender.addChatMessage(new TextComponentString("Reconstructed " + structures.size() + " structures"));
        }

        //restrict usage of development function
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            if (sender instanceof EntityPlayer)
            {
                final EntityPlayer ep = (EntityPlayer) sender;

                return ep.capabilities.isCreativeMode;
            }

            return false;
        }

        @Override
        public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
        {
            return null;
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index)
        {
            return false;
        }

        @Override
        public int compareTo(ICommand iCommand)
        {
            return 0;
        }
    }
}
