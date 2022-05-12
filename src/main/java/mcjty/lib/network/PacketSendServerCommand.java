package mcjty.lib.network;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import javax.annotation.Nonnull;

/**
 * Send a packet from the client to the server in order to execute a server side command
 * registered with McJtyLib.registerCommand()
 */
public class PacketSendServerCommand implements C2SPacket {

    // Package visible for unit tests
    private final String modid;
    private final String command;
    private final TypedMap arguments;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(modid);
        buf.writeUtf(command);
        TypedMapTools.writeArguments(buf, arguments);
    }

    public PacketSendServerCommand(FriendlyByteBuf buf) {
        modid = buf.readUtf(32767);
        command = buf.readUtf(32767);
        arguments = TypedMapTools.readArguments(buf);
    }

    public PacketSendServerCommand(String modid, String command, @Nonnull TypedMap arguments) {
        this.modid = modid;
        this.command = command;
        this.arguments = arguments;
    }

    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        server.execute(() -> {
            boolean result = McJtyLib.handleCommand(modid, command, player, arguments);
            if (!result) {
                Logging.logError("Error handling command '" + command + "' for mod '" + modid + "'!");
            }
        });
    }
}
