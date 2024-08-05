
package ru.kelcuprum.waterplayer.frontend.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jetbrains.annotations.Async;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.waterplayer.WaterPlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.FILE_ICON;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.NO_ICON;

public class TextureHelper {
    public static HashMap<String, ResourceLocation> resourceLocationMap = new HashMap<>();
    public static HashMap<String, Boolean> urls = new HashMap<>();
    public static HashMap<String, DynamicTexture> urlsTextures = new HashMap<>();
    public static JsonArray map = new JsonArray();


    public static HashMap<File, ResourceLocation> resourceLocationMap$file = new HashMap<>();
    public static HashMap<File, Boolean> urls$file = new HashMap<>();
    public static HashMap<File, DynamicTexture> urlsTextures$file = new HashMap<>();

    public static ResourceLocation getTexture(String url, String id) {
        id = formatUrls(id.toLowerCase());
        if (resourceLocationMap.containsKey(id)) return resourceLocationMap.get(id);
        else {
            if (!urls.getOrDefault(id, false)) {
                urls.put(id, true);
                String finalId = id;
                new Thread(() -> registerTexture(url, finalId, AlinLib.MINECRAFT.getTextureManager(), GuiUtils.getResourceLocation("waterplayer", finalId))).start();
            }
            return NO_ICON;
        }
    }

