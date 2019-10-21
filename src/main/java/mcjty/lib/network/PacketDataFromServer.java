package mcjty.lib.network;

import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This packet is used (typically by PacketRequestDataFromServer) to send back a data to the client.
 */
public class PacketDataFromServer {
    private BlockPos pos;
    private TypedMap result;
    private String command;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        NetworkTools.writeString(buf, command);

        buf.writeBoolean(result != null);
        if (result != null) {
            TypedMapTools.writeArguments(buf, result);
        }
    }

    public PacketDataFromServer(PacketBuffer buf) {
        pos = buf.readBlockPos();

        command = NetworkTools.readString(buf);

        boolean resultPresent = buf.readBoolean();
        if (resultPresent) {
            result = TypedMapTools.readArguments(buf);
        } else {
            result = null;
        }
    }

    public PacketDataFromServer(BlockPos pos, String command, TypedMap result) {
        this.pos = pos;
        this.command = command;
        this.result = result;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveDataFromServer(command, result)) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
