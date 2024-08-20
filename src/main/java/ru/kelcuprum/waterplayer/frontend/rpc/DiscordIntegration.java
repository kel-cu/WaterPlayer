package ru.kelcuprum.waterplayer.frontend.rpc;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.ActivityType;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

public class DiscordIntegration {
    public static IPCClient client;
    public static RichPresence lastPresence;
    public static boolean CONNECTED = false;
    public static boolean EMPTY = true;

    public void registerApplication(){
        client = new IPCClient(1197963953695903794L);
        setupListener();
        try {
            client.connect();
        } catch (Exception ex) {
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
    }

    public void exitApplication(){
        client.close();
    }

    public void setupListener() {
        client.setListener(new IPCListener() {
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
            public void onReady(IPCClient client) {
                WaterPlayer.log("The mod has been connected to Discord", Level.DEBUG);
                CONNECTED = true;
            }

            @Override
            public void onClose(IPCClient ipcClient, JsonObject jsonObject) {
                CONNECTED = false;
            }

            @Override
            public void onDisconnect(IPCClient ipcClient, Throwable throwable) {
                WaterPlayer.log("The mod has been pulled from Discord", Level.DEBUG);
                WaterPlayer.log(String.format("Reason: %s", throwable.getLocalizedMessage()), Level.DEBUG);
                CONNECTED = false;
            }
        });
    }

    public void update(){
        AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
        if(track == null || !WaterPlayer.config.getBoolean("DISCORD", false)) send(null);
        else {
            RichPresence.Builder builder = new RichPresence.Builder().setActivityType(ActivityType.Listening);
            String icon = MusicHelper.isFile() ? "file" : (track.getInfo().artworkUrl == null || track.getInfo().artworkUrl.isBlank()) ? "no_icon" : track.getInfo().artworkUrl;
            builder.setLargeImage(icon, Component.translatable("waterplayer.track.service", MusicHelper.getServiceName(MusicHelper.getService(track))).getString())
                    .setDetails(MusicHelper.getTitle())
                    .setState(MusicHelper.getAuthor());
            long start = System.currentTimeMillis() - MusicHelper.getPosition();
            if(WaterPlayer.player.getAudioPlayer().isPaused()) builder.setSmallImage("paused");
            else {
                builder.setStartTimestamp(parseSeconds(start));
                if(!MusicHelper.getIsLive()) builder.setEndTimestamp(parseSeconds(start+MusicHelper.getDuration()));
            }
            send(builder.build());
        }
    }
    public long parseSeconds(long mills){
        long shit = mills % 1000;
        return (mills-shit) /1000;
    }

    public void send(RichPresence presence){
        if(!EMPTY && CONNECTED && presence == null) {
            if(lastPresence != null) exitApplication();
            lastPresence = null;
            EMPTY = true;
        } else if(presence != null && (lastPresence == null || (!lastPresence.toJson().toString().equalsIgnoreCase(presence.toJson().toString())))){
            if(EMPTY) registerApplication();
            EMPTY = false;
            try {
                if (CONNECTED) client.sendRichPresence(presence);
                lastPresence = presence;
            } catch (Exception ex) {
                WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.ERROR);
            }
        }
    }
}
