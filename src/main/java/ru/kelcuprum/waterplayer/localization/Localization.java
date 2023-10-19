package ru.kelcuprum.waterplayer.localization;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.json.JSONObject;
import ru.kelcuprum.waterplayer.config.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Localization {
    /**
     * Получение кода локализации игры который выбрал игрок<br>
     * При запуске может быть null, поэтому иногда en_us;
     */
    public static String getCodeLocalization(){
        Minecraft CLIENT = Minecraft.getInstance();
        try{
//            return CLIENT.getGame().getSelectedLanguage().getCode();
            return CLIENT.options.languageCode;
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
        Minecraft CLIENT = Minecraft.getInstance();
        File localizationFile = new File(CLIENT.gameDirectory + "/config/WaterPlayer/lang/"+getCodeLocalization()+".json");
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
            Minecraft CLIENT = Minecraft.getInstance();
            File localizationFile = new File(CLIENT.gameDirectory + "/config/WaterPlayer/lang/"+getCodeLocalization()+".json");
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
    public static Component getText(String key){
        return Component.translatable(key);
    }

    /**
     * Перевод String в MutableText
     * @param text
     * @return MutableText
     */
    public static Component toText(String text){
        return Component.literal(text);
    }

    /**
     * Перевод Text в String
     * @param text
     * @return MutableText
     */
    public static String toString(Component text){
        return text.getString();
    }


    /**
     * Парс текста
     * @param text
     * @return String
     */
    public static String getParsedText(String text, boolean clearColor){
        String parsedText = StarScript.run(StarScript.compile(text));
        if(clearColor) parsedText = Utils.clearFormatCodes(parsedText);
        else parsedText = Utils.fixFormatCodes(parsedText);
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
