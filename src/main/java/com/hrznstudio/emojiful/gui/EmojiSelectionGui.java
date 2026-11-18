package com.hrznstudio.emojiful.gui;

import com.hrznstudio.emojiful.EmojiEntry;
import com.hrznstudio.emojiful.EmojiRegistry;
import com.hrznstudio.emojiful.EmojiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 复刻原版 Emojiful 的内嵌表情小菜单：附着在聊天界面右下角，
 * 顶部搜索框 + 左侧竖排分类图标 + 按分类分组的表情网格 + 底部表情信息栏。
 * 点击表情直接插入聊天输入框，面板保持打开，可连续插入，全程不联网。
 */
public class EmojiSelectionGui {

    private static final int COLS = 9;
    private static final int VISIBLE_LINES = 6;
    private static final int VISIBLE_CATEGORIES = 7;

    private final Screen screen;
    private final EditBox chatInput;
    private final Font font;
    private final EditBox searchBox;

    private final Area panel;
    private final Area categoryArea;
    private final Area infoArea;
    private final Area searchArea;

    private final List<EmojiRegistry.Category> categories;
    private final Map<String, List<EmojiEntry>> byCategory = new LinkedHashMap<>();
    /** 每一行是分类标题（Category）或一行表情（EmojiEntry[]）。 */
    private final List<Object> lines = new ArrayList<>();

    private int selectionPointer = 1;
    private int categoryPointer;
    private boolean open;
    private int mouseX;
    private int mouseY;
    private EmojiEntry hoveredEmoji;

    public EmojiSelectionGui(Screen screen, EditBox chatInput, int width, int height) {
        this.screen = screen;
        this.chatInput = chatInput;
        this.font = Minecraft.getInstance().font;
        this.categories = EmojiRegistry.categories();

        this.panel = new Area(width - 14 - 11 * 12, height - 16 - 10 * 11 - 4, 11 * 12 + 4, 10 * 11 + 4);
        this.categoryArea = new Area(panel.x, panel.y + 20, 22, panel.h - 20);
        this.infoArea = new Area(panel.x + 22, panel.y + panel.h - 20, panel.w - 22, 20);
        this.searchArea = new Area(panel.x + 6, panel.y + 6, panel.w - 12, 10);

        this.searchBox = new EditBox(font, searchArea.x, searchArea.y, searchArea.w, searchArea.h,
                Component.translatable("emojiful.picker.search"));
        this.searchBox.setHint(Component.translatable("emojiful.picker.search"));
        this.searchBox.setResponder(text -> rebuildLines());

        for (EmojiRegistry.Category category : categories) {
            byCategory.put(category.id(), new ArrayList<>());
        }
        for (EmojiEntry entry : EmojiRegistry.all()) {
            byCategory.computeIfAbsent(entry.category(), k -> new ArrayList<>()).add(entry);
        }
        rebuildLines();
    }

