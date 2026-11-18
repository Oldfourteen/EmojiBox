package com.hrznstudio.emojiful;

import java.util.List;

/**
 * 单个本地表情。名称同时作为短代码（:name:）使用，中文名与关键词用于搜索。
 */
public record EmojiEntry(String name, List<String> aliases, String zhName, List<String> keywords,
                         int codepoint, String category) {

    public String glyph() {
        return new String(Character.toChars(codepoint));
    }

    public String shortcode() {
        return ":" + name + ":";
    }

    public boolean matches(String query) {
        if (query.isEmpty()) {
            return true;
        }
        if (name.contains(query) || zhName.contains(query)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.contains(query)) {
                return true;
            }
        }
        for (String keyword : keywords) {
            if (keyword.contains(query)) {
                return true;
            }
        }
        return false;
    }
}
