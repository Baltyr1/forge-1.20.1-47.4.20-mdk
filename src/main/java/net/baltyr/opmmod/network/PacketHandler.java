package net.baltyr.opmmod.network;

import net.baltyr.opmmod.OpmMod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new net.minecraft.resources.ResourceLocation(OpmMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.registerMessage(0, SyncClassPacket.class,
                SyncClassPacket::encode,
                SyncClassPacket::decode,
                SyncClassPacket::handle
        );
    }
}