package ru.kelcuprum.waterplayer.backend.search;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.MusicManager;
import ru.kelcuprum.waterplayer.backend.TrackScheduler;

import java.util.List;

public class TrackSearch {
	
	private final AudioPlayerManager audioPlayerManager;
	private final MusicManager musicManagers;
	
	public TrackSearch(AudioPlayerManager audioplayermanager, AudioPlayer audioPlayer, TrackScheduler trackScheduler) {
		this.audioPlayerManager = audioplayermanager;
		this.musicManagers = new MusicManager(audioPlayer, trackScheduler);
	}

	public void getTracks(String url)
	{
		final String trackUrl = url;
		audioPlayerManager.loadItemOrdered(musicManagers, trackUrl, new AudioLoadResultHandler()
		{
			@Override
			public void trackLoaded(AudioTrack track)
			{
				musicManagers.scheduler.queue(track);
				WaterPlayer.log("Add track: "+track.getInfo().title);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist)
			{
				WaterPlayer.log("Add playlist: "+playlist.getName());
				List<AudioTrack> tracks = playlist.getTracks();
				tracks.forEach(musicManagers.scheduler::queue);
			}

			@Override
			public void noMatches()
			{
				WaterPlayer.log("Nothing found by " + trackUrl, Level.ERROR);
			}

			@Override
			public void loadFailed(FriendlyException exception)
			{
				WaterPlayer.log(exception.getMessage(), Level.ERROR);
			}
		});
	}
}
