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
package com.foudroyantfactotum.tool.structure.waila;

public final class WailaProvider
{
    public static final String WAILA = "Waila";

    public static void init()
    {
        //FMLInterModComms.sendMessage(WAILA, "register", WailaProvider.class.getCanonicalName() + ".callbackRegister");
    }


    /*@Optional.Method(modid = WAILA)
    public static void callbackRegister(IWailaRegistrar registrar)
    {
        final IWailaDataProvider structureShapeBlock = new WailaStructureShapeBlock();
        final IWailaDataProvider structureBlock = new WailaStructureBlock();

        registrar.registerStackProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerHeadProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerBodyProvider(structureShapeBlock, StructureShapeBlock.class);
        registrar.registerTailProvider(structureShapeBlock, StructureShapeBlock.class);

        registrar.registerHeadProvider(structureBlock, SteamNSteelStructureBlock.class);
        registrar.registerBodyProvider(structureBlock, SteamNSteelStructureBlock.class);
        registrar.registerTailProvider(structureBlock, SteamNSteelStructureBlock.class);
    }*/
}
