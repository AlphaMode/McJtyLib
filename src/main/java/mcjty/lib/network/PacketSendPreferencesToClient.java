package mcjty.lib.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import mcjty.lib.gui.GuiStyle;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendPreferencesToClient implements IMessage {
    private int buffX;
    private int buffY;
    private GuiStyle style;

    @Override
    public void fromBytes(ByteBuf buf) {
        buffX = buf.readInt();
        buffY = buf.readInt();
        style = GuiStyle.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(buffX);
        buf.writeInt(buffY);
        buf.writeInt(style.ordinal());
    }

    public PacketSendPreferencesToClient() {
    }

    public PacketSendPreferencesToClient(int buffX, int buffY, GuiStyle style) {
        this.buffX = buffX;
        this.buffY = buffY;
        this.style = style;
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

    public static class Handler implements IMessageHandler<PacketSendPreferencesToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketSendPreferencesToClient message, MessageContext ctx) {
            Minecraft.getInstance().addScheduledTask(() -> SendPreferencesToClientHelper.setPreferences(message));
            return null;
        }

    }

}
