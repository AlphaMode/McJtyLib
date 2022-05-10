package mcjty.lib.network;

import mcjty.lib.compat.patchouli.PatchouliCompatibility;
import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Open the manual
 */
public class PacketOpenManual implements C2SPacket {

    private final ResourceLocation manual;
    private final ResourceLocation entry;
    private final int page;

    public PacketOpenManual(FriendlyByteBuf buf) {
        manual = buf.readResourceLocation();
        entry = buf.readResourceLocation();
        page = buf.readInt();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(manual);
        buf.writeResourceLocation(entry);
        buf.writeInt(page);
    }

    public PacketOpenManual(ResourceLocation manual, ResourceLocation entry, int page) {
        this.manual = manual;
        this.entry = entry;
        this.page = page;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, SimpleChannel.ResponseTarget responseTarget) {
        server.execute(() -> handle(this, player));
    }

    private static void handle(PacketOpenManual message, ServerPlayer playerEntity) {
        PatchouliCompatibility.openBookEntry(playerEntity, message.manual, message.entry, message.page);
    }
}
