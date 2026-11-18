package net.fabricmc.fabric.api.client.screen.v1;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

/** 编译期桩，签名与 fabric-screen-api-v1 5.2.1 一致，不打包进 mod。 */
public final class ScreenMouseEvents {
    private ScreenMouseEvents() {
    }

    public static Event<ScreenMouseEvents.AllowMouseClick> allowMouseClick(Screen screen) {
        return null;
    }

    public static Event<ScreenMouseEvents.AllowMouseScroll> allowMouseScroll(Screen screen) {
        return null;
    }

    public interface AllowMouseClick {
        boolean allowMouseClick(Screen screen, MouseButtonEvent event);
    }

    public interface AllowMouseScroll {
        boolean allowMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    }
}
