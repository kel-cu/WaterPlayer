package ru.kelcuprum.waterplayer.frontend.localization;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.TrackScheduler;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;

import java.io.File;
import java.util.HashMap;

import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class MusicHelper {
    //
    public static boolean trackIsNull(){
        return trackIsNull(WaterPlayer.player.getAudioPlayer().getPlayingTrack());
    }
    public static boolean trackIsNull(AudioTrack track){
        return track == null;
    }
    //
    public static boolean isAuthorNull(AudioTrack info){
        return trackIsNull(info) || info.getInfo().author.isBlank() || info.getInfo().author.equals("Unknown artist") || info.getInfo().author.equals("Unknown");
    }
    public static boolean isAuthorNull() {return isAuthorNull(WaterPlayer.player.getAudioPlayer().getPlayingTrack());}
    //
    public static String getAuthor(AudioTrack info){
        String author = isAuthorNull(info) ? "" : info.getInfo().author;
        if(author.endsWith(" - Topic")) author = author.replace(" - Topic", "");
        return author;
    }
    public static String getAuthor() {return getAuthor(WaterPlayer.player.getAudioPlayer().getPlayingTrack());}
    //
    public static boolean isTitleNull(AudioTrack info){
        return trackIsNull(info) || info.getInfo().title.isBlank() || info.getInfo().title.equals("Unknown title");
    }
    //
    public static String getTitle(AudioTrack info){
        if(trackIsNull(info)) return "";
        String[] fileArgs = info.getInfo().uri.split("/");
        if(fileArgs.length == 1) fileArgs = info.getInfo().uri.split("\\\\");
        String file = fileArgs[fileArgs.length-1];
        return isTitleNull(info) ? file : info.getInfo().title;
    }
    public static String getTitle() {return getTitle(WaterPlayer.player.getAudioPlayer().getPlayingTrack());}
    //
    public static int getVolume(){
        return WaterPlayer.player.getVolume();
    }
    public static String getSpeakerVolume(){
        return (getVolume() <= 0) ? "🔇" : (getVolume() <= 1) ? "🔈" : (getVolume() <= 70) ? "🔉" :  "🔊";
    }
    public static ResourceLocation getSpeakerVolumeIcon(){
        return ((getVolume() <= 0) ? VOLUME_MUTE : (getVolume() <= 1) ? VOLUME_LOW : (getVolume() <= 70) ? VOLUME_OK :  VOLUME_MAX);
    }
    public static String getRepeatState(){
        return WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 0 ? "" : WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 1 ? " 🔁" : " 🔂";
    }
    public static String getPauseState(){
        return WaterPlayer.player.isPaused() ? "⏸" : "▶";
    }
    private static final HashMap<String, Boolean> noFiles /*?*/ = new HashMap<>();
    public static boolean isFile(AudioTrack info){
        if(trackIsNull(info)) return false;
        if(noFiles.containsKey(info.getInfo().uri)) return noFiles.getOrDefault(info.getInfo().uri, false);
        else {
            File track = new File(info.getInfo().uri);
            boolean state = track.exists() && track.isFile();
            noFiles.put(info.getInfo().uri, state);
            return state;
        }
    }

    public static ResourceLocation getThumbnail(){
        return trackIsNull() ? NO_ICON : getThumbnail(WaterPlayer.player.getAudioPlayer().getPlayingTrack());
    }
    public static ResourceLocation getThumbnail(AudioTrack info){
        if(MusicHelper.isFile(info) && !TextureHelper.urlsTextures.containsKey(info.getInfo().uri)) return TextureHelper.getTexture$File(new File(info.getInfo().uri), (info.getSourceManager().getSourceName() + "_" + info.getInfo().identifier));
        return trackIsNull(info) ? NO_ICON : TextureHelper.getTexture(info.getInfo().artworkUrl == null ? info.getInfo().uri : info.getInfo().artworkUrl, (info.getSourceManager().getSourceName() + "_" + info.getInfo().identifier));// : MusicHelper.isFile(info) ? FILE_ICON : NO_ICON;
    }

    public static boolean isFile(){
        return trackIsNull() || isFile(WaterPlayer.player.getAudioPlayer().getPlayingTrack());
    }
    //
    public static long getPosition(AudioTrack track){
        if(WaterPlayer.config.getBoolean("EXPERIMENT.FILTERS", false) && TrackScheduler.trackSpeed != 1.0 && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == track) return TrackScheduler.trackPosition;
        return trackIsNull(track) ? 0 : track.getPosition();
    }
    public static long getPosition() {
        return getPosition(WaterPlayer.player.getAudioPlayer().getPlayingTrack());
    }
    //
    public static long getDuration(AudioTrack track){
        return trackIsNull(track) ? 0 : track.getDuration();
    }
    public static long getDuration() {return getDuration(WaterPlayer.player.getAudioPlayer().getPlayingTrack());}
    //
    public static String getService(AudioTrack track){
        return trackIsNull(track) ? "" : track.getSourceManager().getSourceName();
    }
    public static String getService() {return getService(WaterPlayer.player.getAudioPlayer().getPlayingTrack());}

    //

    public static Component getServiceName(String service){
        return switch (service.toLowerCase()){
            case "youtube" -> Component.translatable("waterplayer.config.services.youtube");
            case "soundcloud" -> Component.translatable("waterplayer.config.services.soundcloud");
            case "bandcamp" -> Component.translatable("waterplayer.config.services.bandcamp");
            case "vimeo" -> Component.translatable("waterplayer.config.services.vimeo");
            case "twitch" -> Component.translatable("waterplayer.config.services.twitch");
            case "beam.pro" -> Component.translatable("waterplayer.config.services.beam");
            case "vkmusic" -> Component.translatable("waterplayer.config.services.vk");

            case "yandexmusic" -> Component.translatable("waterplayer.config.services.yandex");
            case "spotify" -> Component.translatable("waterplayer.config.services.spotify");
            case "deezer" -> Component.translatable("waterplayer.config.services.deezer");
            case "applemusic" -> Component.translatable("waterplayer.config.services.apple");
            case "flowery-tts" -> Component.translatable("waterplayer.config.services.flowery");
            case "http" -> Component.translatable("waterplayer.config.services.http");
            case "local" -> Component.translatable("waterplayer.config.services.local");
            default -> Component.literal(service);
        };
    }

    //
    public static boolean getIsLive(){return getIsLive(WaterPlayer.player.getAudioPlayer().getPlayingTrack());}
    public static boolean getIsLive(AudioTrack track){return !trackIsNull(track) && WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().isStream;}

}
