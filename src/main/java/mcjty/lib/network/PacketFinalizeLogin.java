package mcjty.lib.network;

import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

/**
 * This is sent from the server to the client after the login has occured so that packets that implement
 * IClientServerDelayed can be sent
 */
public class PacketFinalizeLogin implements S2CPacket {

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public PacketFinalizeLogin(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        finalizeClientLogin();
    }

    private void finalizeClientLogin() {
        PacketHandler.connected = true;
    }

}