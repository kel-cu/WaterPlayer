
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

import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.NO_ICON;

public class TextureHelper {
    public static HashMap<String, ResourceLocation> resourceLocationMap = new HashMap<>();
    public static HashMap<String, Boolean> urls = new HashMap<>();
    public static HashMap<String, DynamicTexture> urlsTextures = new HashMap<>();
    public static JsonArray map = new JsonArray();

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
            registerTexture(data.get("url").getAsString(), data.get("id").getAsString(), textureManager, l);
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
        return url.replace(" ", "_").replace("/", "_").replace(":", "_");
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
