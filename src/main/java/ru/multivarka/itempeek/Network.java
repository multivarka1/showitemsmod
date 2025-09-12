package ru.multivarka.itempeek;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;

@EventBusSubscriber(modid = ItemPeek.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Network {
    private Network() {}

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new net.minecraft.resources.ResourceLocation(ItemPeek.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                ItemPeekMessage.class,
                ItemPeekMessage::encode,
                ItemPeekMessage::decode,
                (msg, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ServerPlayer player = ctx.getSender();
                    if (player != null) {
                        ctx.enqueueWork(() -> handleShowItem(player, msg.slotIndex));
                    }
                    ctx.setPacketHandled(true);
                }
        );
    }

    private static void handleShowItem(ServerPlayer player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= player.containerMenu.slots.size()) {
            return;
        }

        ItemStack stack = player.containerMenu.getSlot(slotIndex).getItem();
        if (stack.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.itempeek.no_item").withStyle(ChatFormatting.RED));
            return;
        }

        Component itemName = stack.getHoverName().copy();

        ChatFormatting rarityColor;
        Rarity rarity = stack.getRarity();
        switch (rarity) {
            case UNCOMMON -> rarityColor = ChatFormatting.YELLOW;
            case RARE -> rarityColor = ChatFormatting.AQUA;
            case EPIC -> rarityColor = ChatFormatting.LIGHT_PURPLE;
            default -> rarityColor = ChatFormatting.WHITE;
        }

        Component itemColored = itemName.copy().withStyle(style -> style.withColor(rarityColor).withItalic(Boolean.FALSE));

        Component leftBracket = Component.literal("[").withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(Boolean.FALSE));
        Component rightBracket = Component.literal("]").withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(Boolean.FALSE));
        Component shown = Component.literal(" ")
                .append(leftBracket)
                .append(itemColored)
                .append(rightBracket);

        try {
            HoverEvent.ItemStackInfo info = null;
            try {
                var of = HoverEvent.ItemStackInfo.class.getDeclaredMethod("of", ItemStack.class);
                info = (HoverEvent.ItemStackInfo) of.invoke(null, stack);
            } catch (NoSuchMethodException e1) {
                try {
                    var create = HoverEvent.ItemStackInfo.class.getDeclaredMethod("create", ItemStack.class);
                    info = (HoverEvent.ItemStackInfo) create.invoke(null, stack);
                } catch (NoSuchMethodException e2) {
                    try {
                        var ctor = HoverEvent.ItemStackInfo.class.getDeclaredConstructor(ItemStack.class);
                        ctor.setAccessible(true);
                        info = (HoverEvent.ItemStackInfo) ctor.newInstance(stack);
                    } catch (NoSuchMethodException e3) {
                        try {
                            var ctor2 = HoverEvent.ItemStackInfo.class.getDeclaredConstructor(Holder.class, int.class, CompoundTag.class);
                            ctor2.setAccessible(true);
                            Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
                            info = (HoverEvent.ItemStackInfo) ctor2.newInstance(holder, stack.getCount(), null);
                        } catch (NoSuchMethodException e4) {
                        }
                    }
                }
            }
            if (info != null) {
                final HoverEvent.ItemStackInfo infoFinal = info;
                shown = shown.copy().withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, infoFinal)));
            }
        } catch (Throwable ignored) {
        }

        int count = stack.getCount();
        net.minecraft.network.chat.MutableComponent baseMsg = (count > 1)
                ? Component.translatable("message.itempeek.shows_item_count", player.getName(), shown, count)
                : Component.translatable("message.itempeek.shows_item", player.getName(), shown);
        Component msg = baseMsg.withStyle(ChatFormatting.GRAY);

        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            target.sendSystemMessage(msg);
        }
    }

    public static void sendShowItemToServer(int slotIndex) {
        CHANNEL.sendToServer(new ItemPeekMessage(slotIndex));
    }
}
