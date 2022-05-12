package mcjty.lib.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mcjty.lib.api.container.IContainerDataListener;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.SafeClientTools;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PacketContainerDataToClient implements S2CPacket {

    private final ResourceLocation id;
    private final FriendlyByteBuf buffer;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        int l = buffer.array().length;
        buf.writeInt(l);
        buf.writeBytes(buffer.array());
    }

    public PacketContainerDataToClient(FriendlyByteBuf buf) {
        id = buf.readResourceLocation();
        int l = buf.readInt();

        ByteBuf newbuf = Unpooled.buffer(l);
        byte[] bytes = new byte[l];
        buf.readBytes(bytes);
        newbuf.writeBytes(bytes);
        buffer = new FriendlyByteBuf(newbuf);
    }

    public PacketContainerDataToClient(ResourceLocation id, FriendlyByteBuf buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    @Override
    public void handle(Minecraft client, ClientPacketListener clientPacketListener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            AbstractContainerMenu container = SafeClientTools.getClientPlayer().containerMenu;
            if (container instanceof GenericContainer gc) {
                IContainerDataListener listener = gc.getListener(id);
                if (listener != null) {
                    listener.readBuf(buffer);
                }
            }
        });
    }


}
