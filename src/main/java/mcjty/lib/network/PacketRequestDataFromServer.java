package mcjty.lib.network;

import mcjty.lib.blockcommands.ICommand;
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
 * a tile entity on the server that has a ResultCommand annotated with @ServerCommand
 */
public class PacketRequestDataFromServer implements C2SPacket {
    protected BlockPos pos;
    private final ResourceKey<Level> type;
    protected String command;
    protected TypedMap params;
    private final boolean dummy;

    public PacketRequestDataFromServer(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        type = LevelTools.getId(buf.readResourceLocation());
        command = buf.readUtf(32767);
        params = TypedMapTools.readArguments(buf);
        dummy = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(type.location());
        buf.writeUtf(command);
        TypedMapTools.writeArguments(buf, params);
        buf.writeBoolean(dummy);
    }

    public PacketRequestDataFromServer(ResourceKey<Level> type, BlockPos pos, String command, TypedMap params, boolean dummy) {
        this.type = type;
        this.pos = pos;
        this.command = command;
        this.params = params;
        this.dummy = dummy;
    }

    public PacketRequestDataFromServer(ResourceKey<Level> type, BlockPos pos, ICommand command, TypedMap params, boolean dummy) {
        this.type = type;
        this.pos = pos;
        this.command = command.name();
        this.params = params;
        this.dummy = dummy;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        server.execute(() -> {
            Level world = LevelTools.getLevel(player.getCommandSenderWorld(), type);
            if (world.hasChunkAt(pos)) {
                if (world.getBlockEntity(pos) instanceof GenericTileEntity generic) {
                    TypedMap result = generic.executeServerCommandWR(command, player, params);
                    if (result != null) {
                        PacketDataFromServer msg = new PacketDataFromServer(dummy ? null : pos, command, result);
                        channel.sendToClient(msg, player);
                        return;
                    }
                }

                Logging.log("Command " + command + " was not handled!");
            }
        });
    }
}
