package ru.multivarka.itempeek;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ItemPeek.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ItemPeek.MODID, value = Dist.CLIENT)
public class ItemPeekClient {
    private static KeyMapping SHOW_ITEM_KEY;

    public ItemPeekClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ItemPeek.LOGGER.info("Client setup initialized for itempeek");
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        SHOW_ITEM_KEY = new KeyMapping(
                "key.itempeek.show_item",
                GLFW.GLFW_KEY_T,
                "key.categories.itempeek");
        event.register(SHOW_ITEM_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (Screen.hasShiftDown() && SHOW_ITEM_KEY != null) {
            int boundKey = SHOW_ITEM_KEY.getKey().getValue();
            if (event.getKey() != boundKey) return;
            if (mc.screen instanceof AbstractContainerScreen<?> contScreen) {
                Slot hovered = contScreen.getSlotUnderMouse();
                if (hovered != null && hovered.hasItem()) {
                    int menuIndex = contScreen.getMenu().slots.indexOf(hovered);
                    if (menuIndex >= 0) {
                        Network.sendShowItemToServer(menuIndex);
                    }
                }
            }
        }
    }
}
