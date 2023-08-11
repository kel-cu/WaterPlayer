package ru.kelcuprum.waterplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.waterplayer.config.Localization;
import ru.kelcuprum.waterplayer.config.UserConfig;
import ru.kelcuprum.waterplayer.config.Utils;
import ru.kelcuprum.waterplayer.screens.MusicScreen;
import ru.kelcuprum.waterplayer.screens.PlaylistScreen;

import java.time.chrono.MinguoEra;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class Client implements ClientModInitializer {
    private static final Timer TIMER = new Timer();
    public static final Logger LOG = LogManager.getLogger("WaterPlayer");
    public static boolean clothConfig = FabricLoader.getInstance().getModContainer("cloth-config").isPresent();
    public static MusicPlayer music;
    public static String mixer;
    private static String lastException;
    public static UUID bossBarUUID = UUID.randomUUID();
    private static boolean lastBossBar = true;
    @Override
    public void onInitializeClient() {
        log("Hello, world! UwU");
        UserConfig.load();
        music = new MusicPlayer();
        music.startAudioOutput();
        mixer = music.getMixer();
        KeyBinding loadTrack = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.load",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L, // The keycode of the key
                "waterplayer.name"
        ));
        KeyBinding playOrPause = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.pause",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P, // The keycode of the key
                "waterplayer.name"
        ));
        KeyBinding skipTrack = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.skip",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_5, // The keycode of the key
                "waterplayer.name"
        ));
        KeyBinding resetQueueKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.reset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_1, // The keycode of the key
                "waterplayer.name"
        ));
        KeyBinding shuffleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.shuffle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z, // The keycode of the key
                "waterplayer.name"
        ));
        KeyBinding repeatingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.repeating",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_2, // The keycode of the key
                "waterplayer.name"
        ));
        KeyBinding volumeMusicUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.volume.up",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT, // The keycode of the key
                "waterplayer.name"
        ));
        KeyBinding volumeMusicDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "waterplayer.key.volume.down",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT, // The keycode of the key
                "waterplayer.name"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            UserConfig.load();

