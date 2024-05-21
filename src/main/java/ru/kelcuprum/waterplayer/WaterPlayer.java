package ru.kelcuprum.waterplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.api.events.client.ScreenEvents;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.backend.MusicPlayer;
import ru.kelcuprum.waterplayer.backend.command.WaterPlayerCommand;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;
import ru.kelcuprum.waterplayer.frontend.gui.overlays.OverlayHandler;

public class WaterPlayer implements ClientModInitializer {
    public static Config config = new Config("config/WaterPlayer/config.json");
    public static final Logger LOG = LogManager.getLogger("WaterPlayer");
    public static MusicPlayer player;
    public static Localization localization = new Localization("waterplayer", "config/WaterPlayer/lang");
    public static Minecraft MINECRAFT = Minecraft.getInstance();

    @Override
    public void onInitializeClient() {
        log("Hello, world! UwU");
        StarScript.init();
        localization.setParser((s) -> StarScript.run(StarScript.compile(s)));
        player = new MusicPlayer();
        registerBinds();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            player.startAudioOutput();
            OverlayHandler hud = new OverlayHandler();
            ScreenEvents.SCREEN_RENDER.register(hud);
            HudRenderCallback.EVENT.register(hud);
            ClientTickEvents.START_CLIENT_TICK.register(hud);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> player.getAudioPlayer().stopTrack());
        ClientCommandRegistrationCallback.EVENT.register(WaterPlayerCommand::register);
    }

    public static ToastBuilder getToast() {
        return new ToastBuilder().setIcon(Items.MUSIC_DISC_STRAD).setTitle(Component.translatable("waterplayer.name"));
    }

    public static void registerBinds() {
        KeyMapping loadTrack = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.load",
                GLFW.GLFW_KEY_ENTER, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping playOrPause = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.pause",
                GLFW.GLFW_KEY_P, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping skipTrack = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.skip",
                GLFW.GLFW_KEY_X, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping resetQueueKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.reset",
                GLFW.GLFW_KEY_DELETE, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping shuffleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.shuffle",
                GLFW.GLFW_KEY_PAGE_DOWN, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping repeatingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.repeating",
                GLFW.GLFW_KEY_PAGE_UP, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping volumeMusicUpKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.volume.up",
                GLFW.GLFW_KEY_UP, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping volumeMusicDownKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.volume.down",
                GLFW.GLFW_KEY_DOWN, // The keycode of the key
                "waterplayer.name"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (playOrPause.consumeClick()) {
                player.getAudioPlayer().setPaused(!player.getAudioPlayer().isPaused());
                if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText(player.getAudioPlayer().isPaused() ? "waterplayer.message.pause" : "waterplayer.message.play"))
                        .show(AlinLib.MINECRAFT.getToasts());
            }
            while (repeatingKey.consumeClick()) {
                player.getTrackScheduler().changeRepeatStatus();
                if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setIcon(player.getTrackScheduler().getRepeatIcon())
                        .setMessage(Localization.getText(player.getTrackScheduler().getRepeatStatus() == 0 ? "waterplayer.message.repeat.no" : player.getTrackScheduler().getRepeatStatus() == 1 ? "waterplayer.message.repeat" : "waterplayer.message.repeat.one" ))
                        .show(AlinLib.MINECRAFT.getToasts());
            }
            while (resetQueueKey.consumeClick()) {
                player.getTrackScheduler().skiping = false;
                if (!player.getTrackScheduler().queue.isEmpty()) {
                    player.getTrackScheduler().queue.clear();
                    if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText("waterplayer.message.reset"))
                            .show(AlinLib.MINECRAFT.getToasts());
                }
            }
            while (shuffleKey.consumeClick()) {
                if (player.getTrackScheduler().queue.size() >= 2) {
                    player.getTrackScheduler().shuffle();
                    if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText("waterplayer.message.shuffle"))
                            .show(AlinLib.MINECRAFT.getToasts());
                }
            }
            while (skipTrack.consumeClick()) {
                if (player.getTrackScheduler().queue.isEmpty() && player.getAudioPlayer().getPlayingTrack() == null)
                    return;
                player.getTrackScheduler().nextTrack();
                if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText("waterplayer.message.skip"))
                        .show(AlinLib.MINECRAFT.getToasts());
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
                AlinLib.MINECRAFT.setScreen(new LoadMusicScreen(AlinLib.MINECRAFT.screen));
            }
        });
    }

    // Logger
    public static void log(String message) {
        log(message, Level.INFO);
    }

    public static void log(String message, Level level) {
        LOG.log(level, "[" + LOG.getName() + "] " + message);
    }
}