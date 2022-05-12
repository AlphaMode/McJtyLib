package mcjty.lib.network;

import mcjty.lib.debugtools.DumpBlockNBT;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.ServerOpList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.level.Level;

/**
 * Debug packet to dump block info
 */
public class PacketDumpBlockInfo implements C2SPacket {

    private final ResourceKey<Level> dimid;
    private final BlockPos pos;
    private final boolean verbose;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimid.location());
        buf.writeBlockPos(pos);
        buf.writeBoolean(verbose);
    }

    public PacketDumpBlockInfo(FriendlyByteBuf buf) {
        dimid = LevelTools.getId(buf.readResourceLocation());
        pos = buf.readBlockPos();
        verbose = buf.readBoolean();
    }

    public PacketDumpBlockInfo(Level world, BlockPos pos, boolean verbose) {
        this.dimid = world.dimension();
        this.pos = pos;
        this.verbose = verbose;
    }

    @Override
    public void handle(MinecraftServer mcServer, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        mcServer.execute(() -> {
            MinecraftServer server = player.getCommandSenderWorld().getServer();
            ServerOpList oppedPlayers = server.getPlayerList().getOps();
            ServerOpListEntry entry = oppedPlayers.get(player.getGameProfile());
            int perm = entry == null ? server.getOperatorUserPermissionLevel() : entry.getLevel();
            if (perm >= 1) {
                Level world = LevelTools.getLevel(player.level, dimid);
                String output = DumpBlockNBT.dumpBlockNBT(world, pos, verbose);
                Logging.getLogger().log(org.apache.logging.log4j.Level.INFO, "### Server side ###");
                Logging.getLogger().log(org.apache.logging.log4j.Level.INFO, output);
            }
        });
    }
}
