package ru.kelcuprum.waterplayer.backend;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.util.*;

import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class TrackScheduler extends AudioEventAdapter {
    public boolean skiping = false;
    final AudioPlayer player;
    public final Queue<AudioTrack> queue;
    public AudioTrack lastTrack;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
    }
    public void queue(AudioTrack track) {
        if (track == null) return;
        if (!player.startTrack(track, true)) queue.offer(track);
    }

    public void nextTrack() {
        if(getRepeatStatus() == 1 && !queue.isEmpty()) {
            AudioTrack track = player.getPlayingTrack() != null ? player.getPlayingTrack().makeClone() : lastTrack != null ? lastTrack.makeClone() : null;
            if(track == null) WaterPlayer.log("There's nothing to add to the queue", Level.DEBUG);
            else queue.add(track.makeClone());
        }
        player.startTrack(queue.poll(), false);
        if(player.getPlayingTrack() != null) {
            WaterPlayer.log("----------");
            WaterPlayer.log("Starting Track: " + (MusicHelper.isAuthorNull() ? "" : (MusicHelper.getAuthor(player.getPlayingTrack()) + " - ")) + MusicHelper.getTitle(player.getPlayingTrack()));
            WaterPlayer.log("Address: "+player.getPlayingTrack().getInfo().uri);
            if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true) && WaterPlayer.config.getBoolean("ENABLE_NOTICE.START_TRACK", true)) {
                if (WaterPlayer.config.getBoolean("ENABLE_NOTICE.START_TRACK.CLEAR", false))
                    AlinLib.MINECRAFT
                                        //#if MC >= 12102
                                        .getToastManager()
                                //#elseif MC < 12102
                                //$$.getToasts()
                                //#endif
                        .clear();
                ToastBuilder toast = WaterPlayer.getToast().setTitle(MusicHelper.isAuthorNull(player.getPlayingTrack()) ? Component.translatable("waterplayer.name") : Component.literal(MusicHelper.getAuthor(player.getPlayingTrack())))
                        .setMessage(Component.literal(MusicHelper.getTitle(player.getPlayingTrack())));
                if (MusicHelper.getAuthor(player.getPlayingTrack()).equals("YonKaGor")) toast.setIcon(getYonKaGorMoment(player.getPlayingTrack()));
                else toast.setIcon(MUSIC);
                toast.buildAndShow();
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        if (endReason.mayStartNext) {
            if (getRepeatStatus() == 2) player.startTrack(lastTrack.makeClone(), false);
            else nextTrack();
        }

    }
    public static long trackPosition = 0;
    public static double trackSpeed = 0;
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        trackPosition = track.getPosition();
        trackSpeed = WaterPlayer.player.speed;
    }

    public int getRepeatStatus() {
        return repeatStatus;
    }
    
    public ResourceLocation getRepeatIcon(){
        return getRepeatStatus() == 0 ? REPEAT_NON : getRepeatStatus() == 1 ? REPEAT : REPEAT_ONE;
    }

    public int repeatStatus = WaterPlayer.config.getNumber("REPEAT_STATUS", 0).intValue();

    public int changeRepeatStatus(){
        int changedRepeatStatus = repeatStatus+1;
        if(changedRepeatStatus>2) changedRepeatStatus = 0;
        setRepeatStatus(changedRepeatStatus);
        return repeatStatus;
    }

    public void setRepeatStatus(int i){
        i = i > 2 ? 0 : i < 0 ? 2 : i;
        WaterPlayer.config.setNumber("REPEAT_STATUS", i);
        repeatStatus = i;
    }

    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }

    protected Item getYonKaGorMoment(AudioTrack track) {
        if (!MusicHelper.getAuthor(track).equals("YonKaGor")) return Items.MUSIC_DISC_STRAD;
        return switch (MusicHelper.getTitle(track)) {
            case "I Forgot That You Exist", "I Forgot That You Exist. ¯\\_(ツ)_/¯" -> Items.MUSIC_DISC_WAIT;
            case "Top 10 Things to Do Before You Die", "Top 10 Things To Do Before You Die",
                 "[TW] Top 10 Things To Do Before You Die (Censored)" -> Items.LIGHT;
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
            case "Memory Merge" -> Items.FLINT_AND_STEEL;
            case "Waterland", "Waterland (Lyric video)" -> Items.WATER_BUCKET;
            case "Artificial Abandonment", "(Original Song) Artificial Abandonment" -> Items.MOSSY_COBBLESTONE;
            default -> Items.MUSIC_DISC_STRAD;
        };
    }
}
