package ru.kelcuprum.waterplayer.frontend.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.waterplayer.WaterPlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class TexturesHelper {
    public static HashMap<String, ResourceLocation> resourceLocationMap = new HashMap<>();
    public static ResourceLocation getTexture(String url, String id) {
        id = id.toLowerCase().replace(" ", "_").replace("/", "_").replace(":", "_");
        if(resourceLocationMap.containsKey(id)) return resourceLocationMap.get(id);
        NativeImage image;
        try {
//            URL imageURL = new URL(url);
//            InputStream imageStream = imageURL.openStream();
            BufferedImage bufferedImage = ImageIO.read(new URL(url));
            if(bufferedImage.getWidth() > bufferedImage.getHeight()){
                int x = (bufferedImage.getWidth()-bufferedImage.getHeight())/2;
                bufferedImage = bufferedImage.getSubimage(x, 0, bufferedImage.getHeight(), bufferedImage.getHeight());
            }
            BufferedImage scaleImage = toBufferedImage(bufferedImage.getScaledInstance(128, 128, 2));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(scaleImage, "png", byteArrayOutputStream);
            byte[] bytesOfImage = byteArrayOutputStream.toByteArray();
            image = NativeImage.read(bytesOfImage);
        } catch (Exception e) {
            WaterPlayer.log("Error loading image from URL: " + url + " - " + e.getMessage());
            e.printStackTrace();
            resourceLocationMap.put(id, new ResourceLocation("waterplayer", "textures/no_icon.png"));
            return resourceLocationMap.get(id);
        }
        TextureManager textureManager = AlinLib.MINECRAFT.getTextureManager();
        DynamicTexture texture = new DynamicTexture(image);
        ResourceLocation textureId = new ResourceLocation("waterplayer", id);
        textureManager.register(textureId, texture);
        resourceLocationMap.put(id, textureId);
        return textureId;
    }
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
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
