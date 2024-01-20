package ru.kelcuprum.waterplayer.frontend.localization;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ru.kelcuprum.waterplayer.WaterPlayer;

public class Music {
    //
    public static boolean trackIsNull(){
        return trackIsNull(WaterPlayer.music.getAudioPlayer().getPlayingTrack());
    }
    public static boolean trackIsNull(AudioTrack track){
        return track == null;
    }
    //
    public static boolean isAuthorNull(AudioTrack info){
        return trackIsNull(info) || info.getInfo().author.equals("Unknown artist");
    }
    public static boolean isAuthorNull() {return isAuthorNull(WaterPlayer.music.getAudioPlayer().getPlayingTrack());}
    //
    public static String getAuthor(AudioTrack info){
        return isAuthorNull(info) ? "" : info.getInfo().author;
    }
    public static String getAuthor() {return getAuthor(WaterPlayer.music.getAudioPlayer().getPlayingTrack());}
    //
    public static boolean isTitleNull(AudioTrack info){
        return trackIsNull(info) || info.getInfo().title.equals("Unknown title");
    }
    public static boolean isTitleNull() {return isTitleNull(WaterPlayer.music.getAudioPlayer().getPlayingTrack());}
    //
    public static String getTitle(AudioTrack info){

        String[] fileArgs = info.getInfo().uri.split("/");
        if(fileArgs.length == 1) fileArgs = info.getInfo().uri.split("\\\\");
        String file = fileArgs[fileArgs.length-1];
        return isTitleNull(info) ? file : info.getInfo().title;
    }
    public static String getTitle() {return getTitle(WaterPlayer.music.getAudioPlayer().getPlayingTrack());}
    //
    public static int getVolume(){
        return WaterPlayer.music.getVolume();
    }
    public static String getSpeakerVolume(){
        return (getVolume() <= 0) ? "🔇" : (getVolume() <= 1) ? "🔈" : (getVolume() <= 70) ? "🔉" :  "🔊";
    }
    //
    public static long getPosition(AudioTrack track){
        return trackIsNull() ? 0 : track.getPosition();
    }
    public static long getPosition() {return getPosition(WaterPlayer.music.getAudioPlayer().getPlayingTrack());}
    //
    public static long getDuration(AudioTrack track){
        return trackIsNull() ? 0 : track.getDuration();
    }
    public static long getDuration() {return getDuration(WaterPlayer.music.getAudioPlayer().getPlayingTrack());}
    //
    public static boolean getIsLive(){return getIsLive(WaterPlayer.music.getAudioPlayer().getPlayingTrack());}
    public static boolean getIsLive(AudioTrack track){return trackIsNull(track) ? false : WaterPlayer.music.getAudioPlayer().getPlayingTrack().getInfo().isStream;}

}
