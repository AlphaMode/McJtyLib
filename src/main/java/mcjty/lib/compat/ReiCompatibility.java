package mcjty.lib.compat;

import mcjty.lib.gui.GenericGuiContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReiCompatibility implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(GenericGuiContainer.class, new Handler<>());
    }

    static class Handler<T extends AbstractContainerMenu> implements ExclusionZonesProvider<GenericGuiContainer<?,T>> {
        @Override
        public Collection<Rectangle> provide(GenericGuiContainer<?, T> containerScreen) {
            List<Rect2i> oldBounds = containerScreen.getExtraWindowBounds();
            List<Rectangle> newBounds = new ArrayList<>();
            oldBounds.forEach(rect2i -> newBounds.add(new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight())));
            return newBounds;
        }
    }
}
