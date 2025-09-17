package thaumic.tinkerer.common.registry;

import net.minecraft.item.Item;

/**
 * Created by localmacaccount on 6/9/14.
 */
public interface ITTinkererItem extends ITTinkererRegisterable {

    public String getItemName();

    public boolean shouldRegister();

    public boolean shouldDisplayInTab();

    default Item[] getMetaItems() {
        throw new IllegalArgumentException();
    }
}
