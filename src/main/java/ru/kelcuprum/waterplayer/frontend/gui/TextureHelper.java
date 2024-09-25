
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import static org.apache.logging.log4j.core.config.plugins.convert.Base64Converter.parseBase64Binary;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class TextureHelper {
    public static HashMap<String, ResourceLocation> resourceLocationMap = new HashMap<>();
    public static HashMap<String, Boolean> urls = new HashMap<>();
    public static HashMap<String, DynamicTexture> urlsTextures = new HashMap<>();
    public static JsonArray map = new JsonArray();


    public static HashMap<File, ResourceLocation> resourceLocationMap$file = new HashMap<>();
    public static HashMap<File, Boolean> urls$file = new HashMap<>();
    public static HashMap<File, DynamicTexture> urlsTextures$file = new HashMap<>();


    public static HashMap<String, ResourceLocation> resourceLocationMap$Base64 = new HashMap<>();
    public static HashMap<String, Boolean> urls$Base64 = new HashMap<>();
    public static HashMap<String, DynamicTexture> urlsTextures$Base64 = new HashMap<>();

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
        if (urlsTextures.containsKey(url)) {
            JsonObject data = new JsonObject();
            data.addProperty("url", url);
            data.addProperty("id", id);
            if (!map.contains(data)) map.add(data);
            texture = urlsTextures.get(url);
        } else {
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
                if (!isFileExists) {
                    Files.createDirectories(textureFile.toPath().getParent());
                    Files.write(textureFile.toPath(), bytesOfImage);
                }
            } catch (Exception e) {
                WaterPlayer.log("Error loading image from URL: " + url + " - " + e.getMessage());
                resourceLocationMap.put(id, NO_ICON);
                return;
            }
            texture = new DynamicTexture(image);
        }
        if (textureManager != null) {
            textureManager.register(textureId, texture);
            resourceLocationMap.put(id, textureId);
            JsonObject data = new JsonObject();
            data.addProperty("url", url);
            data.addProperty("id", id);
            if (!map.contains(data)) map.add(data);
        }
    }


    public static ResourceLocation getTexture$File(File file, String id) {
        id = formatUrls(id.toLowerCase());
        if (resourceLocationMap$file.containsKey(file)) {
            return resourceLocationMap$file.get(file);
        } else {
            if (!urls$file.getOrDefault(file, false)) {
                urls$file.put(file, true);
                String finalId = id;
                new Thread(() -> registerTexture$File(file, finalId, AlinLib.MINECRAFT.getTextureManager(), GuiUtils.getResourceLocation("waterplayer", finalId))).start();
            }
            return FILE_ICON;
        }
    }
    public static void removeTexture$File(File file){
        String id = formatUrls("local_"+file.getAbsolutePath());
        if(urlsTextures$file.containsKey(file))
            urlsTextures$file.remove(file);
        if(resourceLocationMap$file.containsKey(file))
            resourceLocationMap$file.remove(file);
        if(urls$file.containsKey(file))
            urls$file.remove(file);
        File fileIcon = getTextureFile(id);
        if(fileIcon.exists()) fileIcon.delete();
        JsonObject data = new JsonObject();
        data.addProperty("url", file.toPath().toString());
        data.addProperty("id", id);
        if (map.contains(data)) map.remove(data);
    }
    @Async.Execute
    public static void registerTexture$File(File file, String id, TextureManager textureManager, ResourceLocation textureId) {
        WaterPlayer.log(String.format("REGISTER: %s", file.toPath()), Level.DEBUG);
        DynamicTexture texture;
        if (urlsTextures$file.containsKey(file)) {
            JsonObject data = new JsonObject();
            data.addProperty("url", file.toPath().toString());
            data.addProperty("id", id);
            if (!map.contains(data)) map.add(data);
            texture = urlsTextures$file.get(file);
        } else {
            NativeImage image;
            try {
                File textureFile = getTextureFile(id);
                boolean isFileExists = textureFile.exists();
                BufferedImage bufferedImage;
                if (isFileExists) {
                    bufferedImage = ImageIO.read(getTextureFile(id));
                } else {
                    AudioFile f = AudioFileIO.read(file);
                    if (!f.getTag().getArtworkList().isEmpty()) {
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

                if (!isFileExists) {
                    Files.createDirectories(textureFile.toPath().getParent());
                    Files.write(textureFile.toPath(), bytesOfImage);
                }
            } catch (Exception e) {
                WaterPlayer.log("Error loading image from URL: " + file.toPath() + " - " + e.getMessage());
                resourceLocationMap$file.put(file, FILE_ICON);
                return;
            }
            texture = new DynamicTexture(image);
        }
        if (textureManager != null) {
            textureManager.register(textureId, texture);
            resourceLocationMap$file.put(file, textureId);
            JsonObject data = new JsonObject();
            data.addProperty("url", file.toPath().toString());
            data.addProperty("id", id);
            if (!map.contains(data)) map.add(data);
        }
    }

    public static void remove$Base64(String id, String base) {
        if (base != null) urlsTextures$Base64.remove(base);
        urls$Base64.remove(id);
        resourceLocationMap$Base64.remove(id);
        File file = getTextureFile(id);
        if (file.exists()) file.delete();
        JsonObject data = new JsonObject();
        data.addProperty("url", "base64");
        data.addProperty("id", id);
        map.remove(data);
    }

    public static ResourceLocation getTexture$Base64(String base, String id) {
        id = formatUrls(id.toLowerCase());
        if (resourceLocationMap$Base64.containsKey(id)) {
            return resourceLocationMap$Base64.get(id);
        } else {
            if (!urls$Base64.getOrDefault(id, false)) {
                urls$Base64.put(id, true);
                String finalId = id;
                new Thread(() -> registerTexture$Base64(base, finalId, AlinLib.MINECRAFT.getTextureManager(), GuiUtils.getResourceLocation("waterplayer", finalId))).start();
            }
            return FILE_ICON;
        }
    }

    @Async.Execute
    public static void registerTexture$Base64(String base, String id, TextureManager textureManager, ResourceLocation textureId) {
        WaterPlayer.log(String.format("REGISTER: %s", id), Level.DEBUG);
        DynamicTexture texture;
        if (urlsTextures$Base64.containsKey(base)) {
            JsonObject data = new JsonObject();
            data.addProperty("url", "base64");
            data.addProperty("id", id);
            if (!map.contains(data)) map.add(data);
            texture = urlsTextures$Base64.get(base);
        } else {
            NativeImage image;
            try {
                File textureFile = getTextureFile(id);
                boolean isFileExists = textureFile.exists();
                BufferedImage bufferedImage;
                if (isFileExists) {
                    bufferedImage = ImageIO.read(getTextureFile(id));
                } else {
                    byte[] imageBytes = parseBase64Binary(base);
                    bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
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

                if (!isFileExists) {
                    Files.createDirectories(textureFile.toPath().getParent());
                    Files.write(textureFile.toPath(), bytesOfImage);
                }
            } catch (Exception e) {
                WaterPlayer.log("Error loading image from URL: " + id + " - " + e.getMessage());
                resourceLocationMap$Base64.put(id, NO_PLAYLIST_ICON);
                return;
            }
            texture = new DynamicTexture(image);
        }
        if (textureManager != null) {
            textureManager.register(textureId, texture);
            resourceLocationMap$Base64.put(id, textureId);
            JsonObject data = new JsonObject();
            data.addProperty("url", "base64");
            data.addProperty("id", id);
            if (!map.contains(data)) map.add(data);
        }
    }

    public static void registerTexture$Base64(String id, TextureManager textureManager, ResourceLocation textureId) {
        WaterPlayer.log(String.format("REGISTER: %s", id), Level.DEBUG);
        DynamicTexture texture;
        NativeImage image;
        try {
            File textureFile = getTextureFile(id);
            boolean isFileExists = textureFile.exists();
            BufferedImage bufferedImage;
            if (isFileExists) {
                bufferedImage = ImageIO.read(getTextureFile(id));
                if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
                    int x = (bufferedImage.getWidth() - bufferedImage.getHeight()) / 2;
                    bufferedImage = bufferedImage.getSubimage(x, 0, bufferedImage.getHeight(), bufferedImage.getHeight());
                }
                BufferedImage scaleImage = toBufferedImage(bufferedImage.getScaledInstance(128, 128, 2));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(scaleImage, "png", byteArrayOutputStream);
                byte[] bytesOfImage = byteArrayOutputStream.toByteArray();
                image = NativeImage.read(bytesOfImage);
            } else {
                resourceLocationMap$Base64.put(id, NO_PLAYLIST_ICON);
                return;
            }
        } catch (Exception e) {
            WaterPlayer.log("Error loading image from URL: " + id + " - " + e.getMessage());
            resourceLocationMap$Base64.put(id, NO_PLAYLIST_ICON);
            return;
        }
        texture = new DynamicTexture(image);
        if (textureManager != null) {
            textureManager.register(textureId, texture);
            resourceLocationMap$Base64.put(id, textureId);
            JsonObject data = new JsonObject();
            data.addProperty("url", "base64");
            data.addProperty("id", id);
            if (!map.contains(data)) map.add(data);
        }
    }


    public static File getTextureFile(String url) {
        return new File(WaterPlayer.getPath()+"/textures/" + url + ".png");
    }

    public static void saveMap() {
        try {
            Path path = new File(WaterPlayer.getPath()+"/textures/map.json").toPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, map.toString());
        } catch (IOException e) {
            WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
        }
    }

    public static void loadTextures(TextureManager textureManager) {
        loadMap();
        try {
            final JsonArray finalMap = map;
            for (JsonElement json : finalMap) {
                JsonObject data = json.getAsJsonObject();
                ResourceLocation l = GuiUtils.getResourceLocation("waterplayer", data.get("id").getAsString());
                if (new File(data.get("url").getAsString()).exists())
                    registerTexture$File(new File(data.get("url").getAsString()), data.get("id").getAsString(), textureManager, l);
                else if (data.get("url").getAsString().startsWith("base64")) registerTexture$Base64(data.get("id").getAsString(), textureManager, l);
                else registerTexture(data.get("url").getAsString(), data.get("id").getAsString(), textureManager, l);
            }
        } catch (Exception e){
            WaterPlayer.log("MAP ERROR!", Level.ERROR);
            e.printStackTrace();
        }
    }

    public static void loadMap() {
        File mapFile = new File(WaterPlayer.getPath()+"/textures/map.json");
        if (mapFile.exists() && mapFile.isFile()) {
            try {
                map = GsonHelper.parseArray(Files.readString(mapFile.toPath()));
            } catch (Exception e) {
                map = new JsonArray();
                WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            }
        } else map = new JsonArray();
    }

    public static String formatUrls(String url) {
        return url.toLowerCase().replaceAll(" ", "-").replaceAll("[^A-Za-z0-9_-]", "_");
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
    // -=-=-=-=-=-=-=-=-=- //
    public static void removePlaylistsIconCache(){
        File directory = new File(WaterPlayer.getPath()+"/textures");
        resourceLocationMap$Base64.clear();
        urls$Base64.clear();
        urlsTextures$Base64.clear();
        List<JsonElement> removed = new ArrayList<>();
        for(JsonElement element : map){
            JsonObject jsonObject = element.getAsJsonObject();
            if(jsonObject.get("url").getAsString().equals("base64")) removed.add(element);
        }
        for(JsonElement element : removed) map.remove(element);
        for(File file : directory.listFiles()){
            if(file.getName().startsWith("playlist") || file.getName().startsWith("webplaylist")) file.delete();
        }
        saveMap();
    }
    public static void removeTracksCache(){
        File directory = new File(WaterPlayer.getPath()+"/textures");
        resourceLocationMap.clear();
        urls.clear();
        urlsTextures.clear();
        resourceLocationMap$file.clear();
        urls$file.clear();
        urlsTextures$file.clear();
        List<JsonElement> removed = new ArrayList<>();
        for(JsonElement element : map){
            JsonObject jsonObject = element.getAsJsonObject();
            if(!jsonObject.get("url").getAsString().equals("base64")) removed.add(element);
        }
        for(JsonElement element : removed) map.remove(element);
        for(File file : directory.listFiles()){
            if((!file.getName().startsWith("playlist") && !file.getName().startsWith("webplaylist")) && !file.getName().equals("map.json")) file.delete();
        }
        saveMap();
    }
}
