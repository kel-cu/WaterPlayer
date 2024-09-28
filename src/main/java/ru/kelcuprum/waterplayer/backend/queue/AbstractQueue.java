package ru.kelcuprum.waterplayer.backend.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ru.kelcuprum.waterplayer.WaterPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractQueue {
    public List<AudioTrack> tracks;
    public AudioTrack currentTrack;
    public int position = 0;
    public AbstractQueue(List<AudioTrack> tracks){
        this.tracks = tracks;
        if(!tracks.isEmpty()) this.currentTrack = tracks.get(0);
    }

    public AudioTrack getCurrentTrack(){
        return currentTrack == null ? null : currentTrack.makeClone();
    }

    public abstract Boolean addTrackAvailable();

    public abstract String getName();

    public void shuffle(){
        List<AudioTrack> queue = tracks;
        Collections.shuffle(queue);
        if(currentTrack != null){
            AudioTrack first = queue.get(0);
            int i = queue.indexOf(currentTrack);
            queue.set(0, currentTrack);
            queue.set(i, first);
            position = 0;
        }
        tracks = queue;
    }

    public void nextTrack(){
        if(!tracks.isEmpty()) {
            position++;
            if (position >= tracks.size() && WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 1)
                position = 0;
            currentTrack = position >= tracks.size() ? null : tracks.get(position);
        } else currentTrack = null;
    }

    public void backTrack(){
        if(!tracks.isEmpty()) {
            position--;
            if (position < 0)
                position = WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 1 ? (tracks.size()-1) : 0;
            currentTrack = position >= tracks.size() ? null : tracks.get(position);
        } else currentTrack = null;
    }

    public List<AudioTrack> getQueue(){
        List<AudioTrack> queue = new ArrayList<>();
        if(!tracks.isEmpty()){
            for(int i = (position+1); i<tracks.size();i++) queue.add(tracks.get(i));
            if(WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 1) for(int i = 0; i<position; i++) queue.add(tracks.get(i));
        }
        return queue;
    }

    public void addTrack(AudioTrack audioTrack){
        if(audioTrack != null && addTrackAvailable()) {
            if(tracks.isEmpty()) currentTrack = audioTrack;
            tracks.add(audioTrack);
        }
        if(WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null) WaterPlayer.player.getAudioPlayer().startTrack(audioTrack, false);
    }
}
