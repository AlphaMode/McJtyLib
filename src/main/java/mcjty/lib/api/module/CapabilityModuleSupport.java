package mcjty.lib.api.module;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CapabilityModuleSupport {

    public static ComponentKey<IModuleSupport> MODULE_CAPABILITY = ComponentRegistry.getOrCreate();

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IModuleSupport.class);
    }

}
