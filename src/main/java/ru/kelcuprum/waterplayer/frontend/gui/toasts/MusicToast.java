package ru.kelcuprum.waterplayer.frontend.gui.toasts;

import java.util.Iterator;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.Music;

@Environment(EnvType.CLIENT)
public class MusicToast implements Toast {
    private final AudioTrack track;

    public MusicToast(AudioTrack track) {
        this.track = track;
    }

    public int widthAuthor(){
        return WaterPlayer.MINECRAFT.font.width(track.getInfo().author);
    }
    public int widthTitle(){
        return WaterPlayer.MINECRAFT.font.width(track.getInfo().title);
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

    @Override
    public @NotNull Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
        final float fc = 1.5F * 0.9F + 0.1F;
        final int colorBackground = (int) (255.0F * fc);

        guiGraphics.fill(0, 0, width(), height() - 1, colorBackground / 2 << 24);
        guiGraphics.fill(0, height() - 1, width(), height(), track.getInfo().isStream ? InterfaceUtils.Colors.GROUPIE : InterfaceUtils.Colors.SEADRIVE);

        Font font = WaterPlayer.MINECRAFT.font;
        Component author = Music.isAuthorNull(track) ? Component.translatable("waterplayer.name") : Component.literal(Music.getAuthor(track));
        List<FormattedCharSequence> list = font.split(Component.literal(Music.getTitle(track)), maxTitleWidth());
        int i = 16777215;
        if (list.size() == 1) {
            guiGraphics.drawString(font, author, 30, 7, i, false);
            guiGraphics.drawString(font, list.get(0), 30, 18, i, false);
        } else {
            float f = 300.0F;
            int k;
            if (l < 1000L) {
                k = Mth.floor(Mth.clamp((float) (1000L - l) / f, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                guiGraphics.drawString(font, author, 30, 11, i | k, false);
            } else {
                k = Mth.floor(Mth.clamp((float) (l - 1000L) / f, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                int var10001 = list.size();
                int m = (this.height() / 2) - var10001 * 9 / 2;

                for (Iterator<FormattedCharSequence> var12 = list.iterator(); var12.hasNext(); m += 9) {
                    guiGraphics.drawString(font, var12.next(), 30, m, i | k, false);
                }
            }
        }
        guiGraphics.renderFakeItem(getYonKaGorMoment(track).getDefaultInstance(), 8, 8);

        return (double) l >= 10000 * toastComponent.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
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
            case "Silly Plans ~ Revisit" -> Items.FILLED_MAP;
            case "Another Mistake" -> Items.BARRIER;
            case "Artificial Abandonment", "(Original Song) Artificial Abandonment" -> Items.MOSSY_COBBLESTONE;
            default -> Items.MUSIC_DISC_STRAD;
        };
    }
}
