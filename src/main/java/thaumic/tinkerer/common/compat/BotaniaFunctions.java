package thaumic.tinkerer.common.compat;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import thaumic.tinkerer.common.item.foci.ItemFocusDeflect;
import vazkii.botania.api.internal.IManaBurst;

/**
 * Created by Katrina on 30/01/2015.
 */
public class BotaniaFunctions {

    public static void AddBotaniaClasses() {
        ItemFocusDeflect.DeflectBlacklist.add(IManaBurst.class);
    }

    public static boolean isEntityHarmless(Entity entity) {
        ItemStack lens = ((IManaBurst) entity).getSourceLens();
        return lens.getItemDamage() != 8 && lens.getItemDamage() != 11;
    }
}
