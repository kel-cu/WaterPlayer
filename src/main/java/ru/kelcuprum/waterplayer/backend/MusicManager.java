package ru.kelcuprum.waterplayer.backend;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class MusicManager {
    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    public MusicManager(AudioPlayer manager, TrackScheduler sch)
    {
        player = manager;
        scheduler = sch;
        player.addListener(scheduler);
    }
}