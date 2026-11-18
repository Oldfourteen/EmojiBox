package net.fabricmc.fabric.api.client.screen.v1;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

/** 编译期桩，不打包进 mod。 */
public final class Screens {
    private Screens() {
    }

    public static List<AbstractWidget> getWidgets(Screen screen) {
        return null;
    }
}
