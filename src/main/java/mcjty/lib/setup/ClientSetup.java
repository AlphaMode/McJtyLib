package mcjty.lib.setup;

import mcjty.lib.ClientEventHandler;
import mcjty.lib.keys.KeyBindings;
import mcjty.lib.keys.KeyInputHandler;
import mcjty.lib.multipart.MultipartModelLoader;
import mcjty.lib.tooltips.ClientTooltipIcon;
import mcjty.lib.tooltips.TooltipRender;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MinecraftForgeClient.registerTooltipComponentFactory(ClientTooltipIcon.class, (a) -> a);
        ItemTooltipCallback.EVENT.register(TooltipRender::onMakeTooltip);
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        KeyBindings.init();
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());

        ItemBlockRenderTypes.setRenderLayer(Registration.MULTIPART_BLOCK, (RenderType) -> true);
//        Arrays.stream(new RenderType[]{
//                        CustomRenderTypes.TRANSLUCENT_LIGHTNING_NOLIGHTMAPS, CustomRenderTypes.TRANSLUCENT_ADD_NOLIGHTMAPS,
//                        CustomRenderTypes.TRANSLUCENT_ADD, CustomRenderTypes.OVERLAY_LINES, CustomRenderTypes.QUADS_NOTEXTURE})
//                .forEach(type -> {
//                    Minecraft.getInstance().renderBuffers().fixedBuffers.put(type, new BufferBuilder(type.bufferSize()));
//                });

        MultipartModelLoader.register();
    }

}
