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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.backend.MusicPlayer;
import ru.kelcuprum.waterplayer.backend.command.WaterPlayerCommand;
import ru.kelcuprum.waterplayer.frontend.localization.Music;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;
import ru.kelcuprum.waterplayer.frontend.gui.overlays.OverlayHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class WaterPlayer implements ClientModInitializer {
    public static Config config = new Config("config/WaterPlayer/config.json");
    public static IPCClient client = new IPCClient(1197963953695903794L);
    private static final Timer TIMER = new Timer();
    public static final Logger LOG = LogManager.getLogger("WaterPlayer");
    public static MusicPlayer player;
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
        StarScript.init();
        localization.setParser((s) -> StarScript.run(StarScript.compile(s)));
        player = new MusicPlayer();
        player.startAudioOutput();
        mixer = player.getMixer();
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
            player.getAudioPlayer().stopTrack();
        });
        ClientCommandRegistrationCallback.EVENT.register(WaterPlayerCommand::register);
    }

    // MUSIC
    public static void start() {
        TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(closing) return;
                if(WaterPlayer.config.getBoolean("ENABLE_CHANGE_TITLE", true) && WaterPlayer.player != null && player.getAudioPlayer().getPlayingTrack() != null) updateTitle();
                if (WaterPlayer.config.getBoolean("ENABLE_BOSS_BAR", false)) updateBossBar();
                else if (lastBossBar) clearBossBar();

                if (WaterPlayer.config.getBoolean("ENABLE_DISCORD_RPC", false)) updateDiscordPresence();
                else if (lastDiscord) clearDiscord();
            }
        }, 250, 250);
    }
    public static ToastBuilder getToast(){
        return new ToastBuilder().setIcon(Items.MUSIC_DISC_STRAD).setTitle(Component.translatable("waterplayer.name"));
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
                player.getAudioPlayer().setPaused(!player.getAudioPlayer().isPaused());
                getToast().setMessage(Localization.getText(player.getAudioPlayer().isPaused() ? "waterplayer.message.pause" : "waterplayer.message.play"))
                        .show(client.getToasts());
            }
            while (repeatingKey.consumeClick()) {
                player.getTrackManager().setRepeating(!player.getTrackManager().isRepeating());
                getToast().setMessage(Localization.getText(player.getTrackManager().isRepeating() ? "waterplayer.message.repeat" : "waterplayer.message.repeat.no"))
                        .show(client.getToasts());
            }
            while (resetQueueKey.consumeClick()) {
                player.getTrackManager().skiping = false;
                if(!player.getTrackManager().queue.isEmpty()) {
                    player.getTrackManager().queue.clear();
                    getToast().setMessage(Localization.getText("waterplayer.message.reset"))
                            .show(client.getToasts());
                }
            }
            while (shuffleKey.consumeClick()) {
                if(player.getTrackManager().queue.size() >= 2){
                    player.getTrackManager().shuffle();
                    getToast().setMessage(Localization.getText("waterplayer.message.shuffle"))
                            .show(client.getToasts());
                }
            }
            while (skipTrack.consumeClick()) {
                if(player.getTrackManager().queue.isEmpty() && player.getAudioPlayer().getPlayingTrack() == null) return;
                player.getTrackManager().nextTrack();
                getToast().setMessage(Localization.getText("waterplayer.message.skip"))
                        .show(client.getToasts());
            }
            while (volumeMusicUpKey.consumeClick()) {
                int current = config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue() + config.getNumber("SELECT_MUSIC_VOLUME", 1).intValue();
                if (current >= 100) current = 100;
                config.setNumber("CURRENT_MUSIC_VOLUME", current);
                player.getAudioPlayer().setVolume(current);
                config.save();
            }
            while (volumeMusicDownKey.consumeClick()) {
                int current = config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue() - config.getNumber("SELECT_MUSIC_VOLUME", 1).intValue();
                if (current <= 0) current = 0;
                config.setNumber("CURRENT_MUSIC_VOLUME", current);
                player.getAudioPlayer().setVolume(current);
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
                if (WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null) {
                    boolean isPause = WaterPlayer.player.getAudioPlayer().isPaused();
                    boolean isStream = WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().isStream;
                    bossBar = new LerpingBossEvent(WaterPlayer.bossBarUUID,
                            Localization.toText(localization.getLocalization(isPause ? "bossbar.pause" : isStream ?
                                    (Music.isAuthorNull() ? "bossbar.live.withoutAuthor" : "bossbar.live") : (Music.isAuthorNull() ? "bossbar.withoutAuthor" : "bossbar"))),
                            (isPause || isStream) ? 1F : (float) WaterPlayer.player.getAudioPlayer().getPlayingTrack().getPosition() / WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration()
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
                if (WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null) {
                    title = localization.getLocalization(
                            WaterPlayer.player.getAudioPlayer().isPaused() ? "title.pause"
                                    : WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().isStream ? (Music.isAuthorNull() ? "title.live.withoutAuthor"
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
        if(WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null && !WaterPlayer.player.getAudioPlayer().isPaused()){
            if(lastClearDiscord) lastClearDiscord = false;
            AudioTrackInfo trackInfo = WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo();
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            if(trackInfo.uri.equals(lastTrack)) return;
            else lastTrack = trackInfo.uri;

            rich = new RichPresence.Builder()
                    .setState(Music.getTitle())
                    .setLargeImage(trackInfo.artworkUrl == null ? "https://cdn.kelcuprum.ru/icons/music.png" : trackInfo.artworkUrl);
            if(!Music.isAuthorNull()) rich.setDetails(trackInfo.author);
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