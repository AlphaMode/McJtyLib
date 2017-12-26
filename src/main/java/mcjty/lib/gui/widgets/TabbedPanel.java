package mcjty.lib.gui.widgets;

import mcjty.lib.gui.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TabbedPanel extends AbstractContainerWidget<Panel> {

    private Widget current = null;
    private Map<String,Widget> pages = new HashMap<>();

    public TabbedPanel(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public TabbedPanel addPage(String name, Widget child) {
        addChild(child);
        pages.put(name, child);
        return this;
    }

    public Widget getCurrent() {
        return current;
    }

    public String getCurrentName() {
        for (Map.Entry<String,Widget> me : pages.entrySet()) {
            if (current == me.getValue()) {
                return me.getKey();
            }
        }
        return null;
    }

    public TabbedPanel setCurrent(Widget current) {
        this.current = current;
        return this;
    }

    public TabbedPanel setCurrent(String name) {
        this.current = pages.get(name);
        return this;
    }

    @Override
    public Widget getWidgetAtPosition(int x, int y) {
        if (current == null) {
            return this;
        }

        setChildBounds();

        x -= bounds.x;
        y -= bounds.y;

        return current.getWidgetAtPosition(x, y);
    }

    @Override
    public void draw(Window window, int x, int y) {
        if (!visible) {
            return;
        }
        super.draw(window, x, y);
        int xx = x + bounds.x;
        int yy = y + bounds.y;
        drawBox(xx, yy, 0xffff0000);

        setChildBounds();

        if (current != null) {
            current.draw(window, xx, yy);
        }
    }

    private void setChildBounds() {
        if (isDirty()) {
            for (Widget child : children) {
                child.setBounds(new Rectangle(0, 0, getBounds().width, getBounds().height));
            }
            markClean();
        }
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        super.mouseClick(window, x, y, button);

        setChildBounds();

        x -= bounds.x;
        y -= bounds.y;

        if (current != null) {
            if (current.in(x, y) && current.isVisible()) {
                return current.mouseClick(window, x, y, button);
            }
        }

        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);

        setChildBounds();

        x -= bounds.x;
        y -= bounds.y;

        if (current != null) {
            current.mouseRelease(x, y, button);
        }
    }

    @Override
    public void mouseMove(int x, int y) {
        super.mouseMove(x, y);

        setChildBounds();

        x -= bounds.x;
        y -= bounds.y;

        if (current != null) {
            current.mouseMove(x, y);
        }
    }
}
