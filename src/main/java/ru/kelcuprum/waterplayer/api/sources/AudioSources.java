package ru.kelcuprum.waterplayer.api.sources;

import com.github.topisenpai.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topisenpai.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.github.topisenpai.lavasrc.yandexmusic.YandexMusicSourceManager;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.api.sources.flowerytts.FloweryTTSSourceManager;

public class AudioSources {
	
	public static void registerSources(AudioPlayerManager audioPlayerManager) {
		final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true);
		youtube.setPlaylistPageCount(100);
		Config config = WaterPlayer.config;
		if(!config.getString("YANDEX_MUSIC_TOKEN", "").isBlank()) audioPlayerManager.registerSourceManager(new YandexMusicSourceManager(config.getString("YANDEX_MUSIC_TOKEN", "")));
		if(!config.getString("FLOWERY_TTS_VOICE", "Alena").isBlank()) audioPlayerManager.registerSourceManager(new FloweryTTSSourceManager(config.getString("FLOWERY_TTS_VOICE", "")));
		if(!config.getString("DEEZER_DECRYPTION_KEY", "").isBlank()) audioPlayerManager.registerSourceManager(new DeezerAudioSourceManager(config.getString("DEEZER_DECRYPTION_KEY", "")));
		if(!config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", "").isBlank() && !config.getString("APPLE_MUSIC_COUNTRY_CODE", "us").isBlank()) audioPlayerManager.registerSourceManager(new AppleMusicSourceManager(null, config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", ""), config.getString("APPLE_MUSIC_COUNTRY_CODE", "us"), audioPlayerManager));
		if(!config.getString("SPOTIFY_CLIENT_ID", "").isBlank() && !config.getString("SPOTIFY_CLIENT_SECRET", "").isBlank() && !config.getString("SPOTIFY_COUNTRY_CODE", "US").isBlank()) audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, config.getString("SPOTIFY_CLIENT_ID", ""), config.getString("SPOTIFY_CLIENT_SECRET", ""), config.getString("SPOTIFY_COUNTRY_CODE", "US"), audioPlayerManager));

		audioPlayerManager.registerSourceManager(youtube);
		audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
		audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
		audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
		audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
		audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
		audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
	}
}
