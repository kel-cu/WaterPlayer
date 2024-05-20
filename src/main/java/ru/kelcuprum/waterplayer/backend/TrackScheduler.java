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
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.Music;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    public boolean skiping = false;
    final AudioPlayer player;
    public final Queue<AudioTrack> queue;
    public AudioTrack lastTrack;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {

        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (track == null) return;
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if(getRepeatStatus() == 2) queue(player.getPlayingTrack().makeClone());
        player.startTrack(queue.poll(), false);
        if(player.getPlayingTrack() != null) WaterPlayer.log("Starting Track: " + Music.getTitle(player.getPlayingTrack()));
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (getRepeatStatus() == 1)
                player.startTrack(lastTrack.makeClone(), false);
            else
                nextTrack();
        }

    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true) && WaterPlayer.config.getBoolean("ENABLE_NOTICE.START_TRACK", true)) {
            if (WaterPlayer.config.getBoolean("ENABLE_NOTICE.START_TRACK.CLEAR", false))
                WaterPlayer.MINECRAFT.getToasts().clear();
            ToastBuilder toast = WaterPlayer.getToast().setTitle(Music.isAuthorNull(track) ? Component.translatable("waterplayer.name") : Component.literal(Music.getAuthor(track)))
                    .setMessage(Component.literal(Music.getTitle(track)));
            if (Music.getAuthor(track).equals("YonKaGor")) toast.setIcon(getYonKaGorMoment(track));
            else toast.setIcon(new ResourceLocation("waterplayer", "textures/music.png"));
            toast.show(WaterPlayer.MINECRAFT.getToasts());
        }
    }
    public int getRepeatStatus() {
        return repeatStatus;
    }

    public int repeatStatus = 0;
    public int changeRepeatStatus(){
        if(repeatStatus+1>2) repeatStatus = 0;
        else repeatStatus = repeatStatus+1;
        return repeatStatus;
    };
    public void setRepeatStatus(int i){
        if(i>2) i = 0;
        else if(i<0) i =2;
        repeatStatus = i;
    }

    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }

    protected Item getYonKaGorMoment(AudioTrack track) {
        if (!Music.getAuthor(track).equals("YonKaGor")) return Items.MUSIC_DISC_STRAD;
        return switch (Music.getTitle(track)) {
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
            case "Artificial Abandonment", "(Original Song) Artificial Abandonment" -> Items.MOSSY_COBBLESTONE;
            default -> Items.MUSIC_DISC_STRAD;
        };
    }
}
