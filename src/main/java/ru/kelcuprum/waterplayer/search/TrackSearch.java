package ru.kelcuprum.waterplayer.search;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.Client;
import ru.kelcuprum.waterplayer.MusicManager;
import ru.kelcuprum.waterplayer.TrackScheduler;
import ru.kelcuprum.waterplayer.config.Localization;

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
				Client.log("Add track: "+track.getInfo().title);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist)
			{
				Client.log("Add playlist: "+playlist.getName());
				List<AudioTrack> tracks = playlist.getTracks();
				tracks.forEach(musicManagers.scheduler::queue);
			}

			@Override
			public void noMatches()
			{
				Client.log("Nothing found by " + trackUrl, Level.ERROR);
			}

			@Override
			public void loadFailed(FriendlyException exception)
			{
				Client.log(exception.getMessage(), Level.ERROR);
			}
		});
	}
}
