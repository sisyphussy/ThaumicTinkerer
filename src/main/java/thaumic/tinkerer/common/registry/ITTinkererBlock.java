package thaumic.tinkerer.common.registry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by localmacaccount on 6/11/14.
 */
public interface ITTinkererBlock extends ITTinkererRegisterable {

    public String getBlockName();

    public boolean shouldRegister();

    public boolean shouldDisplayInTab();

    public Class<? extends ItemBlock> getItemBlock();

    public Class<? extends TileEntity> getTileEntity();

    default Block[] getMetaBlocks() {
        throw new IllegalArgumentException();
    }
}
