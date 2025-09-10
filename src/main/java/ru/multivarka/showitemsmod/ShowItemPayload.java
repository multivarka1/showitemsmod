package ru.multivarka.showitemsmod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShowItemPayload(int slotIndex) implements CustomPacketPayload {
    public static final Type<ShowItemPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(showitemsmod.MODID, "show_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowItemPayload> CODEC =
            new StreamCodec<RegistryFriendlyByteBuf, ShowItemPayload>() {
                @Override
                public ShowItemPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ShowItemPayload(buf.readVarInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ShowItemPayload value) {
                    buf.writeVarInt(value.slotIndex());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
