package mod.steamnsteel.structure.registry.GeneralBlock;

import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.block.Block;

/**
 * Blocks implementing this interface should never be registered with the ForgeBlock System. These are not blocks, but
 * ways of defining general blocks within the structures. eg. GeneralStone is not a battle field general but a way of
 * grouping all stone so that any "stone" blocks can be used in the structure.
 * <p/>
 * Any blocks defined as...
 * <i>~General:<b>BLOCK</b></i>
 * where BLOCK is the general block used and don't forget the tilde out the front.
 * <p/>
 * Additional Blocks can be defined using a states. eg. blocks must all be of the same type of stone, but any stone
 * could be used. Hence the addition of a reset function.
 */
public interface GeneralBlock
{
    /**
     * Test if the block can be used.
     *
     * @param b   Block to test
     * @param pos Local space location
     * @return true if specified in Block System
     */
    boolean canBlockBeUsed(Block b, int meta, TripleCoord pos);


    /**
     * resets the state of the General Block after series of block tests have been done.
     */
    void resetGeneralBlock();
}
