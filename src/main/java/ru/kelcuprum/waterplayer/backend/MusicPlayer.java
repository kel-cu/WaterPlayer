package ru.kelcuprum.waterplayer.backend;

import com.github.topi314.lavalyrics.AudioLyricsManager;
import com.github.topi314.lavalyrics.LyricsManager;
import com.github.topi314.lavasearch.AudioSearchManager;
import com.github.topi314.lavasearch.SearchManager;
import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.flowerytts.FloweryTTSSourceManager;
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.yandexmusic.YandexMusicSourceManager;
import com.github.topi314.lavasrc.youtube.YoutubeSearchManager;
import com.google.gson.JsonObject;
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
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.AndroidWithThumbnail;
import dev.lavalink.youtube.clients.MusicWithThumbnail;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.output.AudioOutput;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MusicPlayer {

    private final AudioPlayerManager audioPlayerManager;
    private final AudioDataFormat audioDataFormat;
    private final AudioPlayer audioPlayer;
    private final AudioOutput audioOutput;

    private final TrackScheduler trackScheduler;
    private final MusicManager musicManager;
    private final LyricsManager lyricsManager;

    public final LocalAudioSourceManager localAudioSourceManager = new LocalAudioSourceManager();

    public MusicPlayer() {
        audioPlayerManager = new DefaultAudioPlayerManager();
        audioDataFormat = new Pcm16AudioDataFormat(2, 48000, 960, true);
        audioPlayer = audioPlayerManager.createPlayer();
        audioOutput = new AudioOutput(this);
        lyricsManager = new LyricsManager();

        trackScheduler = new TrackScheduler(audioPlayer);
        musicManager = new MusicManager(audioPlayer, trackScheduler);
        audioPlayer.setVolume(WaterPlayer.config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue());

        audioPlayerManager.setFrameBufferDuration(1000);
        audioPlayerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);

        audioPlayerManager.getConfiguration().setResamplingQuality(ResamplingQuality.HIGH);
        audioPlayerManager.getConfiguration().setOpusEncodingQuality(10);
        audioPlayerManager.getConfiguration().setOutputFormat(audioDataFormat);

        registerSources();
    }

    private void registerSources() {
        Config config = WaterPlayer.config;
        if (config.getBoolean("ENABLE_YOUTUBE", true)) {
            final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true, new MusicWithThumbnail(), new WebWithThumbnail(), new AndroidWithThumbnail());
            youtube.setPlaylistPageCount(100);
            audioPlayerManager.registerSourceManager(youtube);
            AudioSearchManager ytSearch = new YoutubeSearchManager(() -> audioPlayerManager, "US");
            lyricsManager.registerLyricsManager((AudioLyricsManager) ytSearch);
        }
        if (!config.getString("YANDEX_MUSIC_TOKEN", "").isBlank()) {
            YandexMusicSourceManager ym = new YandexMusicSourceManager(config.getString("YANDEX_MUSIC_TOKEN", ""));
            audioPlayerManager.registerSourceManager(ym);
            lyricsManager.registerLyricsManager(ym);
        }
        if (!config.getString("FLOWERY_TTS_VOICE", "Alena").isBlank())
            audioPlayerManager.registerSourceManager(new FloweryTTSSourceManager(config.getString("FLOWERY_TTS_VOICE", "Alena")));
        if (!config.getString("DEEZER_DECRYPTION_KEY", "").isBlank()) {
            DeezerAudioSourceManager deezerAudioSourceManager = new DeezerAudioSourceManager(config.getString("DEEZER_DECRYPTION_KEY", ""));
            audioPlayerManager.registerSourceManager(deezerAudioSourceManager);
            lyricsManager.registerLyricsManager(deezerAudioSourceManager);
        }
        if (!config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", "").isBlank() && !config.getString("APPLE_MUSIC_COUNTRY_CODE", "us").isBlank()) {
            AppleMusicSourceManager appleMusicSourceManager = new AppleMusicSourceManager(null, config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", ""), config.getString("APPLE_MUSIC_COUNTRY_CODE", "us"), audioPlayerManager);
            audioPlayerManager.registerSourceManager(appleMusicSourceManager);
        }
        if (!config.getString("SPOTIFY_CLIENT_ID", "").isBlank() && !config.getString("SPOTIFY_CLIENT_SECRET", "").isBlank() && !config.getString("SPOTIFY_COUNTRY_CODE", "US").isBlank()) {
            SpotifySourceManager spotifySourceManager;
            if(config.getString("SPOTIFY_SP_DC", "").isBlank()) spotifySourceManager = new SpotifySourceManager(null, config.getString("SPOTIFY_CLIENT_ID", ""), config.getString("SPOTIFY_CLIENT_SECRET", ""), config.getString("SPOTIFY_COUNTRY_CODE", "US"), audioPlayerManager);
            else {
                spotifySourceManager = new SpotifySourceManager(config.getString("SPOTIFY_CLIENT_ID", ""), config.getString("SPOTIFY_CLIENT_SECRET", ""), config.getString("SPOTIFY_SP_DC", ""), config.getString("SPOTIFY_COUNTRY_CODE", "US"), unused -> audioPlayerManager, new DefaultMirroringAudioTrackResolver(null));
                lyricsManager.registerLyricsManager(spotifySourceManager);
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
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
        audioPlayerManager.registerSourceManager(localAudioSourceManager);
    }

    //
    public void loadMusic(String url, boolean isFirstLoadMusic) {
        if (url.isBlank()) {
            if (isFirstLoadMusic)
                WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add.blank")).show(AlinLib.MINECRAFT.getToasts());
            return;
        }
        if (isFirstLoadMusic) WaterPlayer.config.setString("LAST_REQUEST_MUSIC", url);
        File file = new File(url);
        if (url.startsWith("playlist:") || (file.exists() && file.isFile() && file.getName().endsWith(".json"))) {
            String name = url.replace("playlist:", "");
            Playlist playlist;
            if (file.exists()) {
                try {
                    playlist = new Playlist(file.toPath());
                } catch (Exception exception) {
                    WaterPlayer.log(exception.getMessage(), Level.ERROR);
                    return;
                }
            } else {
                JsonObject jsonPlaylist = new JsonObject();

                final Path configFile = AlinLib.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/" + name + ".json");
                try {
                    jsonPlaylist = GsonHelper.parse(Files.readString(configFile));
                } catch (Exception ex) {
                    WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
                }
                playlist = new Playlist(jsonPlaylist);
            }
            for (int i = 0; i < playlist.urlsJSON.size(); i++) {
                loadMusic(playlist.urlsJSON.get(i).getAsString(), false);
            }
            if (isFirstLoadMusic) WaterPlayer.getToast().setMessage(Localization.toText(
                    Localization.getStringText("waterplayer.load.add.playlist")
                            .replace("%playlist_name%", playlist.title)
            )).show(AlinLib.MINECRAFT.getToasts());
        } else if (file.exists() && file.isDirectory()) {
            try {
                File[] list = file.listFiles();
                assert list != null;

                for (File track : list) {
                    if (track.isFile()) getTracks(track.getPath());
                }
                if (isFirstLoadMusic)
                    WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add.files")).show(AlinLib.MINECRAFT.getToasts());
            } catch (Exception e) {
                WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
            }
        } else {
            getTracks(url);
            if (isFirstLoadMusic)
                WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add")).show(AlinLib.MINECRAFT.getToasts());
        }
    }

    private void getTracks(String url) {
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
                WaterPlayer.log("Adding Playlist: " + playlist.getName() + ". Tracks Count: " + playlist.getTracks().size()
                );
            }

            @Override
            public void noMatches() {
                WaterPlayer.log("Nothing Found by " + url, Level.ERROR);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                WaterPlayer.log(exception.getMessage(), Level.ERROR);
            }
        });
    }

    //
    public AudioDataFormat getAudioDataFormat() {
        return audioDataFormat;
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

    public void startAudioOutput() {
        audioOutput.start();
    }

    public int getVolume() {
        return audioPlayer.getVolume();
    }
    ///

}
