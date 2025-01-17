package mcjty.lib;

import mcjty.lib.client.DelayedRenderer;
import mcjty.lib.gui.IKeyReceiver;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.widgets.Widget;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {

    public ClientEventHandler(){
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseDragged(ScreenEvent.MouseDragEvent.Pre event) {
        if (event.getScreen() instanceof IKeyReceiver container) {
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        manager.mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton());
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseScolled(ScreenEvent.MouseScrollEvent.Pre event) {
        if (event.getScreen() instanceof IKeyReceiver container) {
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        if (container.mouseScrolledFromEvent(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseClicked(ScreenEvent.MouseClickedEvent.Pre event) {
        if (event.getScreen() instanceof IKeyReceiver container) {
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        if (container.mouseClickedFromEvent(event.getMouseX(), event.getMouseY(), event.getButton())) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseReleased(ScreenEvent.MouseReleasedEvent.Pre event) {
        if (event.getScreen() instanceof IKeyReceiver container) {
            if (container.getWindow() != null) {
                WindowManager manager = container.getWindow().getWindowManager();
                if (manager != null) {
                    if (manager.getModalWindows().findFirst().isPresent()) {
                        // There is a modal window. Eat this event and send it directly to the window
                        if (container.mouseReleasedFromEvent(event.getMouseX(), event.getMouseY(), event.getButton())) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiInput(ScreenEvent.KeyboardCharTypedEvent event) {
        if (event.getScreen() instanceof IKeyReceiver container) {
            if (container.getWindow() != null) {
                Widget<?> focus;
                if (container.getWindow().getWindowManager() == null) {
                    focus = container.getWindow().getTextFocus();
                } else {
                    focus = container.getWindow().getWindowManager().getTextFocus();
                }
                if (focus != null) {
                    event.setCanceled(true);
                    container.charTypedFromEvent(event.getCodePoint());
                    // @todo 1.14 check
//                int c0 = event.getKeyCode();
//                if (Keyboard.getEventKey() == 0 && c0 >= 32 || Keyboard.getEventKeyState()) {
//                    container.keyTypedFromEvent(c0, Keyboard.getEventKey());
//                    Minecraft.getInstance().dispatchKeypresses();
//                }
                }
            }
        }

    }

    @SubscribeEvent
    public void onKeyboardInput(ScreenEvent.KeyboardKeyPressedEvent event) {
        if (event.getScreen() instanceof IKeyReceiver container) {
            if (container.getWindow() != null) {
                Widget<?> focus;
                if (container.getWindow().getWindowManager() == null) {
                    focus = container.getWindow().getTextFocus();
                } else {
                    focus = container.getWindow().getWindowManager().getTextFocus();
                }
                if (focus != null) {
                    event.setCanceled(true);
                    container.keyTypedFromEvent(event.getKeyCode(), event.getScanCode());
                    // @todo 1.14 check
//                int c0 = event.getKeyCode();
//                if (Keyboard.getEventKey() == 0 && c0 >= 32 || Keyboard.getEventKeyState()) {
//                    container.keyTypedFromEvent(c0, Keyboard.getEventKey());
//                    Minecraft.getInstance().dispatchKeypresses();
//                }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGameRenderOverlay(RenderLevelLastEvent e) {
        DelayedRenderer.render(e.getPoseStack());
    }
}
