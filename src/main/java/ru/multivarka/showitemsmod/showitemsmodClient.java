package ru.multivarka.showitemsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.InputConstants;
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

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = showitemsmod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = showitemsmod.MODID, value = Dist.CLIENT)
public class showitemsmodClient {
    private static KeyMapping SHOW_ITEM_KEY;

    public showitemsmodClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        showitemsmod.LOGGER.info("Client setup initialized for showitemsmod");
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        SHOW_ITEM_KEY = new KeyMapping(
                "key.showitemsmod.show_item",
                GLFW.GLFW_KEY_T,
                "key.categories.showitemsmod");
        event.register(SHOW_ITEM_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Require Shift + bound key while a container screen is open and cursor hovers an item
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
