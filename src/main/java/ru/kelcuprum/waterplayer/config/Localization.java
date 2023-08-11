package ru.kelcuprum.waterplayer.config;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.json.JSONObject;
import ru.kelcuprum.waterplayer.Client;
import ru.kelcuprum.waterplayer.MusicPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Localization {
    /**
     * Получение кода локализации игры который выбрал игрок<br>
     * При запуске может быть null, поэтому иногда en_us;
     */
    public static String getCodeLocalization(){
        MinecraftClient CLIENT = MinecraftClient.getInstance();
        try{
//            return CLIENT.getGame().getSelectedLanguage().getCode();
            return CLIENT.options.language;
        } catch (Exception e){
            return "en_us";
        }
    }

    /**
     * Получение JSON файл локализации
     * @return JSONObject
     * @throws IOException
     */
    public static JSONObject getJSONFile() throws IOException {
        MinecraftClient CLIENT = MinecraftClient.getInstance();
        File localizationFile = new File(CLIENT.runDirectory + "/config/WaterPlayer/lang/"+getCodeLocalization()+".json");
        if(localizationFile.exists()){
            return new JSONObject(Files.readString(localizationFile.toPath()));
        } else {
            return new JSONObject();
        }
    }

    /**
     * Получение текста локализации
     * @param type
     * @param parse
     * @return String
     */
    public static String getLocalization(String type, boolean parse){
        return getLocalization(type, parse, true);
    }
    public static String getLocalization(String type, boolean parse, boolean clearColor){
        String text = "";
        try {
            JSONObject JSONLocalization = getJSONFile();
            if(JSONLocalization.isNull(type)) text = getText("waterplayer." + type).getString();
            else text = JSONLocalization.getString(type);
        } catch (Exception e){
            e.printStackTrace();
            text = getText("waterplayer." + type).getString();
        }
        if(parse) return getParsedText(text, clearColor);
        else return text;
    }
    public static String getLcnDefault(String type){
        String text = getText("waterplayer." + type).getString();;
        return text;
    }
    /**
     * Задать значение локализации на определённый текст в JSON файле
     * @param type
     * @param text
     */
    public static void setLocalization(String type, String text){
        try {
            JSONObject JSONLocalization = getJSONFile();
            JSONLocalization.put(type, text);
            MinecraftClient CLIENT = MinecraftClient.getInstance();
            File localizationFile = new File(CLIENT.runDirectory + "/config/WaterPlayer/lang/"+getCodeLocalization()+".json");
            Files.createDirectories(localizationFile.toPath().getParent());
            Files.writeString(localizationFile.toPath(), JSONLocalization.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Хуета которая может быть спасёт от Mojang которые сука постоянно меняют либо название класса либо еще что-то
     * @return MutableText
     * @param key
     */
    public static MutableText getText(String key){
        return Text.translatable(key);
    }

    /**
     * Перевод String в MutableText
     * @param text
     * @return MutableText
     */
    public static MutableText toText(String text){
        return Text.literal(text);
    }

    /**
     * Перевод Text в String
     * @param text
     * @return MutableText
     */
    public static String toString(Text text){
        return text.getString();
    }


    /**
     * Парс текста
     * @param text
     * @return String
     */
    public static String getParsedText(String text, boolean clearColor){
        String parsedText = text;
        parsedText = parsedText.replace("%minecraft_version%", MinecraftClient.getInstance().getGameVersion());
        MusicPlayer music = Client.music;
        if(music.getAudioPlayer().getPlayingTrack() != null) {
            parsedText = parsedText.replace("%music_volume%", String.valueOf(music.getAudioPlayer().getVolume()));
            AudioTrack track = music.getAudioPlayer().getPlayingTrack();
            parsedText = getMusicParseText(track, parsedText);
        }
        if(clearColor) parsedText = Utils.clearFormatCodes(parsedText);
        else parsedText = Utils.fixFormatCodes(parsedText);
        return parsedText;
    }
    public static String getMusicParseText(AudioTrack track, String text){
        return getMusicParseText(track, text, "");
    }
    public static String getMusicParseText(AudioTrack track, String text, String artist){
        String parsedText = text;
        AudioTrackInfo info = track.getInfo();
        parsedText = parsedText.replace("%music_author_format%", info.author.equals("Unknown artist") ? artist : Localization.getLocalization("format.author", false));
        String[] file = track.getInfo().uri == null ? track.getInfo().title.split("") : track.getInfo().uri.split("/");
        parsedText = parsedText.replace("%music_title_format%", info.author.equals("Unknown title") ? file[file.length-1] : Localization.getLocalization("format.title", false));
        parsedText = parsedText.replace("%music_author%", info.author);
        parsedText = parsedText.replace("%music_title%", info.title);
        if (info.isStream) {
            parsedText = parsedText.replace("%music_time_format%", Localization.getLocalization("format.live", false));
            parsedText = parsedText.replace("%music_time_format_queue%", Localization.getLocalization("format.live", false));
        } else{
            parsedText = parsedText.replace("%music_time_format%", Localization.getLocalization("format.time", false));
            parsedText = parsedText.replace("%music_time_format_queue%", Localization.getLocalization("format.time.queue", false));
            parsedText = parsedText.replace("%music_time%", getTimestamp(track.getPosition()));
            parsedText = parsedText.replace("%music_time_max%", getTimestamp(track.getDuration()));
        }

        return parsedText;
    }
    public static String getTimestamp(long milliseconds)
    {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }
}
