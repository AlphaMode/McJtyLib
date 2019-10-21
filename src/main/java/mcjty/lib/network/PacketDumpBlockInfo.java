package mcjty.lib.network;

import mcjty.lib.debugtools.DumpBlockNBT;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.OpEntry;
import net.minecraft.server.management.OpList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

/**
 * Debug packet to dump block info
 */
public class PacketDumpBlockInfo {

    private DimensionType dimid;
    private BlockPos pos;
    private boolean verbose;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(dimid.getId());
        buf.writeBlockPos(pos);
        buf.writeBoolean(verbose);
    }

    public PacketDumpBlockInfo(PacketBuffer buf) {
        dimid = DimensionType.getById(buf.readInt());
        pos = buf.readBlockPos();
        verbose = buf.readBoolean();
    }

    public PacketDumpBlockInfo(World world, BlockPos pos, boolean verbose) {
        this.dimid = world.getDimension().getType();
        this.pos = pos;
        this.verbose = verbose;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            MinecraftServer server = player.getEntityWorld().getServer();
            OpList oppedPlayers = server.getPlayerList().getOppedPlayers();
            OpEntry entry = oppedPlayers.getEntry(player.getGameProfile());
            int perm = entry == null ? server.getOpPermissionLevel() : entry.getPermissionLevel();
            if (perm >= 1) {
                World world = WorldTools.getWorld(dimid);
                String output = DumpBlockNBT.dumpBlockNBT(world, pos, verbose);
                Logging.getLogger().log(Level.INFO, "### Server side ###");
                Logging.getLogger().log(Level.INFO, output);
            }
        });
        ctx.setPacketHandled(true);
    }
}
