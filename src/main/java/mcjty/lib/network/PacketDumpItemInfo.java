package mcjty.lib.network;

import mcjty.lib.debugtools.DumpItemNBT;
import mcjty.lib.varia.Logging;
import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;

/**
 * Debug packet to dump item info
 */
public class PacketDumpItemInfo implements C2SPacket {

    private final boolean verbose;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(verbose);
    }

    public PacketDumpItemInfo(FriendlyByteBuf buf) {
        verbose = buf.readBoolean();
    }

    public PacketDumpItemInfo(boolean verbose) {
        this.verbose = verbose;
    }

    public void handle(MinecraftServer mcServer, ServerPlayer player, ServerGamePacketListenerImpl handler, SimpleChannel.ResponseTarget responseTarget) {
        mcServer.execute(() -> {
            MinecraftServer server = player.getCommandSenderWorld().getServer();
            ServerOpList oppedPlayers = server.getPlayerList().getOps();
            ServerOpListEntry entry = oppedPlayers.get(player.getGameProfile());
            int perm = entry == null ? server.getOperatorUserPermissionLevel() : entry.getLevel();
            if (perm >= 1) {
                ItemStack item = player.getMainHandItem();
                if (!item.isEmpty()) {
                    String output = DumpItemNBT.dumpItemNBT(item, verbose);
                    Logging.getLogger().log(Level.INFO, "### Server side ###");
                    Logging.getLogger().log(Level.INFO, output);
                }
            }
        });
    }
}
