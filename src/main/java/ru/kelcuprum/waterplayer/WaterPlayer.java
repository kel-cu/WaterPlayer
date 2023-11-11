package ru.kelcuprum.waterplayer;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
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
import ru.kelcuprum.waterplayer.api.MusicPlayer;
import ru.kelcuprum.waterplayer.command.WaterPlayerCommand;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.localization.Music;
import ru.kelcuprum.waterplayer.localization.StarScript;
import ru.kelcuprum.waterplayer.screens.LoadMusicScreen;
import ru.kelcuprum.waterplayer.screens.OverlayHandler;
import ru.kelcuprum.waterplayer.screens.PlaylistScreen;
import ru.kelcuprum.waterplayer.toasts.ControlToast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class WaterPlayer implements ClientModInitializer {
    public static Config config = new Config("config/WaterPlayer/config.json");
    private static final Timer TIMER = new Timer();
    public static final Logger LOG = LogManager.getLogger("WaterPlayer");
    public static boolean clothConfig = FabricLoader.getInstance().getModContainer("cloth-config").isPresent();
    public static MusicPlayer music;
    public static String mixer;
    private static String lastException;
    public static UUID bossBarUUID = UUID.randomUUID();
    private static boolean lastBossBar = true;
    public static boolean closing = true;
    public static String[] types = {
            "WaterPlayer",
            "BossBar",
            "None"
    };

    @Override
    public void onInitializeClient() {
        log("Hello, world! UwU");
        config.load();
        config.load();
        StarScript.init();
        music = new MusicPlayer();
        music.startAudioOutput();
        mixer = music.getMixer();
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
                int current = config.getInt("CURRENT_MUSIC_VOLUME", 3) + config.getInt("SELECT_MUSIC_VOLUME", 1);
                if (current >= 100) current = 100;
                config.setInt("CURRENT_MUSIC_VOLUME", current);
                music.getAudioPlayer().setVolume(current);
                config.save();
            }
            while (volumeMusicDownKey.consumeClick()) {
                int current = config.getInt("CURRENT_MUSIC_VOLUME", 3) - config.getInt("SELECT_MUSIC_VOLUME", 1);
                if (current <= 0) current = 0;
                config.setInt("CURRENT_MUSIC_VOLUME", current);
                music.getAudioPlayer().setVolume(current);
                config.save();
            }
            while (loadTrack.consumeClick()) {
                client.setScreen(new LoadMusicScreen(client.screen));
            }
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            closing = false;
            start();
            OverlayHandler hud = new OverlayHandler();
            HudRenderCallback.EVENT.register(hud);
            ClientTickEvents.START_CLIENT_TICK.register(hud);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            closing = true;
            music.getAudioPlayer().stopTrack();
        });
//        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
//            dispatcher.register(ClientCommandManager.literal("playlist")
//                    .then(
//                            argument("name", greedyString()).executes(context -> {
//                                if (!WaterPlayer.clothConfig) {
//                                    context.getSource().getPlayer().sendSystemMessage(Localization.getText(("waterplayer.message.clothConfigNotFound")));
//                                } else {
//                                    Minecraft client = context.getSource().getClient();
//                                    client.tell(() -> { client.setScreen(new PlaylistScreen().buildScreen(client.screen, getString(context, "name"))); });
//                                }
//                                return 1;
//                            })
//                    )
//            );
//        });
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
            }
        }, 250, 250);
    }

    public static LerpingBossEvent bossBar;
    public static void updateBossBar() {
        if(closing) return;
        if (!lastBossBar) lastBossBar = true;
        boolean playing = false;
        try {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.player != null) {
                if (WaterPlayer.music.getAudioPlayer().getPlayingTrack() != null) {
                    if (WaterPlayer.music.getAudioPlayer().isPaused()) {

                        bossBar = new LerpingBossEvent(WaterPlayer.bossBarUUID, Localization.toText(Localization.getLocalization("bossbar.pause", true, false)), 1F
                                , BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS, false, false, false);
                    } else {
                        if (WaterPlayer.music.getAudioPlayer().getPlayingTrack().getInfo().isStream)
                            bossBar = new LerpingBossEvent(WaterPlayer.bossBarUUID, Localization.toText(Localization.getLocalization(Music.isAuthorNull() ? "bossbar.live.withoutAuthor" : "bossbar.live", true, false)), (
                                    1F
                            ), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS, false, false, false);
                        else
                            bossBar = new LerpingBossEvent(WaterPlayer.bossBarUUID, Localization.toText(Localization.getLocalization(Music.isAuthorNull() ? "bossbar.withoutAuthor" : "bossbar", true, false)), (
                                    (float) WaterPlayer.music.getAudioPlayer().getPlayingTrack().getPosition() / WaterPlayer.music.getAudioPlayer().getPlayingTrack().getDuration()
                            ), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS, false, false, false);
                    }
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
                    if (WaterPlayer.music.getAudioPlayer().isPaused()) {
                        title = Localization.getLocalization("title.pause", true, false);
                    } else {
                        if (WaterPlayer.music.getAudioPlayer().getPlayingTrack().getInfo().isStream)
                            title = Localization.getLocalization(Music.isAuthorNull() ? "title.live.withoutAuthor" : "title.live", true, false);
                        else
                            title = Localization.getLocalization(Music.isAuthorNull() ? "title.withoutAuthor" : "title", true, false);
                    }
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


    public static void log(String message){log(message, Level.INFO);}
    public static void log(String message, Level level){
        LOG.log(level, "[" + LOG.getName() + "] " + message);
    }
}