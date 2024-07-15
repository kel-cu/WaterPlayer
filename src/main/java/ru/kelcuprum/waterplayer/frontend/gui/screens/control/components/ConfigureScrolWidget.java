package ru.kelcuprum.waterplayer.frontend.gui.screens.control.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.kelcuprum.alinlib.gui.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigureScrolWidget extends AbstractScrollWidget {
    public final Consumer<ConfigureScrolWidget> onScroll;
    public int innerHeight;
    public List<AbstractWidget> widgets = new ArrayList<>();

    public ConfigureScrolWidget(int x, int y, int width, int height, Component message, Consumer<ConfigureScrolWidget> onScroll) {
        super(x, y, width, height, message);

        this.onScroll = onScroll;
    }

    @Override
    protected int getInnerHeight() {
        return innerHeight;
    }

    @Override
    protected double scrollRate() {
        return 9.0;
    }

    @Override
    public double scrollAmount() {
        return super.scrollAmount();
    }

    @Override
    protected void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        this.onScroll.accept(this);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        if (this.scrollbarVisible()) guiGraphics.fill(getX()+this.width, getY(), getX()+this.width+4, getY()+getHeight(), 0x75000000);
    }

    public boolean isScrollbarVisible(){
        return this.scrollbarVisible();
    }

    private int getContentHeight() {
        return this.getInnerHeight() + 4;
    }

    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 16, this.height);
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics) {
        if (this.scrollbarVisible()) {
            int i = this.getScrollBarHeight();
            int j = this.getX() + this.width;
            int k = Math.max(this.getY(), (int)this.scrollAmount() * (this.height - i) / this.getMaxScrollAmount() + this.getY());
            RenderSystem.enableBlend();
            guiGraphics.fill(j, k, j+4, k+i, Colors.getScrollerColor());
            RenderSystem.disableBlend();
        }
    }
//    @Override
//    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
//        if (this.visible) {
//            this.renderBackground(guiGraphics);
//            guiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
//            this.renderContents(guiGraphics, i, j, f);
//            guiGraphics.disableScissor();
//            this.renderDecorations(guiGraphics);
//        }
//    }
//
    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderBorder(GuiGraphics guiGraphics, int x, int y, int width, int height) {
    }

    public void resetWidgets(){
        widgets.clear();
        setScrollAmount(0);
    }
    public void addWidget(AbstractWidget widget) {
        widgets.add(widget);
    }
    public void addWidgets(List<AbstractWidget> widgets) {
        for(AbstractWidget widget : widgets) addWidget(widget);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
}