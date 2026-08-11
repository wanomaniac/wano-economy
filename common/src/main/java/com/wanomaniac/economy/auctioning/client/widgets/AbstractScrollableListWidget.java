package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.client.GUIInputUtil;
import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractScrollableListWidget {
    protected int x, y, width, height;
    protected final int headerHeight;
    protected final int itemSpacing;
    protected final EditBox searchBox;

    protected int scrollOffset = 0;
    protected boolean isDragging = false;

    public boolean active = true;
    protected boolean isScissorActive = false;
    private boolean wasCtrlDown = false;

    public AbstractScrollableListWidget(int x, int y, int width, int height, int headerHeight, int itemSpacing, String searchHintText) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.headerHeight = headerHeight;
        this.itemSpacing = itemSpacing;

        this.searchBox = new EditBox(Minecraft.getInstance().font, x, y-20, 120, 16, Component.literal("Search..."));
        this.searchBox.setHint(Component.literal(searchHintText).withStyle(ChatFormatting.DARK_GRAY));
    }

    // Window resizes need to repositon otherwise it'll be reset which is bad.
    public void setPositionAndBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.searchBox.setX(x);
        this.searchBox.setY(y - 20);
        this.searchBox.setWidth(120);
        int viewHeight = height - headerHeight;
        int totalContentHeight = getItemCount() * itemSpacing;
        int maxScroll = Math.max(0, totalContentHeight - viewHeight);
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, maxScroll);
    }

    /**
     * Subclasses override this to render their items inside the scissored view area.
     * @param currentY The Y position already adjusted for scrollOffset (viewTop - scrollOffset).
     */
    protected abstract void renderContent(GuiGraphics graphics, int viewTop, int viewBottom, int currentY, int mouseX, int mouseY);

    /**
     * Override this in subclasses if you have sticky elements at the top of your scroll view.
     * @return The height (in pixels) of sticky items that DO NOT scroll.
     */
    protected int getTopStaticHeight() {
        return 0; // Default = no sticky header items inside the view
    }

    /**
     * @return The total number of items currently in the list (after filtering).
     */
    protected abstract int getItemCount();

    protected int getScrollAreaTop() {
        return y + headerHeight + getTopStaticHeight();
    }

    protected int getScrollAreaHeight() {
        return (y + height) - getScrollAreaTop();
    }

    protected int getTotalScrollableContentHeight() {
        return getItemCount() * itemSpacing;
    }

    protected int getMaxScroll() {
        return Math.max(0, getTotalScrollableContentHeight() - getScrollAreaHeight());
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!active) return;
        searchBox.render(graphics, mouseX, mouseY, delta);

        int viewTop = y + headerHeight;
        int viewBottom = y + height;
        int scrollAreaTop = getScrollAreaTop();
        int scrollAreaHeight = getScrollAreaHeight();

        int totalContentHeight = getTotalScrollableContentHeight();
        int maxScroll = getMaxScroll();
        this.scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

        int x2 = x + width + 2;
        int minX = Math.min(x, x2) - 2;
        int maxX = Math.max(x, x2) + 2;

        graphics.enableScissor(minX, y, maxX, viewBottom);
        isScissorActive = true;

        int currentY = scrollAreaTop - this.scrollOffset;
        renderContent(graphics, viewTop, viewBottom, currentY, mouseX, mouseY);

        if (isScissorActive) graphics.disableScissor();

        if (totalContentHeight > scrollAreaHeight && scrollAreaHeight > 0) {
            int scrollbarX = x + width - 4;

            graphics.fill(scrollbarX, scrollAreaTop, scrollbarX + 4, viewBottom, 0xFF222222);

            int thumbHeight = Math.max(12, (int) ((float) scrollAreaHeight / totalContentHeight * scrollAreaHeight));
            int thumbY = scrollAreaTop + (int) ((float) this.scrollOffset / maxScroll * (scrollAreaHeight - thumbHeight));

            int thumbColor = this.isDragging ? 0xFFAAAAAA : 0xFF888888;
            graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, thumbColor);
        }
    }

    // --- GENERIC MOUSE INPUT HANDLERS ---
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if(!active || !isMouseOver(mouseX, mouseY)) return false;

        this.scrollOffset -= (int) (scrollY * 14);
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, getMaxScroll());
        return true;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(!active) return false;

        if (event.button() == 0 && isMouseOver(event.x(), event.y())) {
            int scrollbarX = x + width - 4;
            int scrollAreaTop = getScrollAreaTop();
            int viewBottom = y + height;

            if (event.x() >= scrollbarX - 4 && event.x() <= scrollbarX + 8 && event.y() >= scrollAreaTop && event.y() <= viewBottom) {
                this.isDragging = true;
                updateScrollFromMouse(event.y());
                return true;
            }
        }

        if (GUIInputUtil.onEditBoxMouseClicked(searchBox, event, doubleClick)) {
            searchBox.setFocused(true);
            return true;
        }

        searchBox.setFocused(false);
        return false;
    }

    public boolean mouseDragged(double mouseY) {
        if(!active) return false;

        if (this.isDragging) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return false;
    }

    public void mouseReleased(int button) {
        if (button == 0) {
            this.isDragging = false;
        }
    }

    protected void updateScrollFromMouse(double mouseY) {
        int scrollAreaTop = getScrollAreaTop();
        int scrollAreaHeight = getScrollAreaHeight();
        int maxScroll = getMaxScroll();

        if (maxScroll == 0) {
            this.scrollOffset = 0;
            return;
        }

        float progress = (float) (mouseY - scrollAreaTop) / (float) scrollAreaHeight;
        this.scrollOffset = Math.clamp((int) (progress * maxScroll), 0, maxScroll);
    }

    public boolean whenKeyPressed(KeyEvent event){
        if(!active) return false;

        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (searchBox.isFocused()) {
                searchBox.setFocused(false);
                return true; // stop ESC from closing the screen
            }
        }

        if(searchBox.isFocused()){
            wasCtrlDown = event.hasControlDown();
            GUIInputUtil.onEditBoxKeyPressed(searchBox, event);
            return true;
        }

        return false;
    }

    public boolean whenCharTyped(CharacterEvent event){
        if(!active || wasCtrlDown) return false;

        if(searchBox.isFocused()){
            if(GUIInputUtil.onEditBoxCharTyped(searchBox, event)) return true;
        }

        return false;
    }
}
