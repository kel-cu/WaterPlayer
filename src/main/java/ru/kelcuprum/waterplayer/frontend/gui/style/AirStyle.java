package ru.kelcuprum.waterplayer.frontend.gui.style;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.styles.AbstractStyle;

public class AirStyle extends AbstractStyle {
    public AirStyle() {
        super("nothing", Component.literal("nothing"));
    }

    @Override
    public void renderBackground$widget(GuiGraphics guiGraphics, int i, int i1, int i2, int i3, boolean b, boolean b1) {

    }

    @Override
    public void renderBackground$slider(GuiGraphics guiGraphics, int i, int i1, int i2, int i3, boolean b, boolean b1, double v) {

    }
}
