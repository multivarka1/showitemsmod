package ru.multivarka.itempeek;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;

public class ItemPeekClient {
    private static KeyMapping SHOW_ITEM_KEY;

    @Mod.EventBusSubscriber(modid = ItemPeek.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
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
    }

    @Mod.EventBusSubscriber(modid = ItemPeek.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
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
}
