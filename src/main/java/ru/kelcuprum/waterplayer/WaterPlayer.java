package ru.kelcuprum.waterplayer;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.mojang.blaze3d.platform.InputConstants;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.waterplayer.backend.MusicPlayer;
import ru.kelcuprum.waterplayer.backend.command.WaterPlayerCommand;
import ru.kelcuprum.waterplayer.frontend.localization.Music;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.OverlayHandler;
import ru.kelcuprum.waterplayer.frontend.gui.toasts.ControlToast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class WaterPlayer implements ClientModInitializer {
    public static Config config = new Config("config/WaterPlayer/config.json");
    public static IPCClient client = new IPCClient(1197963953695903794L);
    private static final Timer TIMER = new Timer();
    public static final Logger LOG = LogManager.getLogger("WaterPlayer");
    public static boolean clothConfig = FabricLoader.getInstance().getModContainer("cloth-config").isPresent();
    public static MusicPlayer music;
    public static Localization localization = new Localization("waterplayer", "config/WaterPlayer/lang");
    public static String mixer;
    private static String lastException;
    public static UUID bossBarUUID = UUID.randomUUID();
    private static boolean lastBossBar = false;
    public static boolean closing = true;

    @Override
    public void onInitializeClient() {
        log("Hello, world! UwU");
        config.load();
        config.load();
        StarScript.init();
        localization.setParser((s) -> StarScript.run(StarScript.compile(s)));
        music = new MusicPlayer();
        music.startAudioOutput();
        mixer = music.getMixer();
        registerBinds();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            closing = false;
            start();
            OverlayHandler hud = new OverlayHandler();
            HudRenderCallback.EVENT.register(hud);
            ClientTickEvents.START_CLIENT_TICK.register(hud);
            registerApplications();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            closing = true;
            if(CONNECTED_DISCORD) client.close();
            music.getAudioPlayer().stopTrack();
        });
        ClientCommandRegistrationCallback.EVENT.register(WaterPlayerCommand::register);
    }

    // MUSIC
    public static void start() {
        TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(closing) return;
                if(WaterPlayer.config.getBoolean("ENABLE_CHANGE_TITLE", true) && WaterPlayer.music != null && music.getAudioPlayer().getPlayingTrack() != null) updateTitle();
                if (WaterPlayer.config.getBoolean("ENABLE_BOSS_BAR", false)) updateBossBar();
                else if (lastBossBar) clearBossBar();

                if (WaterPlayer.config.getBoolean("ENABLE_DISCORD_RPC", false)) updateDiscordPresence();
                else if (lastDiscord) clearDiscord();
            }
        }, 250, 250);
    }
    public static void registerBinds(){
        KeyMapping loadTrack = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.load",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_L, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping playOrPause = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.pause",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping skipTrack = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.skip",
                InputConstants.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_5, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping resetQueueKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.reset",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_1, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping shuffleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.shuffle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping repeatingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.repeating",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_2, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping volumeMusicUpKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.volume.up",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UP, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping volumeMusicDownKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.volume.down",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN, // The keycode of the key
                "waterplayer.name"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (playOrPause.consumeClick()) {
                music.getAudioPlayer().setPaused(!music.getAudioPlayer().isPaused());
                client.getToasts().addToast(new ControlToast(Localization.getText(music.getAudioPlayer().isPaused() ? "waterplayer.message.pause" : "waterplayer.message.play"), false));
            }
            while (repeatingKey.consumeClick()) {
                music.getTrackManager().setRepeating(!music.getTrackManager().isRepeating());
                client.getToasts().addToast(new ControlToast(Localization.getText(music.getTrackManager().isRepeating() ? "waterplayer.message.repeat" : "waterplayer.message.repeat.no"), false));
            }
            while (resetQueueKey.consumeClick()) {
                music.getTrackManager().skiping = false;
                if(!music.getTrackManager().queue.isEmpty()) {
                    music.getTrackManager().queue.clear();
                    client.getToasts().addToast(new ControlToast(Localization.getText("waterplayer.message.reset"), false));
                }
            }
            while (shuffleKey.consumeClick()) {
                if(music.getTrackManager().queue.size() >= 2){
                    music.getTrackManager().shuffle();
                    client.getToasts().addToast(new ControlToast(Localization.getText("waterplayer.message.shuffle"), false));
                }
            }
            while (skipTrack.consumeClick()) {
                if(music.getTrackManager().queue.isEmpty() && music.getAudioPlayer().getPlayingTrack() == null) return;
                music.getTrackManager().nextTrack();
                client.getToasts().addToast(new ControlToast(Localization.getText("waterplayer.message.skip"), false));
            }
            while (volumeMusicUpKey.consumeClick()) {
                int current = config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue() + config.getNumber("SELECT_MUSIC_VOLUME", 1).intValue();
                if (current >= 100) current = 100;
                config.setNumber("CURRENT_MUSIC_VOLUME", current);
                music.getAudioPlayer().setVolume(current);
                config.save();
            }
            while (volumeMusicDownKey.consumeClick()) {
                int current = config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue() - config.getNumber("SELECT_MUSIC_VOLUME", 1).intValue();
                if (current <= 0) current = 0;
                config.setNumber("CURRENT_MUSIC_VOLUME", current);
                music.getAudioPlayer().setVolume(current);
                config.save();
            }
            while (loadTrack.consumeClick()) {
                client.setScreen(new LoadMusicScreen(client.screen));
            }
        });
    }
    // Minecraft
    public static LerpingBossEvent bossBar;
    public static void updateBossBar() {
        if(closing) return;
        if (!lastBossBar) lastBossBar = true;
        boolean playing = false;
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.player != null) {
                if (WaterPlayer.music.getAudioPlayer().getPlayingTrack() != null) {
                    boolean isPause = WaterPlayer.music.getAudioPlayer().isPaused();
                    boolean isStream = WaterPlayer.music.getAudioPlayer().getPlayingTrack().getInfo().isStream;
                    bossBar = new LerpingBossEvent(WaterPlayer.bossBarUUID,
                            Localization.toText(localization.getLocalization(isPause ? "bossbar.pause" : isStream ?
                                    (Music.isAuthorNull() ? "bossbar.live.withoutAuthor" : "bossbar.live") : (Music.isAuthorNull() ? "bossbar.withoutAuthor" : "bossbar"))),
                            (isPause || isStream) ? 1F : (float) WaterPlayer.music.getAudioPlayer().getPlayingTrack().getPosition() / WaterPlayer.music.getAudioPlayer().getPlayingTrack().getDuration()
                            , isPause ? BossEvent.BossBarColor.YELLOW : isStream ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS, false, false, false);
                    playing = true;
                }
                if(playing) client.gui.getBossOverlay().update(ClientboundBossEventPacket.createAddPacket(bossBar));
                else client.gui.getBossOverlay().update(ClientboundBossEventPacket.createRemovePacket(bossBarUUID));
            }
            if (lastException != null) lastException = null;
        } catch (Exception ex) {
            if (lastException == null || !lastException.equals(ex.getMessage())) {
                ex.printStackTrace();
                lastException = ex.getMessage();
            }
        }
    }
    public static void updateTitle() {
        if(closing) return;
        String title;
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.level == null && client.player == null) {
                if (WaterPlayer.music.getAudioPlayer().getPlayingTrack() != null) {
                    title = localization.getLocalization(
                            WaterPlayer.music.getAudioPlayer().isPaused() ? "title.pause"
                                    : WaterPlayer.music.getAudioPlayer().getPlayingTrack().getInfo().isStream ? (Music.isAuthorNull() ? "title.live.withoutAuthor"
                                    : "title.live") : (Music.isAuthorNull() ? "title.withoutAuthor" : "title"));

                    client.getWindow().setTitle(title);
                }
            }
            if (lastException != null) lastException = null;
        } catch (Exception ex) {
            if (lastException == null || !lastException.equals(ex.getMessage())) {
                ex.printStackTrace();
                lastException = ex.getMessage();
            }
        }
    }

    public static void clearBossBar() {
        try {
            if (lastBossBar) lastBossBar = false;
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.player != null) {
                client.gui.getBossOverlay().update(ClientboundBossEventPacket.createRemovePacket(bossBarUUID));
            }
            if (lastException != null) lastException = null;
        } catch (Exception ex) {
            if (lastException == null || !lastException.equals(ex.getMessage())) {
                ex.printStackTrace();
                lastException = ex.getMessage();
            }
        }
    }
    // Discord
    private static boolean lastDiscord = false;
    public static User USER;
    public static boolean CONNECTED_DISCORD = false;
    public static String lastTrack = "";
    public static boolean lastClearDiscord = false;
    private void registerApplications(){
        setupListener();
    }
    public static void setupListener(){
        client.setListener(new IPCListener(){
            @Override
            public void onPacketSent(IPCClient ipcClient, Packet packet) {

            }

            @Override
            public void onPacketReceived(IPCClient ipcClient, Packet packet) {

            }

            @Override
            public void onActivityJoin(IPCClient ipcClient, String s) {

            }

            @Override
            public void onActivitySpectate(IPCClient ipcClient, String s) {

            }

            @Override
            public void onActivityJoinRequest(IPCClient ipcClient, String s, User user) {

            }

            @Override
            public void onReady(IPCClient client)
            {
                log("The mod has been connected to Discord", Level.DEBUG);
                USER = client.getCurrentUser();
                CONNECTED_DISCORD = true;
            }

            @Override
            public void onClose(IPCClient ipcClient, JsonObject jsonObject) {
                CONNECTED_DISCORD = false;
            }

            @Override
            public void onDisconnect(IPCClient ipcClient, Throwable throwable) {
                log("The mod has been pulled from Discord", Level.DEBUG);
                log(String.format("Reason: %s", throwable.getLocalizedMessage()), Level.DEBUG);
                CONNECTED_DISCORD = false;
            }
        });
    }
    public static void clearDiscord() {
        try {
            if (lastDiscord) lastDiscord = false;
            client.sendRichPresence(null);
            client.close();
            if (lastException != null) lastException = null;
        } catch (Exception ex) {
            if (lastException == null || !lastException.equals(ex.getMessage())) {
                ex.printStackTrace();
                lastException = ex.getMessage();
            }
        }
    }
    public static void updateDiscordPresence(){
        if(!lastDiscord){
            try {
                client.connect();
                lastDiscord = true;
            } catch (Exception ex){
                log(ex.getLocalizedMessage(), Level.ERROR);
            }
        }
        RichPresence.Builder rich = null;
        if(WaterPlayer.music.getAudioPlayer().getPlayingTrack() != null && !WaterPlayer.music.getAudioPlayer().isPaused()){
            if(lastClearDiscord) lastClearDiscord = false;
            AudioTrackInfo trackInfo = WaterPlayer.music.getAudioPlayer().getPlayingTrack().getInfo();
            AudioTrack track = WaterPlayer.music.getAudioPlayer().getPlayingTrack();
            if(trackInfo.uri.equals(lastTrack)) return;
            else lastTrack = trackInfo.uri;

            rich = new RichPresence.Builder()
                    .setDetails(trackInfo.author)
                    .setState(trackInfo.title)
                    .setLargeImage(trackInfo.artworkUrl == null ? "https://cdn.kelcuprum.ru/icons/music.png" : trackInfo.artworkUrl);
            if(trackInfo.isStream) rich.setStartTimestamp(System.currentTimeMillis()-track.getPosition());
            else rich.setStartTimestamp(System.currentTimeMillis()-track.getPosition())
                    .setEndTimestamp(System.currentTimeMillis()+(track.getDuration()- track.getPosition()));
            if(CONNECTED_DISCORD) client.sendRichPresence(rich.build());
        } else if(!lastClearDiscord){
            lastClearDiscord = true;
            lastTrack = "";
            if(CONNECTED_DISCORD) client.sendRichPresence(null);
        }
    }

    // Logger
    public static void log(String message){log(message, Level.INFO);}
    public static void log(String message, Level level){
        LOG.log(level, "[" + LOG.getName() + "] " + message);
    }
}