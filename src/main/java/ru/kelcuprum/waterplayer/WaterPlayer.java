package ru.kelcuprum.waterplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.api.events.client.*;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.backend.KeyBind;
import ru.kelcuprum.waterplayer.backend.MusicPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.backend.command.WaterPlayerCommand;
import ru.kelcuprum.waterplayer.frontend.gui.TexturesHelper;
import ru.kelcuprum.waterplayer.frontend.gui.overlays.SubtitlesHandler;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ControlScreen;
import ru.kelcuprum.waterplayer.frontend.gui.overlays.OverlayHandler;

import java.util.ArrayList;
import java.util.List;

public class WaterPlayer implements ClientModInitializer {
    public static Config config = new Config("config/WaterPlayer/config.json");
    public static final Logger LOG = LogManager.getLogger("WaterPlayer");
    public static MusicPlayer player;
    public static Localization localization = new Localization("waterplayer", "config/WaterPlayer/lang");

    @Override
    public void onInitializeClient() {
        log("Hello, world! UwU");
        WaterPlayerAPI.loadConfig();
        StarScript.init();
        localization.setParser((s) -> StarScript.run(StarScript.compile(s)));
        player = new MusicPlayer();
        WaterPlayer.registerBinds();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            player.startAudioOutput();
            OverlayHandler hud = new OverlayHandler();
            SubtitlesHandler sub = new SubtitlesHandler();
            ScreenEvents.SCREEN_RENDER.register(hud);
            GuiRenderEvents.RENDER.register(hud);
            GuiRenderEvents.RENDER.register(sub);
            ClientTickEvents.START_CLIENT_TICK.register(hud);
            ClientTickEvents.START_CLIENT_TICK.register(sub);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(e -> {
            player.getAudioPlayer().stopTrack();
            TexturesHelper.saveMap();
        });
        ClientCommandRegistrationCallback.EVENT.register(WaterPlayerCommand::register);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for(KeyBind bind : keyBinds){
                if(bind.key().consumeClick()) bind.onExecute().run();
            }
        });
        ScreenEvents.KEY_PRESS.register((Screen screen, int code, int scan, int modifiers, CallbackInfoReturnable<Boolean> var5) -> {
            if(!WaterPlayer.config.getBoolean("ENABLE_KEYBINDS", false)) return;
            if(screen instanceof TitleScreen || screen instanceof PauseScreen || screen instanceof ControlScreen) {
                for (KeyBind bind : keyBinds) {
                    if ((bind.key().matches(code, scan) || bind.key().matchesMouse(code)) && bind.onExecute().run()) {
                            var5.setReturnValue(true);
                            var5.cancel();
                            break;
                    }
                }
            }
        });
    }

    public static ToastBuilder getToast() {
        return new ToastBuilder().setIcon(Items.MUSIC_DISC_STRAD).setTitle(Component.translatable("waterplayer.name"));
    }
    public static List<KeyBind> keyBinds = new ArrayList<>();

    public static void registerBinds() {
        KeyMapping key1 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.control",
                GLFW.GLFW_KEY_ENTER, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping key2 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.pause",
                GLFW.GLFW_KEY_P, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping key3 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.skip",
                GLFW.GLFW_KEY_X, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping key4 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.reset",
                GLFW.GLFW_KEY_DELETE, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping key5 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.shuffle",
                GLFW.GLFW_KEY_PAGE_DOWN, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping key6 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.repeating",
                GLFW.GLFW_KEY_PAGE_UP, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping key7 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.volume.up",
                GLFW.GLFW_KEY_UP, // The keycode of the key
                "waterplayer.name"
        ));
        KeyMapping key8 = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "waterplayer.key.volume.down",
                GLFW.GLFW_KEY_DOWN, // The keycode of the key
                "waterplayer.name"
        ));
        keyBinds.add(new KeyBind(key1, () -> {
            if(!(AlinLib.MINECRAFT.screen instanceof ControlScreen)) {
                AlinLib.MINECRAFT.setScreen(new ControlScreen(AlinLib.MINECRAFT.screen));
                return true;
            } else return false;
        }));
        keyBinds.add(new KeyBind(key2, () -> {
            if(AlinLib.MINECRAFT.screen instanceof ControlScreen){
                if(AlinLib.MINECRAFT.screen.getFocused() instanceof EditBox) return false;
                ((ControlScreen) AlinLib.MINECRAFT.screen).play.onPress();
            } else {
                player.getAudioPlayer().setPaused(!player.getAudioPlayer().isPaused());
            }
            if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText(player.getAudioPlayer().isPaused() ? "waterplayer.message.pause" : "waterplayer.message.play"))
                    .show(AlinLib.MINECRAFT.getToasts());
            return true;
        }));
        keyBinds.add(new KeyBind(key3, () -> {
            if (player.getTrackScheduler().queue.isEmpty() && player.getAudioPlayer().getPlayingTrack() == null)
                return false;
            player.getTrackScheduler().nextTrack();
            if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText("waterplayer.message.skip"))
                    .show(AlinLib.MINECRAFT.getToasts());
            return true;
        }));
        keyBinds.add(new KeyBind(key4, () -> {
            player.getTrackScheduler().skiping = false;
            if (!player.getTrackScheduler().queue.isEmpty()) {
                player.getTrackScheduler().queue.clear();
                if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText("waterplayer.message.reset"))
                        .show(AlinLib.MINECRAFT.getToasts());
            }
            return true;
        }));
        keyBinds.add(new KeyBind(key5, () -> {
            if (player.getTrackScheduler().queue.size() >= 2) {
                player.getTrackScheduler().shuffle();
                if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setMessage(Localization.getText("waterplayer.message.shuffle"))
                        .show(AlinLib.MINECRAFT.getToasts());
            }
            return true;
        }));
        keyBinds.add(new KeyBind(key6, () -> {
            if(player.getAudioPlayer().getPlayingTrack() == null) return false;
            if(AlinLib.MINECRAFT.screen instanceof ControlScreen){
                if(AlinLib.MINECRAFT.screen.getFocused() instanceof EditBox) return false;
                ((ControlScreen) AlinLib.MINECRAFT.screen).repeat.onPress();
            } else {
                player.getTrackScheduler().changeRepeatStatus();
            }
            if (WaterPlayer.config.getBoolean("ENABLE_NOTICE", true)) getToast().setIcon(player.getTrackScheduler().getRepeatIcon())
                    .setMessage(Localization.getText(player.getTrackScheduler().getRepeatStatus() == 0 ? "waterplayer.message.repeat.no" : player.getTrackScheduler().getRepeatStatus() == 1 ? "waterplayer.message.repeat" : "waterplayer.message.repeat.one" ))
                    .show(AlinLib.MINECRAFT.getToasts());
            return true;
        }));
        keyBinds.add(new KeyBind(key7, () -> {
            if(player.getAudioPlayer().getPlayingTrack() == null || AlinLib.MINECRAFT.screen instanceof ControlScreen) return false;
            int current = config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue() + config.getNumber("SELECT_MUSIC_VOLUME", 1).intValue();
            if (current >= 100) current = 100;
            config.setNumber("CURRENT_MUSIC_VOLUME", current);
            player.getAudioPlayer().setVolume(current);
            config.save();
            return true;
        }));
        keyBinds.add(new KeyBind(key8, () -> {
            if(player.getAudioPlayer().getPlayingTrack() == null || AlinLib.MINECRAFT.screen instanceof ControlScreen) return false;
            int current = config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue() - config.getNumber("SELECT_MUSIC_VOLUME", 1).intValue();
            if (current <= 0) current = 0;
            config.setNumber("CURRENT_MUSIC_VOLUME", current);
            player.getAudioPlayer().setVolume(current);
            config.save();
            return true;
        }));
    }

    // Logger
    public static void log(String message) {
        log(message, Level.INFO);
    }

    public static void log(String message, Level level) {
        LOG.log(level, "[" + LOG.getName() + "] " + message);
    }
    public static void confirmLinkNow(Screen screen, String link) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
            if (bl) {
                Util.getPlatform().openUri(link);
            }

            minecraft.setScreen(screen);
        }, link, true));
    }
}