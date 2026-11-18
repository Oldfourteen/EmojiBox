package net.fabricmc.fabric.api.client.message.v1;

import net.fabricmc.fabric.api.event.Event;

/** 编译期桩，签名与 fabric-message-api-v1 7.0.8 一致，不打包进 mod。 */
public final class ClientSendMessageEvents {
    public static final Event<ClientSendMessageEvents.ModifyChat> MODIFY_CHAT = null;

    private ClientSendMessageEvents() {
    }

    public interface ModifyChat {
        String modifySendChatMessage(String message);
    }
}