    private void rebuildLines() {
        lines.clear();
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            for (EmojiRegistry.Category category : categories) {
                List<EmojiEntry> entries = byCategory.get(category.id());
                if (entries == null || entries.isEmpty()) {
                    continue;
                }
                lines.add(category);
                addRows(entries);
            }
        } else {
            List<EmojiEntry> matched = new ArrayList<>();
            for (EmojiEntry entry : EmojiRegistry.all()) {
                if (entry.matches(query)) {
                    matched.add(entry);
                }
            }
            addRows(matched);
        }
        selectionPointer = 1;
        categoryPointer = Mth.clamp(categoryPointer, 0, maxCategoryPointer());
    }

    private void addRows(List<EmojiEntry> entries) {
        for (int start = 0; start < entries.size(); start += COLS) {
            EmojiEntry[] row = new EmojiEntry[COLS];
            for (int i = 0; i < COLS && start + i < entries.size(); i++) {
                row[i] = entries.get(start + i);
            }
            lines.add(row);
        }
    }

    private Object getLine(int line) {
        return line >= 1 && line <= lines.size() ? lines.get(line - 1) : null;
    }

    private EmojiRegistry.Category categoryOfLine(int line) {
        for (int i = Math.min(line, lines.size()); i >= 1; i--) {
            if (getLine(i) instanceof EmojiRegistry.Category category) {
                return category;
            }
        }
        return null;
    }

    private int maxSelectionPointer() {
        return Math.max(1, lines.size() - (VISIBLE_LINES - 1));
    }

    private int maxCategoryPointer() {
        return Math.max(0, categories.size() - VISIBLE_CATEGORIES);
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.hoveredEmoji = null;

        if (!open) {
            return;
        }

        graphics.fill(panel.x, panel.y, panel.x + panel.w, panel.y + panel.h, 0x80000000);
        graphics.fill(categoryArea.x, categoryArea.y, categoryArea.x + categoryArea.w, categoryArea.y + categoryArea.h, 0x80000000);
        graphics.fill(infoArea.x, infoArea.y, infoArea.x + infoArea.w, infoArea.y + infoArea.h, 0x80000000);

        for (int i = 0; i < VISIBLE_LINES; i++) {
            drawLine(graphics, i * 12, i + selectionPointer);
        }

        if (lines.size() > VISIBLE_LINES) {
            int track = infoArea.y - categoryArea.y - 5;
            int progressY = (int) (track / (double) lines.size() * selectionPointer);
            graphics.fill(panel.x + panel.w - 2, categoryArea.y + progressY,
                    panel.x + panel.w - 1, categoryArea.y + progressY + 5, 0xFF525252);
        }

        drawCategories(graphics);
        drawInfo(graphics);
        searchBox.extractWidgetRenderState(graphics, mouseX, mouseY, 0);
    }

    private void drawLine(GuiGraphicsExtractor graphics, int height, int line) {
        Object lineToDraw = getLine(line);
        if (lineToDraw == null) {
            return;
        }
        int gridX = categoryArea.x + categoryArea.w + 2;
        int y = categoryArea.y + height + 2;
        if (lineToDraw instanceof EmojiRegistry.Category category) {
            graphics.text(font, Component.translatable("emojiful.category." + category.id()), gridX, y, 0xFF969696, false);
            return;
        }
        EmojiEntry[] row = (EmojiEntry[]) lineToDraw;
        for (int i = 0; i < row.length; i++) {
            EmojiEntry entry = row[i];
            if (entry == null) {
                continue;
            }
            int x = gridX + i * 12;
            Area cell = new Area(x, y - 1, 11, 11);
            if (cell.contains(mouseX, mouseY)) {
                hoveredEmoji = entry;
                graphics.fill(cell.x - 1, cell.y - 1, cell.x + cell.w, cell.y + cell.h, 0x80FFFFFF);
            }
            graphics.text(font, EmojiText.glyphComponent(entry), x, y, 0xFFFFFFFF, false);
        }
    }

    private void drawCategories(GuiGraphicsExtractor graphics) {
        EmojiRegistry.Category current = categoryOfLine(selectionPointer);
        for (int i = 0; i < VISIBLE_CATEGORIES; i++) {
            int index = i + categoryPointer;
            if (index >= categories.size()) {
                break;
            }
            EmojiRegistry.Category category = categories.get(index);
            Area icon = new Area(categoryArea.x + 6, categoryArea.y + 6 + i * 12, 11, 11);
            if (category.equals(current)) {
                graphics.fill(icon.x - 1, icon.y - 2, icon.x + icon.w, icon.y + icon.h - 1, 0x80FFFFFF);
            }
            if (icon.contains(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("emojiful.category." + category.id()), mouseX, mouseY);
            }
            List<EmojiEntry> entries = byCategory.get(category.id());
            if (entries != null && !entries.isEmpty()) {
                graphics.text(font, EmojiText.glyphComponent(entries.get(0)), icon.x, icon.y, 0xFFFFFFFF, false);
            }
        }
        if (categories.size() > VISIBLE_CATEGORIES) {
            int progressY = (int) ((categoryArea.h - 10) / (double) (categories.size() - VISIBLE_CATEGORIES) * categoryPointer);
            graphics.fill(categoryArea.x + categoryArea.w - 2, categoryArea.y + progressY + 2,
                    categoryArea.x + categoryArea.w - 1, categoryArea.y + progressY + 7, 0xFF525252);
        }
    }

    private void drawInfo(GuiGraphicsExtractor graphics) {
        if (hoveredEmoji == null) {
            return;
        }
        graphics.text(font, EmojiText.glyphComponent(hoveredEmoji), infoArea.x + 2, infoArea.y + 6, 0xFFFFFFFF, false);
        StringBuilder builder = new StringBuilder(hoveredEmoji.zhName()).append(' ');
        for (String alias : hoveredEmoji.aliases()) {
            builder.append(':').append(alias).append(": ");
        }
        String text = builder.toString().trim();
        int maxWidth = (infoArea.w - 18) * 2;
        while (text.length() > 1 && font.width(text) > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5f, 0.5f);
        graphics.text(font, text, (infoArea.x + 15) * 2, (infoArea.y + 8) * 2, 0xFF969696, false);
        graphics.pose().popMatrix();
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!open) {
            return false;
        }
        boolean inSearch = searchArea.contains(mouseX, mouseY);
        searchBox.setFocused(inSearch);
        if (inSearch) {
            screen.setFocused(searchBox);
            return true;
        }
        if (categoryArea.contains(mouseX, mouseY)) {
            for (int i = 0; i < VISIBLE_CATEGORIES; i++) {
                int index = i + categoryPointer;
                if (index >= categories.size()) {
                    break;
                }
                Area icon = new Area(categoryArea.x + 6, categoryArea.y + 6 + i * 12, 11, 11);
                if (icon.contains(mouseX, mouseY)) {
                    EmojiRegistry.Category target = categories.get(index);
                    for (int line = 1; line <= lines.size(); line++) {
                        if (target.equals(getLine(line))) {
                            selectionPointer = Mth.clamp(line, 1, maxSelectionPointer());
                            break;
                        }
                    }
                }
            }
            return true;
        }
        if (panel.contains(mouseX, mouseY)) {
            for (int line = 0; line < VISIBLE_LINES; line++) {
                if (getLine(line + selectionPointer) instanceof EmojiEntry[] row) {
                    for (int i = 0; i < row.length; i++) {
                        if (row[i] == null) {
                            continue;
                        }
                        int x = categoryArea.x + categoryArea.w + 2 + i * 12;
                        int y = categoryArea.y + line * 12 + 2;
                        Area cell = new Area(x, y - 1, 11, 11);
                        if (cell.contains(mouseX, mouseY)) {
                            chatInput.setValue(chatInput.getValue() + row[i].shortcode());
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!open) {
            return false;
        }
        if (categoryArea.contains(mouseX, mouseY)) {
            categoryPointer = Mth.clamp(categoryPointer - (int) delta, 0, maxCategoryPointer());
            return true;
        }
        if (panel.contains(mouseX, mouseY)) {
            selectionPointer = Mth.clamp(selectionPointer - (int) delta, 1, maxSelectionPointer());
            EmojiRegistry.Category current = categoryOfLine(selectionPointer);
            int index = categories.indexOf(current);
            if (index >= 0) {
                categoryPointer = Mth.clamp(index, 0, maxCategoryPointer());
            }
            return true;
        }
        return false;
    }

    public void toggle() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        open = !open;
        if (!open && screen.getFocused() == searchBox) {
            screen.setFocused(chatInput);
        }
    }

    private static final class Area {
        private final int x;
        private final int y;
        private final int w;
        private final int h;

        private Area(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        }
    }
}
