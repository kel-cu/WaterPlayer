package ru.kelcuprum.waterplayer.toasts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.kelcuprum.waterplayer.localization.Localization;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ControlToast implements Toast {
    private final Component title = Localization.getText("waterplayer.name");
    public static final int DISPLAY_TIME = 5000;
    private final Component message;
    private final boolean isFail;
    private boolean playedSound;

    public ControlToast(Component message, boolean isFail) {
        this.message = message;
        this.isFail = isFail;
    }

    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
        int rgba = new Color(0x75020E15, true).getRGB();
        int r = FastColor.ARGB32.red(rgba);
        int g = FastColor.ARGB32.green(rgba);
        int b = FastColor.ARGB32.blue(rgba);
        int a = FastColor.ARGB32.alpha(rgba);

        int rgbaTimeLine = new Color(this.isFail ? 0xB6FF3131 : 0xB631FF83, true).getRGB();
        int rTimeLine = FastColor.ARGB32.red(rgbaTimeLine);
        int gTimeLine = FastColor.ARGB32.green(rgbaTimeLine);
        int bTimeLine = FastColor.ARGB32.blue(rgbaTimeLine);
        int aTimeLine = FastColor.ARGB32.alpha(rgbaTimeLine);
        guiGraphics.fill(0, 1, this.width(), this.height() - 2, FastColor.ARGB32.color(a, r, g, b));
        guiGraphics.fill(0, this.height() - 1, this.width(), this.height(), FastColor.ARGB32.color(aTimeLine, rTimeLine, gTimeLine, bTimeLine));

        List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(this.message, 125);
        int i = 16777215;
        if (list.size() == 1) {
            guiGraphics.drawString(toastComponent.getMinecraft().font, title, 30, 7, i, false);
            guiGraphics.drawString(toastComponent.getMinecraft().font, list.get(0), 30, 18, i, false);
        } else {
            float f = 300.0F;
            int k;
            if (l < 1000L) {
                k = Mth.floor(Mth.clamp((float) (1000L - l) / f, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                guiGraphics.drawString(toastComponent.getMinecraft().font, title, 30, 11, i | k, false);
            } else {
                k = Mth.floor(Mth.clamp((float) (l - 1000L) / f, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                int var10000 = this.height() / 2;
                int var10001 = list.size();
                Objects.requireNonNull(toastComponent.getMinecraft().font);
                int m = var10000 - var10001 * 9 / 2;

                for (Iterator var12 = list.iterator(); var12.hasNext(); m += 9) {
                    FormattedCharSequence formattedCharSequence = (FormattedCharSequence) var12.next();
                    guiGraphics.drawString(toastComponent.getMinecraft().font, formattedCharSequence, 30, m, i | k, false);
                    Objects.requireNonNull(toastComponent.getMinecraft().font);
                }
            }
        }

        if (!this.playedSound && l > 0L) {
            this.playedSound = true;
        }
        guiGraphics.renderFakeItem(new ItemStack(isFail ? Items.MUSIC_DISC_11 : Items.MUSIC_DISC_STRAD), 8, 8);
        return (double) l >= DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }
}
