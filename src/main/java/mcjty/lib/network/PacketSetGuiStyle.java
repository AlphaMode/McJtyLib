package mcjty.lib.network;

import mcjty.lib.McJtyLib;
import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Change the GUI style.
 */
public class PacketSetGuiStyle implements C2SPacket {

    // Package visible for unit tests
    private final String style;

    public PacketSetGuiStyle(FriendlyByteBuf buf) {
        style = buf.readUtf(32767);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(style);
    }

    public PacketSetGuiStyle(String style) {
        this.style = style;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, SimpleChannel.ResponseTarget responseTarget) {
        server.execute(() -> handle(this, player));
    }

    private static void handle(PacketSetGuiStyle message, ServerPlayer playerEntity) {
        McJtyLib.getPreferencesProperties(playerEntity).ifPresent(p -> p.setStyle(message.style));
    }
}
