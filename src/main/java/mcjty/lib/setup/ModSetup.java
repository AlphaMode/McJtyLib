package mcjty.lib.setup;

import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.event.common.PlayerTickEvents;
import mcjty.lib.McJtyLib;
import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.information.CapabilityPowerInformation;
import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.api.module.CapabilityModuleSupport;
import mcjty.lib.multipart.MultipartBlock;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.preferences.PreferencesDispatcher;
import mcjty.lib.preferences.PreferencesProperties;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import static mcjty.lib.McJtyLib.MODID;

public class ModSetup extends DefaultModSetup {

    public static final ResourceLocation PREFERENCES_CAPABILITY_KEY = new ResourceLocation(MODID, "preferences");

    public static boolean patchouli = false;

    public static Capability<PreferencesProperties> PREFERENCES_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityContainerProvider.register(event);
        CapabilityInfusable.register(event);
        CapabilityPowerInformation.register(event);
        CapabilityModuleSupport.register(event);
        PreferencesProperties.register(event);
    }

    @Override
    public void init() {
        super.init();
        McJtyLib.networkHandler = new SimpleChannel(new ResourceLocation(MODID, MODID));
        PacketHandler.registerMessages(McJtyLib.networkHandler);
//        MinecraftForge.EVENT_BUS.register(new EventHandler());
        McJtyLib.tesla = FabricLoader.getInstance().isModLoaded("tesla");
        McJtyLib.cofhapiitem = FabricLoader.getInstance().isModLoaded("cofhapi|item");

        PlayerTickEvents.START.register(EventHandler::onPlayerTickEvent);
        BlockEvents.LEFT_CLICK_BLOCK.register(EventHandler::onPlayerInteract);
    }

    @Override
    protected void setupModCompat() {
        patchouli = FabricLoader.getInstance().isModLoaded("patchouli");
    }

    public static class EventHandler {

        @SubscribeEvent
        public static void onWorldTick(TickEvent.WorldTickEvent event) {
            if (event.phase == TickEvent.Phase.START && event.world.dimension() == Level.OVERWORLD) {
                McJtyLib.SYNCER.sendOutData(event.world.getServer());
            }
        }

        @SubscribeEvent
        public static void onChunkWatch(ChunkWatchEvent.Watch event) {
            McJtyLib.SYNCER.startWatching(event.getPlayer());
        }

        public static void onPlayerTickEvent(Player player) {
            if (!player.getCommandSenderWorld().isClientSide) {
                McJtyLib.getPreferencesProperties(event.player).ifPresent(handler -> handler.tick((ServerPlayer) event.player));
            }
        }

        @SubscribeEvent
        public static void onEntityConstructing(AttachCapabilitiesEvent<Entity> event){
            if (event.getObject() instanceof Player) {
                if (!event.getCapabilities().containsKey(PREFERENCES_CAPABILITY_KEY) && !event.getObject().getCapability(PREFERENCES_CAPABILITY).isPresent()) {
                    event.addCapability(PREFERENCES_CAPABILITY_KEY, new PreferencesDispatcher());
                } else {
                    throw new IllegalStateException(event.getObject().toString());
                }
            }
        }

        public static void onPlayerInteract(Player player, BlockPos pos, Direction face) {
            Level world = player.getLevel();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof MultipartBlock) {
                BlockEntity tileEntity = world.getBlockEntity(pos);
                if (tileEntity instanceof MultipartTE) {
                    if (!world.isClientSide) {

                        // @todo 1.14 until LeftClickBlock has 'hitVec' again we need to do this:
                        Vec3 start = player.getEyePosition(1.0f);
                        Vec3 vec31 = player.getViewVector(1.0f);
                        float dist = 20;
                        Vec3 end = start.add(vec31.x * dist, vec31.y * dist, vec31.z * dist);
                        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
                        HitResult result = player.getCommandSenderWorld().clip(context);
                        Vec3 hitVec = result.getLocation();

                        if (MultipartHelper.removePart((MultipartTE) tileEntity, state, player, hitVec/*@todo*/)) {
                            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
                event.setCanceled(true);
            }
        }

    }

}
