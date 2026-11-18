package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.gui.EmojiSelectionGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

public class EmojifulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EmojifulConfig.load();
        EmojiRegistry.load();

        // 通过 Fabric 界面事件在聊天输入框上方添加原生"表情"按钮，不依赖 Mixin。
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof ChatScreen) || !EmojifulConfig.showChatButton()) {
                return;
            }
            EditBox input = null;
            for (AbstractWidget widget : Screens.getWidgets(screen)) {
                if (widget instanceof EditBox box) {
                    input = box;
                    break;
                }
            }
            if (input == null) {
                return;
            }
            EmojiSelectionGui selector = new EmojiSelectionGui(screen, input, scaledWidth, scaledHeight);
            Screens.getWidgets(screen).add(Button.builder(
                            Component.translatable("emojiful.button.tooltip"), button -> selector.toggle())
                    .bounds(4, scaledHeight - 30, 50, 16)
                    .build());
            ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
                    selector.render(graphics, mouseX, mouseY));
            ScreenMouseEvents.allowMouseClick(screen).register((s, event) ->
                    event.button() != 0 || !selector.mouseClicked(event.x(), event.y()));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) ->
                    !selector.mouseScrolled(mouseX, mouseY, vertical));
            Constants.LOG.info("[EmojiBox] 表情按钮已通过 Fabric 事件添加到聊天界面");
        });

        // 发送聊天时把 :smile: 等短代码转换为表情字形字符（PUA 码位），
        // 由 mod 内置的默认字体扩展直接渲染，无需修改聊天组件。
        ClientSendMessageEvents.MODIFY_CHAT.register(EmojiText::toUnicode);

        Constants.LOG.info("EmojiBox 已就绪（本地表情，无需联网）");
    }
}
