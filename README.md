##Structure

A tool for creating structures in minecraft for mods.
check [here](https://github.com/SteamNSteel/McGUI/blob/master/README.md) on how to shade this tool

###Base Requirements
The Structure Block is the block that you are rendering from and contains the main data.
On the other hand the rest of the blocks that make up the collision, item and fluid handling are the shape blocks.
There job is to handle the request and delegate them to the main block.

Blocks must extend from the StructureBlock class due to default implementations to keep the structure sound in game. This
includes Structure Shape Blocks.
TE have no requirement to be extended from the StructureTE, but currently holds a default implemeatation that can be used.

Structure Lib must be passed the modId for login purposes. Should be done before the first interaction with the library.
```java
StructureRegistry.setMOD_ID(String modId);
TransformLAG.initStatic();
```

Structures must be register with...
```java
StructureRegistry.registerStructureForLoad(StructureBlock, StructureShapeBlock);
```
where *StructureBlock* is the instance of your structure and *StructureShapeBlock* is the shape block that defines this structure.
This must be done in the preInit stage.

```java
StructureRegistry.loadRegisteredPatterns();
```
Must then be called in the Init block.

When adding support for Fluid and Item input the Structure TE must extend Implement the associated interface else things will fail.
eg for fluid support *TEStructure implements IStructureFluidHandler* **and** *TEShape implements IStructureShapeFluidHandler*

Client side must also init network.
```java
StructureNetwork.init();
```

To reload a Structure after a hot swap, register the following command
```java
StructureRegistry.CommandReloadStructures());
```
Please remember that this is a development command only. Do not leave it available in any release version.


//TODO
rewrite *this* messy explanation.

### Licensing

- Source code Copyright &copy; 2016 Foudroyant Factotum

  ![LGPL3](https://www.gnu.org/graphics/lgplv3-147x51.png)

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses>.
