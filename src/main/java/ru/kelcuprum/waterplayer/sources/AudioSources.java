package ru.kelcuprum.waterplayer.sources;

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
import ru.kelcuprum.waterplayer.config.UserConfig;
import ru.kelcuprum.waterplayer.sources.flowerytts.FloweryTTSSourceManager;

public class AudioSources {
	
	public static void registerSources(AudioPlayerManager audioPlayerManager) {
//		final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(false, UserConfig.YOUTUBE_EMAIL, UserConfig.YOUTUBE_PASSWORD);
		final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true);
		youtube.setPlaylistPageCount(100);
		if(!UserConfig.YANDEX_MUSIC_TOKEN.isBlank()) audioPlayerManager.registerSourceManager(new YandexMusicSourceManager(UserConfig.YANDEX_MUSIC_TOKEN));
		if(!UserConfig.FLOWERY_TTS_VOICE.isBlank()) audioPlayerManager.registerSourceManager(new FloweryTTSSourceManager(UserConfig.FLOWERY_TTS_VOICE));
		if(!UserConfig.DEEZER_DECRYPTION_KEY.isBlank()) audioPlayerManager.registerSourceManager(new DeezerAudioSourceManager(UserConfig.DEEZER_DECRYPTION_KEY));
		if(!UserConfig.APPLE_MUSIC_MEDIA_API_TOKEN.isBlank() && !UserConfig.APPLE_MUSIC_COUNTRY_CODE.isBlank()) audioPlayerManager.registerSourceManager(new AppleMusicSourceManager(null, UserConfig.APPLE_MUSIC_MEDIA_API_TOKEN, UserConfig.APPLE_MUSIC_COUNTRY_CODE, audioPlayerManager));
		if(!UserConfig.SPOTIFY_CLIENT_ID.isBlank() && !UserConfig.SPOTIFY_CLIENT_SECRET.isBlank() && !UserConfig.SPOTIFY_COUNTRY_CODE.isBlank()) audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, UserConfig.SPOTIFY_CLIENT_ID, UserConfig.SPOTIFY_CLIENT_SECRET, UserConfig.SPOTIFY_COUNTRY_CODE, audioPlayerManager));

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
