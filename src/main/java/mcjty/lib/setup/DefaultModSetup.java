package mcjty.lib.setup;

import io.github.fabricators_of_create.porting_lib.util.LazyItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class DefaultModSetup {

    private Logger logger;
    protected CreativeModeTab creativeTab;

    public void init() {
        logger = LogManager.getLogger();

        setupModCompat();
    }

    protected abstract void setupModCompat();

    protected void createTab(String name, Supplier<ItemStack> stack) {
        creativeTab = new LazyItemGroup(name) {
            @Override
            @Nonnull
            public ItemStack makeIcon() {
                return stack.get();
            }
        };
    }

    public Logger getLogger() {
        return logger;
    }

    public CreativeModeTab getTab() {
        return creativeTab;
    }
}
