package mcjty.lib.network;

import mcjty.lib.gui.BuffStyle;
import mcjty.lib.gui.GuiStyle;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class PacketSendPreferencesToClient implements S2CPacket {
    private final BuffStyle buffStyle;
    private final int buffX;
    private final int buffY;
    private final GuiStyle style;

    public PacketSendPreferencesToClient(FriendlyByteBuf buf) {
        buffStyle = BuffStyle.values()[buf.readInt()];
        buffX = buf.readInt();
        buffY = buf.readInt();
        style = GuiStyle.values()[buf.readInt()];
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(buffStyle.ordinal());
        buf.writeInt(buffX);
        buf.writeInt(buffY);
        buf.writeInt(style.ordinal());
    }

    public PacketSendPreferencesToClient(BuffStyle buffStyle, int buffX, int buffY, GuiStyle style) {
        this.buffStyle = buffStyle;
        this.buffX = buffX;
        this.buffY = buffY;
        this.style = style;
    }

    public BuffStyle getBuffStyle() {
        return buffStyle;
    }

    public int getBuffX() {
        return buffX;
    }

    public int getBuffY() {
        return buffY;
    }

    public GuiStyle getStyle() {
        return style;
    }

    @Override
    public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            SendPreferencesToClientHelper.setPreferences(this);
        });
    }

}
