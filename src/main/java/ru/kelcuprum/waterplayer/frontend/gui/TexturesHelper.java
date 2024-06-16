package ru.kelcuprum.waterplayer.frontend.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Async;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.waterplayer.WaterPlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;

public class TexturesHelper {
    public static HashMap<String, ResourceLocation> resourceLocationMap = new HashMap<>();
    public static HashMap<String, Boolean> urls = new HashMap<>();
    public static HashMap<String, DynamicTexture> urlsTextures = new HashMap<>();

    public static ResourceLocation getTexture(String url, String id) {
        id = formatUrls(id.toLowerCase());
        if (resourceLocationMap.containsKey(id)) return resourceLocationMap.get(id);
        else {
            if (!urls.getOrDefault(id, false)) {
                urls.put(id, true);
                String finalId = id;
                new Thread(() -> registerTexture(url, finalId, InterfaceUtils.getResourceLocation("waterplayer", finalId))).start();
            }
            return InterfaceUtils.getResourceLocation("waterplayer", "textures/no_icon.png");
        }
    }

    @Async.Execute
    public static void registerTexture(String url, String id, ResourceLocation textureId) {
        DynamicTexture texture;
        if(urlsTextures.containsKey(url)) texture = urlsTextures.get(url);
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
                e.printStackTrace();
                resourceLocationMap.put(id, InterfaceUtils.getResourceLocation("waterplayer", "textures/no_icon.png"));
                return;
            }
            texture = new DynamicTexture(image);
        }
        TextureManager textureManager = AlinLib.MINECRAFT.getTextureManager();
        textureManager.register(textureId, texture);
        resourceLocationMap.put(id, textureId);
    }

    public static File getTextureFile(String url) {
        return new File("config/waterplayer/textures/" + url + ".png");
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
