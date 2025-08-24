package thaumic.tinkerer.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumic.tinkerer.client.core.handler.kami.KamiArmorClientHandler;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.item.kami.armor.ItemIchorclothArmorAdv;

/**
 * Created by Katrina on 28/02/14.
 */
@SideOnly(Side.CLIENT)
public class GemArmorKeyHandler {

    static KeyBinding SpecialAbility = new KeyBinding("ttmisc.toggleArmor", Keyboard.KEY_NONE, "ttmisc.keyCategory");

    public GemArmorKeyHandler() {
        FMLCommonHandler.instance().bus().register(this);
        ClientRegistry.registerKeyBinding(SpecialAbility);
    }

    @SubscribeEvent
    public void keyUp(InputEvent.KeyInputEvent event) {
        if (SpecialAbility.isPressed()) {
            final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if ((player.getCurrentArmor(0) != null
                    && player.getCurrentArmor(0).getItem() instanceof ItemIchorclothArmorAdv)
                    || (player.getCurrentArmor(1) != null
                            && player.getCurrentArmor(1).getItem() instanceof ItemIchorclothArmorAdv)
                    || (player.getCurrentArmor(2) != null
                            && player.getCurrentArmor(2).getItem() instanceof ItemIchorclothArmorAdv)
                    || (player.getCurrentArmor(3) != null
                            && player.getCurrentArmor(3).getItem() instanceof ItemIchorclothArmorAdv)) {
                KamiArmorClientHandler.SetStatus(!ThaumicTinkerer.proxy.armorStatus(player));
            }
        }
    }
}
