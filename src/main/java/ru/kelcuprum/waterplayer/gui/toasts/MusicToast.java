package ru.kelcuprum.waterplayer.gui.toasts;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.localization.Music;

@Environment(EnvType.CLIENT)
public class MusicToast implements Toast {
    public static final int DISPLAY_TIME = 10000;
    private final AudioTrack track;
    private boolean playedSound;

    public MusicToast(AudioTrack track) {
        this.track = track;
    }

    public int widthAuthor(){
        return Minecraft.getInstance().font.width(track.getInfo().author);
    }
    public int widthTitle(){
        return Minecraft.getInstance().font.width(track.getInfo().title);
    }
    public int maxTitleWidth(){
        if(widthTitle()/2 <= 125 && widthAuthor() <= 125) return 125;
        else return Math.max(widthTitle() / 2, widthAuthor());
    }
    public int width(){
        if(widthTitle()/2 <= 125 && widthAuthor() <= 125) return 160;
        else if(widthTitle()/2 > widthAuthor()) return (int) (((widthTitle()/2) / 125.0)*160);
        else return(int) ((widthAuthor() / 125.0)*160);
    }

    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
        final float fc = 1.5F * 0.9F + 0.1F;
        final int colorBackground = (int) (255.0F * fc);
        int statusLine = new Color(track.getInfo().isStream ? 0xB6FF3131 : 0xB631FF83, true).getRGB();
        guiGraphics.fill(0, 0, this.width(), this.height()-1, colorBackground / 2 << 24);
        guiGraphics.fill(0, this.height() - 1, this.width(), this.height(), statusLine);

        List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(Localization.toText(Music.getTitle(this.track)), maxTitleWidth());
        int i = 16746751;
        if (list.size() == 1) {
            guiGraphics.drawString(toastComponent.getMinecraft().font, Localization.toText(Music.getAuthor(this.track)), 30, 7, i | -16777216, false);
            guiGraphics.drawString(toastComponent.getMinecraft().font, list.get(0), 30, 18, 16777215, false);
        } else {
            float f = 300.0F;
            int k;
            if (l < 2000L) {
                k = Mth.floor(Mth.clamp((float) (1500L - l) / f, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                guiGraphics.drawString(toastComponent.getMinecraft().font, Localization.toText(Music.getAuthor(this.track)), 30, 11, i | k, false);
            } else {
                k = Mth.floor(Mth.clamp((float) (l - 1500L) / f, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                int var10000 = this.height() / 2;
                int var10001 = list.size();
                Objects.requireNonNull(toastComponent.getMinecraft().font);
                int m = var10000 - var10001 * 9 / 2;

                for (Iterator var12 = list.iterator(); var12.hasNext(); m += 9) {
                    FormattedCharSequence formattedCharSequence = (FormattedCharSequence) var12.next();
                    guiGraphics.drawString(toastComponent.getMinecraft().font, formattedCharSequence, 30, m, 16777215 | k, false);
                    Objects.requireNonNull(toastComponent.getMinecraft().font);
                }
            }
        }

        if (!this.playedSound && l > 0L) {
            this.playedSound = true;
        }
        guiGraphics.renderFakeItem(new ItemStack(getYonKaGorMoment(track)), 8, 8);
        return (double) l >= DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }

    protected Item getYonKaGorMoment(AudioTrack track) {
        if (!Music.getAuthor().equals("YonKaGor")) return Items.MUSIC_DISC_STRAD;
        return switch (Music.getTitle(track)) {
            case "I Forgot That You Exist", "I Forgot That You Exist. ¯\\_(ツ)_/¯" -> Items.MUSIC_DISC_WAIT;
            case "Top 10 Things to Do Before You Die", "[TW] Top 10 Things To Do Before You Die (Censored)" -> Items.LIGHT;
            case "Trash Talkin'", "kennyoung & YonKaGor - Trash Talkin'" -> Items.MUSIC_DISC_OTHERSIDE;
            case "Fallacy" -> Items.MUSIC_DISC_PIGSTEP;
            case "You're Just Like Pop Music" -> Items.MUSIC_DISC_MELLOHI;
            case "Dandelion", "Dandelion \uD83C\uDF3C (Full Song)" -> Items.DANDELION;
            case "Mr. Sunfish", "Good Morning, Mr. Sunfish!", "Fish ! (Original)" -> Items.TROPICAL_FISH;
            case "You'll Be Gone" -> Items.MUSIC_DISC_MALL;
            case "It's Normal", "It's Normal [TW]" -> Items.MUSIC_DISC_11;
            case "Circus Hop", "Circus Hop [TW]" -> Items.MUSIC_DISC_CHIRP;
            case "Paper Alibis", "Paper Alibis (Full Song)" -> Items.PAPER;
            case "Silly Plans" -> Items.LIGHT_BLUE_BED;
            case "Another Mistake" -> Items.BARRIER;
            case "Artificial Abandonment", "(Original Song) Artificial Abandonment" -> Items.MOSSY_COBBLESTONE;
            default -> Items.MUSIC_DISC_STAL;
        };
    }
}
