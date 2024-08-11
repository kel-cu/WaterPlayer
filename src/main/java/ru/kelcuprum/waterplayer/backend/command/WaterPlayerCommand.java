//#if FABRIC
package ru.kelcuprum.waterplayer.backend.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.frontend.gui.screens.playlist.ViewPlaylistScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.MainConfigsScreen;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static java.lang.Integer.parseInt;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static ru.kelcuprum.waterplayer.WaterPlayer.getTimestamp;

public class WaterPlayerCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("waterplayer")
                .then(literal("play")
                        .then(
                                argument("url", greedyString()).executes(context -> {
                                    WaterPlayer.player.loadMusic(getString(context, "url"), true);
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            Minecraft client = context.getSource().getClient();
                            WaterPlayer.player.loadMusic(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", ""), true);
                            return 1;
                        })
                )
                .then(literal("now-playing")
                        .executes(context -> {
                            if (MusicHelper.trackIsNull()) {
                                context.getSource().sendFeedback(Localization.getText("waterplayer.command.now_playing.notPlaying"));
                            } else {
                                context.getSource().sendFeedback(Localization.getText("waterplayer.command.now_playing"));
                                context.getSource().sendFeedback(Localization.toText(
                                        MusicHelper.isAuthorNull() ? MusicHelper.getTitle() : MusicHelper.getAuthor() + " - " + MusicHelper.getTitle()));
                                context.getSource().sendFeedback(Localization.toText(WaterPlayer.localization.getParsedText("{waterplayer.format.time}")));
                            }
                            return 1;
                        })
                )
                .then(literal("playlist")
                        .then(
                                argument("name", greedyString()).executes(context -> {
                                    Minecraft client = context.getSource().getClient();
                                    client.tell(() -> {
                                        try {
                                            client.setScreen(new ViewPlaylistScreen(client.screen, new Playlist(getString(context, "name"))));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            context.getSource().sendFeedback(Localization.getText("waterplayer.command.playlist.noName"));
                            return 1;
                        }))
                .then(literal("skip")
                        .executes(context -> {
                            if (!(WaterPlayer.player.getTrackScheduler().queue.isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)) {
                                WaterPlayer.player.getTrackScheduler().nextTrack();
                                WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.message.skip")).show(context.getSource().getClient().getToasts());
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
                    client.tell(() -> client.setScreen(MainConfigsScreen.build(client.screen)));
                    return 1;
                })
        );
    }

    public static void queue(int size, CommandContext<FabricClientCommandSource> context) {
        List<String> list = new ArrayList<>();
        int pos = 0;
        for (AudioTrack track : WaterPlayer.player.getTrackScheduler().queue) {
            if (pos == size) break;
            StringBuilder builder = new StringBuilder();
            if (MusicHelper.isAuthorNull(track)) builder.append(MusicHelper.getTitle(track)).append(" ");
            else
                builder.append("«").append(MusicHelper.getAuthor(track)).append("» ").append(MusicHelper.getTitle(track)).append(" ");
            builder.append(MusicHelper.getIsLive(track) ? WaterPlayer.localization.getLocalization("format.live") : getTimestamp(MusicHelper.getDuration(track)));
            list.add(builder.toString());
            pos++;
        }
        context.getSource().sendFeedback(Localization.getText(list.isEmpty() ? "waterplayer.command.queue.blank" : "waterplayer.command.queue"));
        if (!list.isEmpty()) {
            int number = 1;
            for (String track : list) {
                context.getSource().sendFeedback(Localization.toText(number + ". " + track));
                if (number != list.size()) number++;
            }
            context.getSource().sendFeedback(Localization.toText(number + "/" + WaterPlayer.player.getTrackScheduler().queue.size()));
        }
    }
}
//#endif
