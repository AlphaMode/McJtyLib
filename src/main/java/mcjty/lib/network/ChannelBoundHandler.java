package mcjty.lib.network;

import mcjty.lib.varia.TriConsumer;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class ChannelBoundHandler<T> implements BiConsumer<T, ChannelBoundHandler.NetworkContext> {
    private final SimpleChannel channel;
    private final TriConsumer<T, SimpleChannel, NetworkContext> innerHandler;

    public ChannelBoundHandler(SimpleChannel channel, TriConsumer<T, SimpleChannel, NetworkContext> innerHandler) {
        this.channel = channel;
        this.innerHandler = innerHandler;
    }

    @Override
    public void accept(T message, NetworkContext ctx) {
        innerHandler.accept(message, channel, ctx);
    }

    record NetworkContext(Executor executor, Player player) {}

}