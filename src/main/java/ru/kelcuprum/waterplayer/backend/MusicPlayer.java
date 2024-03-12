package ru.kelcuprum.waterplayer.backend;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.flowerytts.FloweryTTSSourceManager;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.yandexmusic.YandexMusicSourceManager;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
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
import ru.kelcuprum.waterplayer.backend.output.AudioOutput;
import ru.kelcuprum.waterplayer.backend.search.TrackSearch;

public class MusicPlayer {

    private final AudioPlayerManager audioPlayerManager;
    private final AudioDataFormat audioDataFormat;
    private final AudioPlayer audioPlayer;
    private final AudioOutput audioOutput;

    private final TrackSearch trackSearch;
    private final TrackScheduler trackManager;

    public MusicPlayer() {
        audioPlayerManager = new DefaultAudioPlayerManager();
        audioDataFormat = new Pcm16AudioDataFormat(2, 48000, 960, true);
        audioPlayer = audioPlayerManager.createPlayer();
        audioOutput = new AudioOutput(this);

        trackManager = new TrackScheduler(audioPlayer);
        trackSearch = new TrackSearch(audioPlayerManager, audioPlayer, trackManager);
        audioPlayer.setVolume(WaterPlayer.config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue());
        setup();
    }

    private void setup() {
        audioPlayerManager.setFrameBufferDuration(1000);
        audioPlayerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);

        audioPlayerManager.getConfiguration().setResamplingQuality(ResamplingQuality.HIGH);
        audioPlayerManager.getConfiguration().setOpusEncodingQuality(10);
        audioPlayerManager.getConfiguration().setOutputFormat(audioDataFormat);

        registerSources();
    }
    private void registerSources() {
        if(!audioPlayerManager.getSourceManagers().isEmpty()){
            audioPlayerManager.getSourceManagers().clear();
        }
        Config config = WaterPlayer.config;
        if(!config.getString("YANDEX_MUSIC_TOKEN", "").isBlank()) audioPlayerManager.registerSourceManager(new YandexMusicSourceManager(config.getString("YANDEX_MUSIC_TOKEN", "")));
        if(!config.getString("FLOWERY_TTS_VOICE", "Alena").isBlank()) audioPlayerManager.registerSourceManager(new FloweryTTSSourceManager(config.getString("FLOWERY_TTS_VOICE", "Alena")));
        if(!config.getString("DEEZER_DECRYPTION_KEY", "").isBlank()) audioPlayerManager.registerSourceManager(new DeezerAudioSourceManager(config.getString("DEEZER_DECRYPTION_KEY", "")));
        if(!config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", "").isBlank() && !config.getString("APPLE_MUSIC_COUNTRY_CODE", "us").isBlank()) audioPlayerManager.registerSourceManager(new AppleMusicSourceManager(null, config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", ""), config.getString("APPLE_MUSIC_COUNTRY_CODE", "us"), audioPlayerManager));
        if(!config.getString("SPOTIFY_CLIENT_ID", "").isBlank() && !config.getString("SPOTIFY_CLIENT_SECRET", "").isBlank() && !config.getString("SPOTIFY_COUNTRY_CODE", "US").isBlank()) audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, config.getString("SPOTIFY_CLIENT_ID", ""), config.getString("SPOTIFY_CLIENT_SECRET", ""), config.getString("SPOTIFY_COUNTRY_CODE", "US"), audioPlayerManager));

        if(config.getBoolean("ENABLE_YOUTUBE", true)) {
            final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager();
            youtube.setPlaylistPageCount(100);
            audioPlayerManager.registerSourceManager(youtube);
        }
        if(config.getBoolean("ENABLE_SOUNDCLOUD", true)) audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        if(config.getBoolean("ENABLE_BANDCAMP", true)) audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        if(config.getBoolean("ENABLE_VIMEO", true)) audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        if(config.getBoolean("ENABLE_TWITCH", false)) audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        if(config.getBoolean("ENABLE_BEAM", true)) audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
    }
    //
    public AudioDataFormat getAudioDataFormat() {
        return audioDataFormat;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public TrackScheduler getTrackManager() {
        return trackManager;
    }

    public TrackSearch getTrackSearch() {
        return trackSearch;
    }

    public void startAudioOutput() {
        audioOutput.start();
    }

    public int getVolume() {
        return audioPlayer.getVolume();
    }
    ///

}
