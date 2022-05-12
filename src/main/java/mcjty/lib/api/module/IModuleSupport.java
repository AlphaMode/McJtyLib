package mcjty.lib.api.module;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Implement this capability based interface in your TE if you want to support
 * automatic insertion of modules
 */
public interface IModuleSupport extends Component {

    boolean isModule(ItemStack item);

    int getFirstSlot();

    int getLastSlot();
}
