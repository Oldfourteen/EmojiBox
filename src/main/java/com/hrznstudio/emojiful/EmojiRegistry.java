package com.hrznstudio.emojiful;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 从 mod 内置资源读取表情清单，完全不联网。
 */
public final class EmojiRegistry {

    public static final Identifier EMOJI_FONT = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "emoji");
    private static final String INDEX = "/assets/emojiful/emoji/index.json";

    private static final List<EmojiEntry> ALL = new ArrayList<>();
    private static final Map<String, EmojiEntry> BY_NAME = new LinkedHashMap<>();
    private static final Map<String, Category> CATEGORIES = new LinkedHashMap<>();
    private static boolean loaded;

    private EmojiRegistry() {
    }

    public record Category(String id, String zhName, String enName) {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        try (var stream = EmojiRegistry.class.getResourceAsStream(INDEX)) {
            if (stream == null) {
                Constants.LOG.error("找不到内置表情清单 {}", INDEX);
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement element : root.getAsJsonArray("categories")) {
                JsonObject category = element.getAsJsonObject();
                Category value = new Category(category.get("id").getAsString(),
                        category.get("zh").getAsString(), category.get("en").getAsString());
                CATEGORIES.put(value.id(), value);
            }
            for (JsonElement element : root.getAsJsonArray("emojis")) {
                JsonObject emoji = element.getAsJsonObject();
                List<String> aliases = readStrings(emoji.getAsJsonArray("a"));
                List<String> keywords = readStrings(emoji.getAsJsonArray("kw"));
                EmojiEntry entry = new EmojiEntry(
                        emoji.get("n").getAsString(),
                        aliases,
                        emoji.get("zh").getAsString(),
                        keywords,
                        emoji.get("cp").getAsInt(),
                        emoji.get("cat").getAsString());
                ALL.add(entry);
                for (String alias : aliases) {
                    BY_NAME.putIfAbsent(alias.toLowerCase(Locale.ROOT), entry);
                }
                BY_NAME.putIfAbsent(entry.name().toLowerCase(Locale.ROOT), entry);
            }
            Constants.LOG.info("已加载 {} 个本地表情", ALL.size());
        } catch (Exception e) {
            Constants.LOG.error("加载内置表情失败", e);
        }
    }

    private static List<String> readStrings(JsonArray array) {
        if (array == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            result.add(element.getAsString());
        }
        return List.copyOf(result);
    }

    public static List<EmojiEntry> all() {
        load();
        return Collections.unmodifiableList(ALL);
    }

    public static List<Category> categories() {
        load();
        return List.copyOf(CATEGORIES.values());
    }

    public static Category category(String id) {
        load();
        return CATEGORIES.get(id);
    }

    /** 按短代码查找表情，name 不带冒号。 */
    public static EmojiEntry byName(String name) {
        load();
        return BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }
}
