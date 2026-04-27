package net.baltyr.opmmod.network;

import net.baltyr.opmmod.client.abilities.ServerAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseAbilityPacket {

    private final int slot;

    public UseAbilityPacket(int slot) {
        this.slot = slot;
    }

    public static void encode(UseAbilityPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.slot);
    }

    public static UseAbilityPacket decode(FriendlyByteBuf buf) {
        return new UseAbilityPacket(buf.readInt());
    }

    public static void handle(UseAbilityPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerAbilityHandler.activate(packet.slot, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}