    @Async.Execute
    public static void registerTexture(String url, String id, TextureManager textureManager, ResourceLocation textureId) {
        WaterPlayer.log(String.format("REGISTER: %s %s", url, id), Level.DEBUG);
        DynamicTexture texture;
        if(urlsTextures.containsKey(url)) {
            JsonObject data = new JsonObject();
            data.addProperty("url", url);
            data.addProperty("id", id);
            if(!map.contains(data)) map.add(data);
            texture = urlsTextures.get(url);
        }
        else {
            NativeImage image;
            File textureFile = getTextureFile(id);
            boolean isFileExists = textureFile.exists();
            try {
                BufferedImage bufferedImage = isFileExists ? ImageIO.read(getTextureFile(id)) : ImageIO.read(new URL(url));
                if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
                    int x = (bufferedImage.getWidth() - bufferedImage.getHeight()) / 2;
                    bufferedImage = bufferedImage.getSubimage(x, 0, bufferedImage.getHeight(), bufferedImage.getHeight());
                }
                BufferedImage scaleImage = toBufferedImage(bufferedImage.getScaledInstance(128, 128, 2));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(scaleImage, "png", byteArrayOutputStream);
                byte[] bytesOfImage = byteArrayOutputStream.toByteArray();
                image = NativeImage.read(bytesOfImage);
                if(!isFileExists){
                    Files.createDirectories(textureFile.toPath().getParent());
                    Files.write(textureFile.toPath(), image.asByteArray());
                }
            } catch (Exception e) {
                WaterPlayer.log("Error loading image from URL: " + url + " - " + e.getMessage());
                resourceLocationMap.put(id, NO_ICON);
                return;
            }
            texture = new DynamicTexture(image);
        }
        textureManager.register(textureId, texture);
        resourceLocationMap.put(id, textureId);
        JsonObject data = new JsonObject();
        data.addProperty("url", url);
        data.addProperty("id", id);
        if(!map.contains(data)) map.add(data);
    }


    public static ResourceLocation getTexture$File(File file, String id) {
        id = formatUrls$files(id.toLowerCase());
        if (resourceLocationMap$file.containsKey(file)) {
            return resourceLocationMap$file.get(file);
        }
        else {
            if (!urls$file.getOrDefault(file, false)) {
                urls$file.put(file, true);
                String finalId = id;
                new Thread(() -> registerTexture$File(file, finalId, AlinLib.MINECRAFT.getTextureManager(), GuiUtils.getResourceLocation("waterplayer", finalId))).start();
            }
            return NO_ICON;
        }
    }
    @Async.Execute
    public static void registerTexture$File(File file, String id, TextureManager textureManager, ResourceLocation textureId) {
        WaterPlayer.log(String.format("REGISTER: %s", file.toPath()), Level.DEBUG);
        DynamicTexture texture;
        if(urlsTextures$file.containsKey(file)) {
            JsonObject data = new JsonObject();
            data.addProperty("url", file.toPath().toString());
            data.addProperty("id", id);
            if(!map.contains(data)) map.add(data);
            texture = urlsTextures$file.get(file);
        }
        else {
            NativeImage image;
            try {
                File textureFile = getTextureFile(id);
                boolean isFileExists = textureFile.exists();
                BufferedImage bufferedImage;
                if(isFileExists){
                    bufferedImage =  ImageIO.read(getTextureFile(id));
                } else {
                    AudioFile f = AudioFileIO.read(file);
                    if(!f.getTag().getArtworkList().isEmpty()){
                        bufferedImage = (BufferedImage) f.getTag().getFirstArtwork().getImage();
                    } else {
                        resourceLocationMap$file.put(file, FILE_ICON);
                        return;
                    }
                }
                if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
                    int x = (bufferedImage.getWidth() - bufferedImage.getHeight()) / 2;
                    bufferedImage = bufferedImage.getSubimage(x, 0, bufferedImage.getHeight(), bufferedImage.getHeight());
                }
                BufferedImage scaleImage = toBufferedImage(bufferedImage.getScaledInstance(128, 128, 2));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(scaleImage, "png", byteArrayOutputStream);
                byte[] bytesOfImage = byteArrayOutputStream.toByteArray();
                image = NativeImage.read(bytesOfImage);

                if(!isFileExists){
                    Files.createDirectories(textureFile.toPath().getParent());
                    Files.write(textureFile.toPath(), image.asByteArray());
                }
            } catch (Exception e) {
                WaterPlayer.log("Error loading image from URL: " + file.toPath() + " - " + e.getMessage());
                resourceLocationMap$file.put(file, FILE_ICON);
                return;
            }
            texture = new DynamicTexture(image);
        }
        textureManager.register(textureId, texture);
        resourceLocationMap$file.put(file, textureId);
        JsonObject data = new JsonObject();
        data.addProperty("url", file.toPath().toString());
        data.addProperty("id", id);
        if(!map.contains(data)) map.add(data);
    }



    public static File getTextureFile(String url) {
        return new File("config/waterplayer/textures/" + url + ".png");
    }
    public static void saveMap(){
        try {
            Path path = new File("config/waterplayer/textures/map.json").toPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, map.toString());
        } catch (IOException e) {
            WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
        }
    }

    public static void loadTextures(TextureManager textureManager){
        loadMap();
        for(JsonElement json : map){
            JsonObject data = json.getAsJsonObject();
            ResourceLocation l = GuiUtils.getResourceLocation("waterplayer", data.get("id").getAsString());
            if(new File(data.get("url").getAsString()).exists())
                registerTexture$File(new File(data.get("url").getAsString()), data.get("id").getAsString(), textureManager, l);
            else registerTexture(data.get("url").getAsString(), data.get("id").getAsString(), textureManager, l);
        }
    }

    public static void loadMap(){
        File mapFile = new File("config/waterplayer/textures/map.json");
        if(mapFile.exists() && mapFile.isFile()){
            try {
                map = GsonHelper.parseArray(Files.readString(mapFile.toPath()));
            } catch (Exception e){
                map = new JsonArray();
                WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            }
        } else map = new JsonArray();
    }

    public static String formatUrls(String url) {
        return url.toLowerCase().replaceAll("[^A-Za-z0-9]", "_");
    }
    public static String formatUrls$files(String url) {
        return convertCyrilic(url).toLowerCase().replaceAll("[^A-Za-z0-9_-]", "_");
    }
    public static String convertCyrilic(String message){
        char[] abcCyr =   {'а','б','в','г','д','ѓ','е', 'ж','з','ѕ','и','ј','к','л','љ','м','н','њ','о','п','р','с','т','у', 'ф','х','ц','ч','џ','ш', 'А','Б','В','Г','Д','Ѓ','Е', 'Ж','З','Ѕ','И','Ј','К','Л','Љ','М','Н','Њ','О','П','Р','С','Т', 'Ќ', 'У','Ф', 'Х','Ц','Ч','Џ','Ш','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9','/','-'};
        String[] abcLat = {"a","b","v","g","d","]","e","zh","z","y","i","j","k","l","q","m","n","w","o","p","r","s","t","u","f","h", "c",";", "x","{","A","B","V","G","D","}","E","Zh","Z","Y","I","J","K","L","Q","M","N","W","O","P","R","S","T","KJ","U","F","H", "C",":", "X","{", "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","1","2","3","4","5","6","7","8","9","/","-"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++ ) {
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
        }
        return builder.toString();
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        // Return the buffered image
        return bimage;
    }

}
