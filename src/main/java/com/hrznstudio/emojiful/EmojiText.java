package com.hrznstudio.emojiful;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表情字形工具：菜单绘制使用 emojiful:emoji 独立字体；
 * 聊天文本在发送时把 :name: 短代码替换为 PUA 字形字符，
 * 由 mod 内置的默认字体扩展（assets/minecraft/font/default.json）渲染为彩色表情。
 */
public final class EmojiText {

    private static final Pattern SHORTCODE = Pattern.compile(":([a-z0-9_+\\-]{1,24}):", Pattern.CASE_INSENSITIVE);
    private static final FontDescription.Resource EMOJI_FONT = new FontDescription.Resource(EmojiRegistry.EMOJI_FONT);

    private EmojiText() {
    }

    /** 单个表情字形组件，供界面直接绘制。 */
    public static Component glyphComponent(EmojiEntry entry) {
        return Component.literal(entry.glyph()).withStyle(Style.EMPTY.withFont(EMOJI_FONT));
    }

    /** 把文本中的 :name: 短代码替换为表情字形字符；未匹配的部分原样保留。 */
    public static String toUnicode(String text) {
        if (text == null || !EmojifulConfig.isEnabled()) {
            return text;
        }
        Matcher matcher = SHORTCODE.matcher(text);
        StringBuilder out = new StringBuilder(text.length());
        int last = 0;
        boolean changed = false;
        while (matcher.find()) {
            EmojiEntry emoji = EmojiRegistry.byName(matcher.group(1).toLowerCase(Locale.ROOT));
            if (emoji == null) {
                continue;
            }
            out.append(text, last, matcher.start()).append(emoji.glyph());
            last = matcher.end();
            changed = true;
        }
        if (!changed) {
            return text;
        }
        out.append(text, last, text.length());
        return out.toString();
    }
}
