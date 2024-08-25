package ru.kelcuprum.waterplayer.frontend.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.ActivityType;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.util.Objects;

public class DiscordIntegration {
    public static IPCClient client;
    public static RichPresence lastPresence;
    public static boolean CONNECTED = false;
    public static boolean EMPTY = true;

    public void registerApplication(){
        client = new IPCClient(
                //#if WALTER == 0
                1197963953695903794L
                //#else
                //$$ 1277205430275145758L
                //#endif
        );
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
            builder.setLargeImage(icon, MusicHelper.getServiceName(MusicHelper.getService(track)).getString())
                    .setDetails(MusicHelper.getTitle())
                    .setState(MusicHelper.getAuthor());
            getYonKaGorMoment(track, builder, icon);
            long start = System.currentTimeMillis() - MusicHelper.getPosition();
            if(WaterPlayer.player.getAudioPlayer().isPaused()) builder.setSmallImage("paused");
            else {
                builder.setStartTimestamp(parseSeconds(start));
                if(!MusicHelper.getIsLive()) builder.setEndTimestamp(parseSeconds(start+MusicHelper.getDuration()));
            }
            if(track.getInfo().uri.startsWith("https://") || track.getInfo().uri.startsWith("http://")){
                JsonArray buttons = new JsonArray();
                JsonObject button = new JsonObject();
                button.addProperty("label", MusicHelper.getServiceName(MusicHelper.getService(track)).getString());
                button.addProperty("url", track.getInfo().uri);
                buttons.add(button);
                builder.setButtons(buttons);
            }
            send(builder.build());
        }
    }
    public long parseSeconds(long mills){
        return (mills-(mills % 1000)) /1000;
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
    protected void getYonKaGorMoment(AudioTrack track, RichPresence.Builder builder, String icon) {
        if (!MusicHelper.getAuthor(track).equals("YonKaGor")) return;
        switch (MusicHelper.getTitle(track)) {
//            case "I Forgot That You Exist", "I Forgot That You Exist. ¯\\_(ツ)_/¯" -> Items.MUSIC_DISC_WAIT;
            case "Top 10 Things to Do Before You Die", "Top 10 Things To Do Before You Die",
                 "[TW] Top 10 Things To Do Before You Die (Censored)" -> {
                builder.setLargeImage("https://wf.kelcu.ru/mods/waterplayer/icons/seadrive.gif", "HowTo");
            }
//            case "Trash Talkin'", "kennyoung & YonKaGor - Trash Talkin'" -> Items.MUSIC_DISC_OTHERSIDE;
//            case "Fallacy" -> Items.MUSIC_DISC_PIGSTEP;
//            case "You're Just Like Pop Music" -> Items.MUSIC_DISC_MELLOHI;
//            case "Dandelion", "Dandelion \uD83C\uDF3C (Full Song)" -> Items.DANDELION;
//            case "Mr. Sunfish", "Good Morning, Mr. Sunfish!", "Fish ! (Original)" -> Items.TROPICAL_FISH;
//            case "You'll Be Gone" -> Items.MUSIC_DISC_MALL;
//            case "It's Normal", "It's Normal [TW]" -> Items.MUSIC_DISC_11;
//            case "Circus Hop", "Circus Hop [TW]" -> Items.MUSIC_DISC_CHIRP;
//            case "Paper Alibis", "Paper Alibis (Full Song)" -> Items.PAPER;
//            case "Silly Plans" -> Items.LIGHT_BLUE_BED;
//            case "Silly Plans ~ Revisit" -> Items.FILLED_MAP;
//            case "Another Mistake" -> Items.BARRIER;
//            case "Memory Merge" -> Items.FLINT_AND_STEEL;
//            case "Waterland", "Waterland (Lyric video)" -> Items.WATER_BUCKET;
//            case "Artificial Abandonment", "(Original Song) Artificial Abandonment" -> Items.MOSSY_COBBLESTONE;
        };
    }
}
