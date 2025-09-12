package ru.multivarka.itempeek;

import net.minecraft.network.FriendlyByteBuf;

public class ItemPeekMessage {
    final int slotIndex;

    public ItemPeekMessage(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public static void encode(ItemPeekMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slotIndex);
    }

    public static ItemPeekMessage decode(FriendlyByteBuf buf) {
        return new ItemPeekMessage(buf.readVarInt());
    }
}

