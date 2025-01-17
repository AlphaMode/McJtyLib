package mcjty.lib;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.blockcommands.CommandInfo;
import mcjty.lib.network.IServerCommand;
import mcjty.lib.preferences.PreferencesProperties;
import mcjty.lib.setup.ModSetup;
import mcjty.lib.syncpositional.PositionalDataSyncer;
import mcjty.lib.typed.TypedMap;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class McJtyLib implements ModInitializer {

    public static final String MODID = "mcjtylib";

    public static final ModSetup setup = new ModSetup();

    public static McJtyLib instance;

    public static SimpleChannel networkHandler;
    public static boolean tesla;
    public static boolean cofhapiitem;

    private static final Map<Pair<String, String>, IServerCommand> serverCommands = new HashMap<>();
    private static final Map<Pair<String, String>, IServerCommand> clientCommands = new HashMap<>();
    private static final Map<String, CommandInfo> commandInfos = new HashMap<>();

    public static final PositionalDataSyncer SYNCER = new PositionalDataSyncer();

    @Override
    public void onInitialize() {
        instance = this;
        // Register the setup method for modloading
        setup.init();

        ModLoadingContext.registerConfig(MODID, ModConfig.Type.CLIENT, GeneralConfig.CLIENT_CONFIG);
        ModLoadingContext.registerConfig(MODID, ModConfig.Type.SERVER, GeneralConfig.SERVER_CONFIG);

        ModConfigEvent.LOADING.register(GeneralConfig::onLoad);
        ModConfigEvent.RELOADING.register(GeneralConfig::onFileChange);
    }

    /**
     * This is automatically called by annotated ListCommands (@ServerCommand) if they have
     * an associated type parameter
     */
    public static <T> void registerListCommandInfo(String command, Class<T> type, Function<FriendlyByteBuf, T> deserializer, BiConsumer<FriendlyByteBuf, T> serializer) {
        commandInfos.put(command, new CommandInfo<T>(type, deserializer, serializer));
    }

    public static CommandInfo getCommandInfo(String command) {
        return commandInfos.get(command);
    }

    /**
     * Used in combination with PacketSendServerCommand for a more global command
     */
    public static void registerCommand(String modid, String id, IServerCommand command) {
        serverCommands.put(Pair.of(modid, id), command);
    }

    public static void registerClientCommand(String modid, String id, IServerCommand command) {
        clientCommands.put(Pair.of(modid, id), command);
    }

    public static boolean handleCommand(String modid, String id, Player player, TypedMap arguments) {
        IServerCommand command = serverCommands.get(Pair.of(modid, id));
        if (command == null) {
            return false;
        }
        return command.execute(player, arguments);
    }

    public static boolean handleClientCommand(String modid, String id, Player player, TypedMap arguments) {
        IServerCommand command = clientCommands.get(Pair.of(modid, id));
        if (command == null) {
            return false;
        }
        return command.execute(player, arguments);
    }

    public static LazyOptional<PreferencesProperties> getPreferencesProperties(Player player) {
        return player.getCapability(ModSetup.PREFERENCES_CAPABILITY);
    }
}