//            log("tick");
            while (playOrPause.wasPressed()) {
                music.getAudioPlayer().setPaused(!music.getAudioPlayer().isPaused());
                if (client.player != null)
                    client.player.sendMessage(Localization.getText(music.getAudioPlayer().isPaused() ? "waterplayer.message.pause" : "waterplayer.message.play"));
            }
            while (repeatingKey.wasPressed()) {
                music.getTrackManager().setRepeating(!music.getTrackManager().isRepeating());
                if (client.player != null)
                    client.player.sendMessage(Localization.getText(music.getTrackManager().isRepeating() ? "waterplayer.message.repeat" : "waterplayer.message.repeat.no"));
            }
            while (resetQueueKey.wasPressed()) {
                music.getTrackManager().skiping = false;
                if(music.getTrackManager().queue.size() != 0) {
                    music.getTrackManager().queue.clear();
                    if (client.player != null)
                        client.player.sendMessage(Localization.getText("waterplayer.message.reset"));
                }
            }
            while (shuffleKey.wasPressed()) {
                if(music.getTrackManager().queue.size() >= 2){
                    music.getTrackManager().shuffle();
                    if (client.player != null)
                        client.player.sendMessage(Localization.getText("waterplayer.message.shuffle"));
                }
            }
            while (skipTrack.wasPressed()) {
                music.getTrackManager().nextTrack();
                if (client.player != null) client.player.sendMessage(Localization.getText("waterplayer.message.skip"));
            }
            while (volumeMusicUpKey.wasPressed()) {
                int current = UserConfig.CURRENT_MUSIC_VOLUME + UserConfig.SELECT_MUSIC_VOLUME;
                if (current >= 100) current = 100;
                UserConfig.CURRENT_MUSIC_VOLUME = current;
                music.getAudioPlayer().setVolume(current);
                UserConfig.save();
            }
            while (volumeMusicDownKey.wasPressed()) {
                int current = UserConfig.CURRENT_MUSIC_VOLUME - UserConfig.SELECT_MUSIC_VOLUME;
                if (current <= 1) current = 1;
                UserConfig.CURRENT_MUSIC_VOLUME = current;
                music.getAudioPlayer().setVolume(current);
                UserConfig.save();
            }
            while (loadTrack.wasPressed()) {
                if (!Client.clothConfig) {
                    if (client.player != null)
                        client.player.sendMessage(Localization.getText(("waterplayer.message.clothConfigNotFound")), false);
                    return;
                }
                client.send(() -> client.setScreen(MusicScreen.buildScreen(client.currentScreen)));
            }
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            start();
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("playlist")
                    .then(
                            argument("name", greedyString()).executes(context -> {
                                if (!Client.clothConfig) {
                                    context.getSource().getPlayer().sendMessage(Localization.getText(("waterplayer.message.clothConfigNotFound")), false);
                                } else {
                                    final Screen current = MinecraftClient.getInstance().currentScreen;
                                    Screen configScreen = new PlaylistScreen().buildScreen(current, getString(context, "name"));
                                    MinecraftClient client = context.getSource().getClient();
                                    client.send(() -> client.setScreen(configScreen));
                                }
                                return 1;
                            })
                    )
            );
        });
    }

    // MUSIC
    public static void start() {
        TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(UserConfig.ENABLE_CHANGE_TITLE && music.getAudioPlayer().getPlayingTrack() != null) updateTitle();
                if (UserConfig.ENABLE_OVERLAY) updateBossBar();
                else if (lastBossBar) clearBossBar();
            }
        }, 250, 250);
    }

    public static ClientBossBar bossBar;
    public static void updateBossBar() {
        if (!lastBossBar) lastBossBar = true;
        boolean playing = false;
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) {
                if (Client.music.getAudioPlayer().getPlayingTrack() != null) {
                    if (Client.music.getAudioPlayer().isPaused()) {
                        bossBar = new ClientBossBar(Client.bossBarUUID, Localization.toText(Localization.getLocalization("overlay.pause", true, false)), 1F
                                , BossBar.Color.YELLOW, BossBar.Style.PROGRESS, false, false, false);
                    } else {
                        if (Client.music.getAudioPlayer().getPlayingTrack().getInfo().isStream)
                            bossBar = new ClientBossBar(Client.bossBarUUID, Localization.toText(Localization.getLocalization("overlay.live", true, false)), (
                                    1F
                            ), BossBar.Color.RED, BossBar.Style.PROGRESS, false, false, false);
                        else
                            bossBar = new ClientBossBar(Client.bossBarUUID, Localization.toText(Localization.getLocalization("overlay", true, false)), (
                                    (float) Client.music.getAudioPlayer().getPlayingTrack().getPosition() / Client.music.getAudioPlayer().getPlayingTrack().getDuration()
                            ), BossBar.Color.GREEN, BossBar.Style.PROGRESS, false, false, false);
                    }
                    playing = true;
                }
                if(playing) client.inGameHud.getBossBarHud().handlePacket(BossBarS2CPacket.add(bossBar));
                else client.inGameHud.getBossBarHud().handlePacket(BossBarS2CPacket.remove(bossBarUUID));
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
        String title;
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null && client.player == null) {
                if (Client.music.getAudioPlayer().getPlayingTrack() != null) {
                    if (Client.music.getAudioPlayer().isPaused()) {
                        title = Localization.getLocalization("title.pause", true, false);
                    } else {
                        if (Client.music.getAudioPlayer().getPlayingTrack().getInfo().isStream)
                            title = Localization.getLocalization("title.live", true, false);
                        else
                            title = Localization.getLocalization("title", true, false);
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
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) {
                client.inGameHud.getBossBarHud().handlePacket(BossBarS2CPacket.remove(bossBarUUID));
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