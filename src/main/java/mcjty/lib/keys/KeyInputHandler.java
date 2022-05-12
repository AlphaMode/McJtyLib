package mcjty.lib.keys;

import mcjty.lib.client.ClientManualHelper;

public class KeyInputHandler {

    public static void onKeyInput(int key, int scancode, int action, int mods) {
        if (KeyBindings.openManual.consumeClick()) {
            ClientManualHelper.openManualFromGui();
        }
    }
}
