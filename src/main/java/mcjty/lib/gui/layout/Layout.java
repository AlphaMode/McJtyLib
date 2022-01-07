package mcjty.lib.gui.layout;

import mcjty.lib.gui.widgets.Widget;

import java.util.Collection;

public interface Layout {
    /**
     * Calculate the layout of the children in the container.
     */
    void doLayout(Collection<Widget<?>> children, int width, int height);
}
