package ru.kelcuprum.waterplayer.backend;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.github.topi314.lavalyrics.AudioLyricsManager;
import com.github.topi314.lavalyrics.LyricsManager;
import com.github.topi314.lavasearch.AudioSearchManager;
import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.flowerytts.FloweryTTSSourceManager;
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.vkmusic.VkMusicSourceManager;
import com.github.topi314.lavasrc.yandexmusic.YandexMusicSourceManager;
import com.github.topi314.lavasrc.youtube.YoutubeSearchManager;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
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
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AllocatingAudioFrameBuffer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.output.AudioOutput;
import ru.kelcuprum.waterplayer.backend.sources.directory.DirectoriesSource;
import ru.kelcuprum.waterplayer.backend.sources.waterplayer.LyricsWithoutException;
import ru.kelcuprum.waterplayer.backend.sources.waterplayer.WaterPlayerSource;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MusicPlayer {

    private final AudioPlayerManager audioPlayerManager;
    private final AudioDataFormat audioDataFormat;
    private final AudioPlayer audioPlayer;
    private final AudioOutput audioOutput;

    private final TrackScheduler trackScheduler;
    private final MusicManager musicManager;
    private final LyricsManager lyricsManager;

    public final LocalAudioSourceManager localAudioSourceManager = new LocalAudioSourceManager();
    public double speed = WaterPlayer.config.getNumber("CURRENT_MUSIC_SPEED", 1).doubleValue();
    public double pitch = WaterPlayer.config.getNumber("CURRENT_MUSIC_PITCH", 1).doubleValue();

    public MusicPlayer() {
        audioPlayerManager = new DefaultAudioPlayerManager();
        audioDataFormat = new Pcm16AudioDataFormat(2, 48000, 960, true);
        audioPlayer = audioPlayerManager.createPlayer();
        audioOutput = new AudioOutput(this);
        lyricsManager = new LyricsManager();

        trackScheduler = new TrackScheduler(audioPlayer);
        musicManager = new MusicManager(audioPlayer, trackScheduler);
        audioPlayer.setVolume(WaterPlayer.config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue());
        if(WaterPlayer.config.getBoolean("EXPERIMENT.FILTERS", false)) {
            audioPlayer.setFilterFactory(((track, format, output) -> {
                final TimescalePcmAudioFilter filter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
                filter.setSpeed(speed);
                filter.setPitch(pitch);
                return Collections.singletonList(filter);
            }));
        }

        audioPlayerManager.getConfiguration().setFrameBufferFactory((bufferDuration, format, stopping) -> new AllocatingAudioFrameBuffer(bufferDuration, format, stopping) {
            @Override
            public AudioFrame provide() {
                AudioFrame frame = super.provide();
                if (frame != null && !frame.isTerminator()) TrackScheduler.trackPosition += (long) (frame.getFormat().frameDuration() * TrackScheduler.trackSpeed);
                return frame;
            }

            @Override
            public AudioFrame provide(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
                AudioFrame frame = super.provide(timeout, unit);
                if (frame != null && !frame.isTerminator()) TrackScheduler.trackPosition += (long) (frame.getFormat().frameDuration() * TrackScheduler.trackSpeed);
                return frame;
            }
        });

        audioPlayerManager.setFrameBufferDuration(1000);
        audioPlayerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);

        audioPlayerManager.getConfiguration().setResamplingQuality(ResamplingQuality.HIGH);
        audioPlayerManager.getConfiguration().setOpusEncodingQuality(10);
        audioPlayerManager.getConfiguration().setOutputFormat(audioDataFormat);

        registerSources();
    }
    public void updateFilter(){
        if(!WaterPlayer.config.getBoolean("EXPERIMENT.FILTERS", false)) return;
        audioPlayer.setFilterFactory((track, format, output) -> {
            final TimescalePcmAudioFilter filter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
            filter.setSpeed(speed);
            filter.setPitch(pitch);
            return Collections.singletonList(filter);
        });
    }

    private void registerSources() {
        Config config = WaterPlayer.config;
        WaterPlayerSource wps = new WaterPlayerSource();
        lyricsManager.registerLyricsManager(wps);

        if (config.getBoolean("ENABLE_YOUTUBE", true)) {
            final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true, new MusicWithThumbnail(), new WebWithThumbnail(), new TvHtml5EmbeddedWithThumbnail());
            youtube.setPlaylistPageCount(100);
            audioPlayerManager.registerSourceManager(youtube);
            AudioSearchManager ytSearch = new YoutubeSearchManager(() -> audioPlayerManager, "US");
            lyricsManager.registerLyricsManager(new LyricsWithoutException((AudioLyricsManager) ytSearch));
        }
        if (!config.getString("VK_MUSIC_TOKEN", "").isBlank() && config.getBoolean("ENABLE_VK_MUSIC", false)) {
            //#if WALTER == 1
            //$$ config.setBoolean("ENABLE_VK_MUSIC", false);
            //$$ Util.getPlatform().openUri("https://www.youtube.com/watch?v=PkT0PJwy8mI");
            //$$ throw new RuntimeException("Don't use VK Group products, please, I don't want you to eat shit.");
            //#else
            VkMusicSourceManager vk = new VkMusicSourceManager(config.getString("VK_MUSIC_TOKEN", ""));
            audioPlayerManager.registerSourceManager(vk);
            lyricsManager.registerLyricsManager(new LyricsWithoutException(vk));
            //#endif
        }
        if (!config.getString("YANDEX_MUSIC_TOKEN", "").isBlank()) {
            YandexMusicSourceManager ym = new YandexMusicSourceManager(config.getString("YANDEX_MUSIC_TOKEN", ""));
            audioPlayerManager.registerSourceManager(ym);
            lyricsManager.registerLyricsManager(new LyricsWithoutException(ym));
        }
        if (!config.getString("FLOWERY_TTS_VOICE", "Alena").isBlank())
            audioPlayerManager.registerSourceManager(new FloweryTTSSourceManager(config.getString("FLOWERY_TTS_VOICE", "Alena")));
        if (!config.getString("DEEZER_DECRYPTION_KEY", "").isBlank()) {
            DeezerAudioSourceManager deezerAudioSourceManager = new DeezerAudioSourceManager(config.getString("DEEZER_DECRYPTION_KEY", ""));
            audioPlayerManager.registerSourceManager(deezerAudioSourceManager);
            lyricsManager.registerLyricsManager(new LyricsWithoutException(deezerAudioSourceManager));
        }
        if (!config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", "").isBlank() && !config.getString("APPLE_MUSIC_COUNTRY_CODE", "us").isBlank()) {
            AppleMusicSourceManager appleMusicSourceManager = new AppleMusicSourceManager(null, config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", ""), config.getString("APPLE_MUSIC_COUNTRY_CODE", "us"), audioPlayerManager);
            audioPlayerManager.registerSourceManager(appleMusicSourceManager);
        }
        if (!config.getString("SPOTIFY_CLIENT_ID", "").isBlank() && !config.getString("SPOTIFY_CLIENT_SECRET", "").isBlank() && !config.getString("SPOTIFY_COUNTRY_CODE", "US").isBlank()) {
            SpotifySourceManager spotifySourceManager;
            if (config.getString("SPOTIFY_SP_DC", "").isBlank())
                spotifySourceManager = new SpotifySourceManager(null, config.getString("SPOTIFY_CLIENT_ID", ""), config.getString("SPOTIFY_CLIENT_SECRET", ""), config.getString("SPOTIFY_COUNTRY_CODE", "US"), audioPlayerManager);
            else {
                spotifySourceManager = new SpotifySourceManager(config.getString("SPOTIFY_CLIENT_ID", ""), config.getString("SPOTIFY_CLIENT_SECRET", ""), config.getString("SPOTIFY_SP_DC", ""), config.getString("SPOTIFY_COUNTRY_CODE", "US"), unused -> audioPlayerManager, new DefaultMirroringAudioTrackResolver(null));
                lyricsManager.registerLyricsManager(new LyricsWithoutException(spotifySourceManager));
            }
            audioPlayerManager.registerSourceManager(spotifySourceManager);
        }
        if (config.getBoolean("ENABLE_SOUNDCLOUD", true)) {
            SoundCloudAudioSourceManager soundCloudAudioSourceManager = SoundCloudAudioSourceManager.createDefault();
            audioPlayerManager.registerSourceManager(soundCloudAudioSourceManager);
        }
        if (config.getBoolean("ENABLE_BANDCAMP", true)) {
            BandcampAudioSourceManager bandcampAudioSourceManager = new BandcampAudioSourceManager();
            audioPlayerManager.registerSourceManager(bandcampAudioSourceManager);
        }
        if (config.getBoolean("ENABLE_VIMEO", true)) {
            VimeoAudioSourceManager vimeoAudioSourceManager = new VimeoAudioSourceManager();
            audioPlayerManager.registerSourceManager(vimeoAudioSourceManager);
        }
        if (config.getBoolean("ENABLE_TWITCH", false)) {
            TwitchStreamAudioSourceManager twitchStreamAudioSourceManager = new TwitchStreamAudioSourceManager();
            audioPlayerManager.registerSourceManager(twitchStreamAudioSourceManager);
        }
        if (config.getBoolean("ENABLE_BEAM", true)) {
            BeamAudioSourceManager beamAudioSourceManager = new BeamAudioSourceManager();
            audioPlayerManager.registerSourceManager(beamAudioSourceManager);
        }
        audioPlayerManager.registerSourceManager(new DirectoriesSource());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
        audioPlayerManager.registerSourceManager(localAudioSourceManager);
        audioPlayerManager.registerSourceManager(wps);
    }
    public void setPosition(long position){
        if(getAudioPlayer().getPlayingTrack() == null) return;
        TrackScheduler.trackPosition = position;
        getAudioPlayer().getPlayingTrack().setPosition(position);
    }

    //
    public void loadMusic(String url, boolean isFirstLoadMusic) {
        if (url.isBlank()) {
            if (isFirstLoadMusic)
                WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add.blank")).buildAndShow();
            return;
        }
        if (isFirstLoadMusic) WaterPlayer.config.setString("LAST_REQUEST_MUSIC", url);
        loadTracks(url);
        if (isFirstLoadMusic)
            WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add")).buildAndShow();
    }

    private void loadTracks(String url) {
        url = url.replace("\\", "/");
        String finalUrl = url;
        audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                WaterPlayer.log("Adding Track: " + track.getInfo().title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                tracks.forEach(musicManager.scheduler::queue);
                WaterPlayer.log("Adding Playlist: " + playlist.getName() + ". Tracks Count: " + playlist.getTracks().size());
            }

            @Override
            public void noMatches() {
                WaterPlayer.log("Nothing Found by " + finalUrl, Level.WARN);
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
            }
        });
    }

    //
    public AudioDataFormat getAudioDataFormat() {
        return audioDataFormat;
    }
    public boolean isPaused(){
        return audioPlayer.isPaused();
    }
    public boolean changePaused(){
        return changePaused(!isPaused());
    }
    public boolean changePaused(boolean pause){
        audioPlayer.setPaused(pause);
        return isPaused();
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public LyricsManager getLyricsManager() {
        return lyricsManager;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }
    public AudioOutput getAudioOutput() {
        return audioOutput;
    }

    public void startAudioOutput() {
        audioOutput.start();
    }

    public int getVolume() {
        return audioPlayer.getVolume();
    }
    ///

}
