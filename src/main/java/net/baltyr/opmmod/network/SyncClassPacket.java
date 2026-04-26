package net.baltyr.opmmod.network;

import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncClassPacket {
    private final OpmClass playerClass;

    public SyncClassPacket(OpmClass playerClass) {
        this.playerClass = playerClass;
    }

    public static void encode(SyncClassPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.playerClass.name());
    }

    public static SyncClassPacket decode(FriendlyByteBuf buf) {
        return new SyncClassPacket(OpmClass.fromString(buf.readUtf()));
    }

    public static void handle(SyncClassPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Côté client : on met à jour la capability du joueur local
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(cap -> {
                    cap.setPlayerClass(packet.playerClass);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}