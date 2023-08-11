package ru.kelcuprum.waterplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class MusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public MusicManager(AudioPlayer manager, TrackScheduler sch)
    {
        player = manager;
        scheduler = sch;
        player.addListener(scheduler);
    }
}