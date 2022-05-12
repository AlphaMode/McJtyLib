package mcjty.lib.syncpositional;

import mcjty.lib.McJtyLib;
import mcjty.lib.varia.LevelTools;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * This packet is used to sync positional data from server to all affected clients
 */
public class PacketSendPositionalDataToClients implements S2CPacket {

    private final GlobalPos pos;
    private final IPositionalData data;

    public PacketSendPositionalDataToClients(GlobalPos pos, IPositionalData data) {
        this.pos = pos;
        this.data = data;
    }

    public PacketSendPositionalDataToClients(FriendlyByteBuf buf) {
        ResourceKey<Level> dimension = LevelTools.getId(buf.readResourceLocation());
        pos = GlobalPos.of(dimension, buf.readBlockPos());
        ResourceLocation id = buf.readResourceLocation();
        data = McJtyLib.SYNCER.create(id, buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(pos.dimension().location());
        buf.writeBlockPos(pos.pos());
        buf.writeResourceLocation(data.getId());
        data.toBytes(buf);
    }

    public void handle(Minecraft client, ClientPacketListener handler, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            McJtyLib.SYNCER.handle(pos, data);
        });
    }
}
