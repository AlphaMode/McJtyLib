package mcjty.lib.network;

import mcjty.lib.container.GenericContainer;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

/**
 * This packet is used (typically by PacketRequestDataFromServer) to send back a data to the client.
 */
@SuppressWarnings("ALL")
public class PacketDataFromServer implements S2CPacket {
    // Package visible for unittests
    BlockPos pos;
    TypedMap result;
    String command;

    @Override
    public void encode(FriendlyByteBuf buf) {
        if (pos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeUtf(command);

        buf.writeBoolean(result != null);
        if (result != null) {
            TypedMapTools.writeArguments(buf, result);
        }
    }

    public PacketDataFromServer(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        } else {
            pos = null;
        }
        command = buf.readUtf(32767);

        if (buf.readBoolean()) {
            result = TypedMapTools.readArguments(buf);
        } else {
            result = null;
        }
    }

    public PacketDataFromServer(@Nullable BlockPos pos, String command, TypedMap result) {
        this.pos = pos;
        this.command = command;
        this.result = result;
    }

    public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        client.execute(() -> {
            BlockEntity te;
            if (pos == null) {
                // We are working from a tablet. Find the tile entity through the open container
                GenericContainer container = getOpenContainer();
                if (container == null) {
                    Logging.log("Container is missing!");
                    return;
                }
                te = container.getTe();
            } else {
                te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            }

            if (te instanceof GenericTileEntity generic) {
                if (generic.executeClientCommand(command, SafeClientTools.getClientPlayer(), result)) {
                    return;
                }
            }

            Logging.log("Command " + command + " was not handled!");
        });
    }

    private static GenericContainer getOpenContainer() {
        AbstractContainerMenu container = SafeClientTools.getClientPlayer().containerMenu;
        if (container instanceof GenericContainer) {
            return (GenericContainer) container;
        } else {
            return null;
        }
    }
}
