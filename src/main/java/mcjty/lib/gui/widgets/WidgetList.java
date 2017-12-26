package mcjty.lib.gui.widgets;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.gui.RenderHelper;
import mcjty.lib.gui.Scrollable;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.SelectionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class WidgetList extends AbstractContainerWidget<WidgetList> implements Scrollable {
    private int rowheight = 16;
    private int first = 0;
    private int selected = -1;
    private long prevTime = -1;
    private boolean propagateEventsToChildren = false;
    private List<SelectionEvent> selectionEvents = null;
    private Set<Integer> hilightedRows = new HashSet<>();
    private boolean noselection = false;
    private boolean invisibleselection = false;
    private boolean drawHorizontalLines = true;
    private int leftMargin = 2;
    private int topMargin = 1;

    public WidgetList(Minecraft mc, Gui gui) {
        super(mc, gui);
        setFilledRectThickness(-1);
        setFilledBackground(StyleConfig.colorListBackground);
    }

    public int getRowheight() {
        return rowheight;
    }

    // Setting rowheight to -1 will use variable height depending on the desired height of every row
    public WidgetList setRowheight(int rowheight) {
        this.rowheight = rowheight;
        return this;
    }

    public boolean isDrawHorizontalLines() {
        return drawHorizontalLines;
    }

    public WidgetList setDrawHorizontalLines(boolean drawHorizontalLines) {
        this.drawHorizontalLines = drawHorizontalLines;
        return this;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public WidgetList setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
        return this;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public WidgetList setTopMargin(int topMargin) {
        this.topMargin = topMargin;
        return this;
    }

    public int getSelected() {
        return selected;
    }

    public WidgetList setSelected(int selected) {
        this.selected = selected;
        return this;
    }

    public boolean isPropagateEventsToChildren() {
        return propagateEventsToChildren;
    }

    public boolean isInvisibleSelection() {
        return invisibleselection;
    }

    public WidgetList setInvisibleSelection(boolean invisibleselection) {
        this.invisibleselection = invisibleselection;
        return this;
    }

    public WidgetList setNoSelectionMode(boolean m) {
        this.noselection = m;
        return this;
    }

    public boolean isNoSelection() {
        return noselection;
    }

    public WidgetList setPropagateEventsToChildren(boolean propagateEventsToChildren) {
        this.propagateEventsToChildren = propagateEventsToChildren;
        return this;
    }

    public void addHilightedRow(int index) {
        hilightedRows.add(index);
    }

    public void clearHilightedRows() {
        hilightedRows.clear();
    }

    @Override
    public Widget getWidgetAtPosition(int x, int y) {
        x -= bounds.x;
        y -= bounds.y;

        doLayout();
        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Widget child = children.get(i);
            if (child.in(x, y) && child.isVisible()) {
                return child.getWidgetAtPosition(x, y);
            }
        }

        return this;
    }

    private void doLayout() {
        int top = 0;
        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Widget child = children.get(i);
            int rh = rowheight == -1 ? child.getDesiredHeight() : rowheight;
            child.setBounds(new Rectangle(0 /*@@@ margin?*/, top, bounds.width, rh));
            top += rh;
        }
    }

    @Override
    public void draw(Window window, int x, int y) {
        if (!visible) {
            return;
        }

        // Use the function above to force reset of the slider in case the contents changed so that it doesn't look weird.
        mouseWheel(0, x, y);

        super.draw(window, x, y);
        int xx = x + bounds.x + leftMargin;
        int yy = y + bounds.y + topMargin;
        int top = 0;        // Margin@@@?
//        drawBox(xx, yy, 0xffff0000);

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Widget child = children.get(i);
            int rh = rowheight == -1 ? child.getDesiredHeight() : rowheight;
            child.setBounds(new Rectangle(0 /*@@@ margin?*/, top, bounds.width, rh));
            boolean hilighted = hilightedRows.contains(i);
            if ((top + rh-1 < bounds.height-3) && drawHorizontalLines) {
                RenderHelper.drawHorizontalLine(xx + 2, yy + top + rh - 1, xx + bounds.width - 7, StyleConfig.colorListSeparatorLine);
            }
            if (!invisibleselection) {
                if (i == selected && hilighted) {
                    RenderHelper.drawHorizontalGradientRect(xx, yy + top + 1, xx + bounds.width - 5, yy + top + rh - 2, StyleConfig.colorListSelectedHighlightedGradient1, StyleConfig.colorListSelectedHighlightedGradient2);
                } else if (i == selected) {
                    RenderHelper.drawHorizontalGradientRect(xx, yy + top + 1, xx + bounds.width - 5, yy + top + rh - 2, StyleConfig.colorListSelectedGradient1, StyleConfig.colorListSelectedGradient2);        // 0xff515151
                } else if (hilighted) {
                    RenderHelper.drawHorizontalGradientRect(xx, yy + top + 1, xx + bounds.width - 5, yy + top + rh - 2, StyleConfig.colorListHighlightedGradient1, StyleConfig.colorListHighlightedGradient2);
                }
            }
            if (isEnabledAndVisible()) {
                child.draw(window, xx, yy);
            } else {
                boolean en = child.isEnabled();
                child.setEnabled(false);
                child.draw(window, xx, yy);
                child.setEnabled(en);
            }
            top += rh;
        }
    }

    @Override
    public void drawPhase2(Window window, int x, int y) {
        if (!visible) {
            return;
        }

        super.drawPhase2(window, x, y);
        int xx = x + bounds.x + leftMargin;
        int yy = y + bounds.y + topMargin;
        int top = 0;        // Margin@@@?
//        drawBox(xx, yy, 0xffff0000);

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            Widget child = children.get(i);
            int rh = rowheight == -1 ? child.getDesiredHeight() : rowheight;
            if (isEnabledAndVisible()) {
                child.drawPhase2(window, xx, yy);
            }
            top += rh;
        }}

    @Override
    public void mouseMove(int x, int y) {
        if (!isEnabledAndVisible()) {
            return;
        }

        if (noselection) {
            return;
        }

        int newSelected = -1;
        int top = bounds.y;        // Margin@@@?

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            int rh = rowheight == -1 ? children.get(i).getDesiredHeight() : rowheight;
            Rectangle r = new Rectangle(bounds.x, top, bounds.width, rh);
            if (r.contains(x, y)) {
                newSelected = i;
                break;
            }
            top += rh;
        }
        if (propagateEventsToChildren && newSelected != -1) {
            Widget child = getSelectedWidgetSafe(newSelected);
            int xx = x-bounds.x;
            int yy = y-bounds.y;
            if (child != null && child.in(xx, yy) && child.isVisible()) {
                child.mouseMove(xx, yy);
            }
        }
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        if (!isEnabledAndVisible()) {
            return null;
        }

        if (noselection) {
            return null;
        }

        int newSelected = -1;
        int top = bounds.y;        // Margin@@@?

        for (int i = first ; i < first+getCountSelected() && i < children.size(); i++) {
            int rh = rowheight == -1 ? children.get(i).getDesiredHeight() : rowheight;
            Rectangle r = new Rectangle(bounds.x, top, bounds.width, rh);
            if (r.contains(x, y)) {
                newSelected = i;
                break;
            }
            top += rh;
        }
        if (newSelected != selected) {
            selected = newSelected;
            fireSelectionEvents(selected);
        }

        if (propagateEventsToChildren && selected != -1) {
            Widget child = getSelectedWidgetSafe(selected);
            int xx = x-bounds.x;
            int yy = y-bounds.y;
            if (child != null && child.in(xx, yy) && child.isVisible()) {
                child.mouseClick(window, xx, yy, button);
            }
        }

        long t = System.currentTimeMillis();
        if (prevTime != -1 && (t - prevTime) < 250) {
            fireDoubleClickEvent(selected);
        }
        prevTime = t;

        return null;
    }

    @Override
    public void mouseRelease(int x, int y, int button) {
        if (!isEnabledAndVisible()) {
            return;
        }
        if (noselection) {
            return;
        }

        if (propagateEventsToChildren && selected != -1) {
            Widget child = getSelectedWidgetSafe(selected);
            int xx = x-bounds.x;
            int yy = y-bounds.y;
            if (child != null && child.in(xx, yy) && child.isVisible()) {
                child.mouseRelease(xx, yy, button);
            }
        }

        super.mouseRelease(x, y, button);
    }

    private Widget getSelectedWidgetSafe(int sel) {
        if (sel < children.size()) {
            return children.get(sel);
        } else {
            return null;
        }
    }

    @Override
    public boolean mouseWheel(int amount, int x, int y) {
        int divider = getMaximum() - getCountSelected();
        if (divider <= 0) {
            first = 0;
        } else {
            if (amount > 0) {
                first -= 3;
            } else if (amount < 0) {
                first += 3;
            }
        }
        if (first > divider) {
            first = divider;
        }
        if (first < 0) {
            first = 0;
        }

        return true;
    }

    @Override
    public int getMaximum() {
        return children.size();
    }

    @Override
    public int getCountSelected() {
        if (bounds == null) {
            return 0;
        }
        if (rowheight != -1) {
            return bounds.height / rowheight;
        } else {
            int totalh = 0;
            int cnt = 0;
            for (int i = first; i < children.size(); i++) {
                int rh = children.get(i).getDesiredHeight();
                if (totalh + rh > bounds.height) {
                    break;
                }
                totalh += rh;
                cnt++;
            }
            return cnt;
        }
    }

    @Override
    public int getFirstSelected() {
        return first;
    }

    @Override
    public void setFirstSelected(int first) {
        this.first = first;
    }

    public WidgetList addSelectionEvent(SelectionEvent event) {
        if (selectionEvents == null) {
            selectionEvents = new ArrayList<> ();
        }
        selectionEvents.add(event);
        return this;
    }

    public void removeSelectionEvent(SelectionEvent event) {
        if (selectionEvents != null) {
            selectionEvents.remove(event);
        }
    }

    private void fireSelectionEvents(int index) {
        if (selectionEvents != null) {
            for (SelectionEvent event : selectionEvents) {
                event.select(this, index);
            }
        }
    }

    private void fireDoubleClickEvent(int index) {
        if (selectionEvents != null) {
            for (SelectionEvent event : selectionEvents) {
                event.doubleClick(this, index);
            }
        }
    }

    @Override
    public WidgetList removeChild(Widget child) {
        int index = children.indexOf(child);
        if (index != -1) {
            Set<Integer> newHighlights = new HashSet<>();
            for (Integer i : hilightedRows) {
                if (i < index) {
                    newHighlights.add(i);
                } else if (i > index) {
                    newHighlights.add(i-1);
                }
            }
            hilightedRows = newHighlights;
        }
        return super.removeChild(child);
    }

    @Override
    public void removeChildren() {
        super.removeChildren();
        hilightedRows.clear();
    }
}
