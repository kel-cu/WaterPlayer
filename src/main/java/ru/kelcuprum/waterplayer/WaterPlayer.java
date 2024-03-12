package ru.kelcuprum.waterplayer;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
        config.load();
        StarScript.init();
        localization.setParser((s) -> StarScript.run(StarScript.compile(s)));
        player = new MusicPlayer();
        registerBinds();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            player.startAudioOutput();
            OverlayHandler hud = new OverlayHandler();
            HudRenderCallback.EVENT.register(hud);
            ClientTickEvents.START_CLIENT_TICK.register(hud);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> player.getAudioPlayer().stopTrack());
        ClientCommandRegistrationCallback.EVENT.register(WaterPlayerCommand::register);
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
    // Logger
    public static void log(String message){log(message, Level.INFO);}
    public static void log(String message, Level level){
        LOG.log(level, "[" + LOG.getName() + "] " + message);
    }
}