package ru.kelcuprum.waterplayer.backend;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.flowerytts.FloweryTTSSourceManager;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.yandexmusic.YandexMusicSourceManager;
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
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
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

    public final LocalAudioSourceManager localAudioSourceManager = new LocalAudioSourceManager();

    public MusicPlayer() {
        audioPlayerManager = new DefaultAudioPlayerManager();
        audioDataFormat = new Pcm16AudioDataFormat(2, 48000, 960, true);
        audioPlayer = audioPlayerManager.createPlayer();
        audioOutput = new AudioOutput(this);

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
        if (!config.getString("YANDEX_MUSIC_TOKEN", "").isBlank())
            audioPlayerManager.registerSourceManager(new YandexMusicSourceManager(config.getString("YANDEX_MUSIC_TOKEN", "")));
        if (!config.getString("FLOWERY_TTS_VOICE", "Alena").isBlank())
            audioPlayerManager.registerSourceManager(new FloweryTTSSourceManager(config.getString("FLOWERY_TTS_VOICE", "Alena")));
        if (!config.getString("DEEZER_DECRYPTION_KEY", "").isBlank())
            audioPlayerManager.registerSourceManager(new DeezerAudioSourceManager(config.getString("DEEZER_DECRYPTION_KEY", "")));
        if (!config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", "").isBlank() && !config.getString("APPLE_MUSIC_COUNTRY_CODE", "us").isBlank())
            audioPlayerManager.registerSourceManager(new AppleMusicSourceManager(null, config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", ""), config.getString("APPLE_MUSIC_COUNTRY_CODE", "us"), audioPlayerManager));
        if (!config.getString("SPOTIFY_CLIENT_ID", "").isBlank() && !config.getString("SPOTIFY_CLIENT_SECRET", "").isBlank() && !config.getString("SPOTIFY_COUNTRY_CODE", "US").isBlank())
            audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, config.getString("SPOTIFY_CLIENT_ID", ""), config.getString("SPOTIFY_CLIENT_SECRET", ""), config.getString("SPOTIFY_COUNTRY_CODE", "US"), audioPlayerManager));

        if (config.getBoolean("ENABLE_YOUTUBE", true)) {
            final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager();
            youtube.setPlaylistPageCount(100);
            audioPlayerManager.registerSourceManager(youtube);
        }
        if (config.getBoolean("ENABLE_SOUNDCLOUD", true))
            audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        if (config.getBoolean("ENABLE_BANDCAMP", true))
            audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        if (config.getBoolean("ENABLE_VIMEO", true))
            audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        if (config.getBoolean("ENABLE_TWITCH", false))
            audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        if (config.getBoolean("ENABLE_BEAM", true))
            audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
        audioPlayerManager.registerSourceManager(localAudioSourceManager);
    }

    //
    public void loadMusic(String url, boolean isFirstLoadMusic) {
        if (url.isBlank()) {
            if (isFirstLoadMusic)
                WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add.blank")).show(WaterPlayer.MINECRAFT.getToasts());
            return;
        }
        if (isFirstLoadMusic) WaterPlayer.config.setString("LAST_REQUEST_MUSIC", url);
        File folder = new File(url);
        if (url.startsWith("playlist:")) {
            String name = url.replace("playlist:", "");
            Playlist playlist;
            JsonObject jsonPlaylist = new JsonObject();

            final Path configFile = WaterPlayer.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/" + name + ".json");
            try {
                jsonPlaylist = GsonHelper.parse(Files.readString(configFile));
            } catch (Exception ex) {
                WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
            }
            playlist = new Playlist(jsonPlaylist);
            for (int i = 0; i < playlist.urlsJSON.size(); i++) {
                loadMusic(playlist.urlsJSON.get(i).getAsString(), false);
            }
            if (isFirstLoadMusic) WaterPlayer.getToast().setMessage(Localization.toText(
                    Localization.getStringText("waterplayer.load.add.playlist")
                            .replace("%playlist_name%", playlist.title)
            )).show(WaterPlayer.MINECRAFT.getToasts());
        } else if (folder.exists() && folder.isDirectory()) {
            try {
                File[] list = folder.listFiles();
                assert list != null;

                for (File file : list) {
                    if (file.isFile()) getTracks(file.getPath());
                }
                if(isFirstLoadMusic) WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add.files")).show(WaterPlayer.MINECRAFT.getToasts());
            } catch (Exception e) {
                WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
            }
        } else {
            getTracks(url);
            if (isFirstLoadMusic)
                WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add")).show(WaterPlayer.MINECRAFT.getToasts());
        }
    }
    private void getTracks(String url) {
        audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                WaterPlayer.log("Add track: " + track.getInfo().title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                tracks.forEach(musicManager.scheduler::queue);
                WaterPlayer.log("Add playlist: " + playlist.getName() + ", tracks count: " + playlist.getTracks().size()
                );
            }

            @Override
            public void noMatches() {
                WaterPlayer.log("Nothing found by " + url, Level.ERROR);
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
