package com.hrznstudio.emojiful;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地配置文件（config/emojiful.json），不依赖任何第三方配置库。
 */
public final class EmojifulConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = Path.of("config", "emojiful.json");

    private static boolean enabled = true;
    private static boolean showChatButton = true;

    private EmojifulConfig() {
    }

    public static void load() {
        try {
            if (Files.notExists(PATH)) {
                save();
                return;
            }
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("enabled")) {
                    enabled = json.get("enabled").getAsBoolean();
                }
                if (json.has("showChatButton")) {
                    showChatButton = json.get("showChatButton").getAsBoolean();
                }
            }
        } catch (Exception e) {
            Constants.LOG.warn("读取 emojiful 配置失败，使用默认配置", e);
        }
    }

    public static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", enabled);
        json.addProperty("showChatButton", showChatButton);
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            Constants.LOG.warn("保存 emojiful 配置失败", e);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean showChatButton() {
        return showChatButton;
    }
}
