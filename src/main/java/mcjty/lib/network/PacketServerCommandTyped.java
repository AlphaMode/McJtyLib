package mcjty.lib.network;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
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
import net.minecraft.world.level.Level;

/**
 * This is a packet that can be used to send a command from the client side (typically the GUI) to
 * a tile entity on the server side that implements CommandHandler. This will call 'execute()' on
 * that command handler.
 */
public class PacketServerCommandTyped implements C2SPacket {

    private final BlockPos pos;
    private final ResourceKey<Level> dimensionId;
    private final String command;
    private final TypedMap params;

    public PacketServerCommandTyped(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        command = buf.readUtf(32767);
        params = TypedMapTools.readArguments(buf);
        if (buf.readBoolean()) {
            dimensionId = LevelTools.getId(buf.readResourceLocation());
        } else {
            dimensionId = null;
        }
    }

    public PacketServerCommandTyped(BlockPos pos, ResourceKey<Level> dimensionId, String command, TypedMap params) {
        this.pos = pos;
        this.command = command;
        this.params = params;
        this.dimensionId = dimensionId;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(command);
        TypedMapTools.writeArguments(buf, params);
        if (dimensionId != null) {
            buf.writeBoolean(true);
            buf.writeResourceLocation(dimensionId.location());
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer playerEntity, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        server.execute(() -> {
            Level world;
            if (dimensionId == null) {
                world = playerEntity.getCommandSenderWorld();
            } else {
                world = LevelTools.getLevel(playerEntity.level, dimensionId);
            }
            if (world == null) {
                return;
            }
            if (world.hasChunkAt(pos)) {
                if (world.getBlockEntity(pos) instanceof GenericTileEntity generic) {
                    if (generic.executeServerCommand(command, playerEntity, params)) {
                        return;
                    }
                }
                Logging.log("Command " + command + " was not handled!");
            }
        });
    }
}
