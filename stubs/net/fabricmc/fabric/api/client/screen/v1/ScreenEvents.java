package net.fabricmc.fabric.api.client.screen.v1;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/** 编译期桩，签名与 fabric-screen-api-v1 5.2.1 一致，不打包进 mod。 */
public final class ScreenEvents {
    public static final Event<ScreenEvents.AfterInit> AFTER_INIT = null;

    private ScreenEvents() {
    }

    public static Event<ScreenEvents.AfterExtract> afterExtract(Screen screen) {
        return null;
    }

    public interface AfterInit {
        void afterInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight);
    }

    public interface AfterExtract {
        void afterExtract(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta);
    }
}
