package mcjty.lib.network;

import mcjty.lib.syncpositional.PacketSendPositionalDataToClients;
import mcjty.lib.typed.TypedMap;
import me.pepperbell.simplenetworking.SimpleChannel;

import javax.annotation.Nonnull;

public class PacketHandler {

    public static boolean connected = false;

    // Only use client-side!
    private static <MSG> boolean canBeSent(MSG message) {
        return connected;
    }

    // Only use client-side!
    public static void onDisconnect() {
        connected = false;
    }


    public static void registerMessages(SimpleChannel channel) {
        int startIndex = 0;
        channel.registerS2CPacket(PacketSendPreferencesToClient.class, startIndex++);
        channel.registerC2SPacket(PacketSetGuiStyle.class, startIndex++);
        channel.registerC2SPacket(PacketOpenManual.class, startIndex++);
        channel.registerS2CPacket(PacketContainerDataToClient.class, startIndex++);
        channel.registerS2CPacket(PacketSendPositionalDataToClients.class, startIndex++);
        channel.registerS2CPacket(PacketSendResultToClient.class, startIndex++);
    }

    public static void registerStandardMessages(int id, SimpleChannel channel) {

        // Server side
        channel.registerC2SPacket(PacketGetListFromServer.class, id++);
        channel.registerC2SPacket(PacketServerCommandTyped.class, id++);
        channel.registerC2SPacket(PacketSendServerCommand.class, id++);
        channel.registerC2SPacket(PacketDumpItemInfo.class, id++);
        channel.registerC2SPacket(PacketDumpBlockInfo.class, id++);

        // Client side
        channel.registerS2CPacket(PacketSendClientCommand.class, id++);
        channel.registerS2CPacket(PacketDataFromServer.class, id++);
        channel.registerS2CPacket(PacketFinalizeLogin.class, id++);
    }

    // From client side only: send server command
    public static void sendCommand(SimpleChannel network, String modid, String command, @Nonnull TypedMap arguments) {
        network.sendToServer(new PacketSendServerCommand(modid, command, arguments));
    }

}
