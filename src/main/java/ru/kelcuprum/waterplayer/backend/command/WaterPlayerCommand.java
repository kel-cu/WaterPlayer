package ru.kelcuprum.waterplayer.backend.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.Music;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.PlaylistScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.MainConfigsScreen;
import ru.kelcuprum.waterplayer.frontend.gui.toasts.ControlToast;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static java.lang.Integer.parseInt;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WaterPlayerCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("waterplayer")
                .then(literal("load")
                        .then(
                                argument("url", greedyString()).executes(context -> {
                                    LoadMusicScreen.loadMusic(getString(context, "url"));
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            Minecraft client = context.getSource().getClient();
                            client.tell(() -> client.setScreen(new LoadMusicScreen(client.screen)));
                            return 1;
                        })
                )
                .then(literal("now-playing")
                        .executes(context -> {
                            if(Music.trackIsNull()){
                                context.getSource().sendFeedback(Localization.getText("waterplayer.command.now_playing.notPlaying"));
                            } else {
                                context.getSource().sendFeedback(Localization.getText("waterplayer.command.now_playing"));
                                context.getSource().sendFeedback(Localization.toText(WaterPlayer.localization.getParsedText(
                                        Music.isAuthorNull() ? "{track.title}" : "{format.author} {track.title}")));
                                context.getSource().sendFeedback(Localization.toText(WaterPlayer.localization.getParsedText("{format.time}")));
                            }
                            return 1;
                        })
                )
                .then(literal("playlist")
                        .then(
                                argument("name", greedyString()).executes(context -> {
                                    Minecraft client = context.getSource().getClient();
                                    client.tell(() -> { client.setScreen(new PlaylistScreen(client.screen, getString(context, "name"))); });
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            context.getSource().sendFeedback(Localization.getText("waterplayer.command.playlist.noName"));
                            return 1;
                        }))
                .then(literal("skip")
                        .executes(context -> {
                            if(!(WaterPlayer.player.getTrackManager().queue.isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)) {
                                WaterPlayer.player.getTrackManager().nextTrack();
                                context.getSource().getClient().getToasts().addToast(new ControlToast(Localization.getText("waterplayer.message.skip"), false));
                            }
                            return 1;
                        }))
                .then(literal("queue")
                        .then(
                                argument("size", greedyString()).executes(context -> {
                                    queue(parseInt(getString(context, "size")), context);
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            queue(10, context);
                            return 1;
                        }))
                .executes(context -> {
                    Minecraft client = context.getSource().getClient();
                    client.tell(() -> client.setScreen(new MainConfigsScreen().build(client.screen)));
                    return 1;
                })
        );
    }
    public static void queue(int size, CommandContext<FabricClientCommandSource> context){
        List<String> list = new ArrayList<>();
        int pos = 0;
        for(AudioTrack track : WaterPlayer.player.getTrackManager().queue){
            if(pos == size) break;
            StringBuilder builder = new StringBuilder();
            if(Music.isAuthorNull(track)) builder.append(Music.getTitle(track)).append(" ");
            else builder.append("«").append(Music.getAuthor(track)).append("» ").append(Music.getTitle(track)).append(" ");
            builder.append(Music.getIsLive(track) ? WaterPlayer.localization.getLocalization("format.live") : StarScript.getTimestamp(Music.getDuration(track)));
            list.add(builder.toString());
            pos++;
        }
        context.getSource().sendFeedback(Localization.getText(list.isEmpty() ? "waterplayer.command.queue.blank" : "waterplayer.command.queue"));
        if(!list.isEmpty()) {
            int number = 1;
            for (String track : list) {
                context.getSource().sendFeedback(Localization.toText(number + ". " + track));
                if (number != list.size()) number++;
            }
            context.getSource().sendFeedback(Localization.toText(number + "/" + WaterPlayer.player.getTrackManager().queue.size()));
        }
    }
}
