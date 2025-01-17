package mcjty.lib.network;

import mcjty.lib.McJtyLib;
import mcjty.lib.blockcommands.CommandInfo;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Packet to send back the list to the client. This requires
 * that the command is registered to McJtyLib.registerListCommandInfo
 */
public class PacketSendResultToClient implements S2CPacket {

    private final BlockPos pos;
    private final List list;
    private final String command;

    public PacketSendResultToClient(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        command = buf.readUtf(32767);
        CommandInfo<?> info = McJtyLib.getCommandInfo(command);
        if (info == null) {
            throw new IllegalStateException("Command '" + command + "' is not registered!");
        }
        Function<FriendlyByteBuf, ?> deserializer = info.deserializer();
        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                list.add(deserializer.apply(buf));
            }
        } else {
            list = null;
        }
    }

    public PacketSendResultToClient(BlockPos pos, String command, List list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>(list);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(command);
        CommandInfo<?> info = McJtyLib.getCommandInfo(command);
        if (info == null) {
            throw new IllegalStateException("Command '" + command + "' is not registered!");
        }
        BiConsumer<FriendlyByteBuf, Object> serializer = (BiConsumer<FriendlyByteBuf, Object>) info.serializer();
        if (serializer == null) {
            throw new IllegalStateException("Command '" + command + "' is not registered!");
        }
        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (Object item : list) {
                serializer.accept(buf, item);
            }
        }
    }

    public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            BlockEntity te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            if (te instanceof GenericTileEntity generic) {
                generic.handleListFromServer(command, SafeClientTools.getClientPlayer(), TypedMap.EMPTY, list);
            } else {
                Logging.logError("Can't handle command '" + command + "'!");
            }
        });
    }

}
