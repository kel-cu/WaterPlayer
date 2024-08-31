package ru.kelcuprum.waterplayer.backend.playlist;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.info.Player;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static ru.kelcuprum.waterplayer.frontend.gui.TextureHelper.toBufferedImage;

public class Playlist {
    public String title;
    public String author;
    public JsonArray urlsJSON;
    public List<String> urls = new ArrayList<>();
    public Path path;
    public String icon;
    public String fileName = "Unknown";

    public Playlist(String name) throws IOException {
        this(AlinLib.MINECRAFT.gameDirectory.toPath().resolve(WaterPlayer.getPath()+"/playlists/"+name+".json"));
    }

    public Playlist(Path path) throws IOException {
        this(path.toFile().exists() ? GsonHelper.parse(Files.readString(path)) : new JsonObject());
        this.path = path;
        this.fileName = path.getFileName().toString();
        this.fileName = fileName.substring(0, fileName.length()-5);
    }

    public Playlist(JsonObject data){
        title = data.has("title") ? data.get("title").getAsString() : "Example title";
        author = data.has("author") ? data.get("author").getAsString() : Player.getName();
        urlsJSON = data.has("urls") ? data.get("urls").getAsJsonArray() : GsonHelper.parseArray("[\"https://www.youtube.com/watch?v=2bjBl-nX1oc\"]");
        icon = data.has("icon") ? data.get("icon").getAsString() : null;
        for(int i = 0; i < urlsJSON.size(); i++){
            urls.add(urlsJSON.get(i).getAsString());
        }
    }
    public static boolean isValid(JsonObject data){
        return data.has("title") && data.has("author") && data.has("urls");
    }

    public void save(){
        if(this.path == null) return;
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, toJSON().toString());
        } catch (IOException e) {
            WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
        }
    }

    public Playlist addUrl(String url){
        urls.add(url);
        save();
        return this;
    }
    public Playlist setUrl(String url, int position){
        urls.set(position, url);
        save();
        return this;
    }

    public void setIcon(File path){
        try {
            BufferedImage bufferedImage = ImageIO.read(path);
            if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
                int x = (bufferedImage.getWidth() - bufferedImage.getHeight()) / 2;
                bufferedImage = bufferedImage.getSubimage(x, 0, bufferedImage.getHeight(), bufferedImage.getHeight());
            }
            BufferedImage scaleImage = toBufferedImage(bufferedImage.getScaledInstance(128, 128, 2));
            if(TextureHelper.resourceLocationMap$Base64.containsKey(String.format("playlist-%s", fileName))) TextureHelper.remove$Base64(String.format("playlist-%s", fileName), this.icon);
            this.icon = encodeToString(scaleImage);
            save();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    protected String encodeToString(BufferedImage image) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            imageString = Base64.getEncoder().encodeToString(imageBytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    public JsonObject toJSON(){
        JsonObject data = new JsonObject();
        data.addProperty("title", title);
        data.addProperty("author", author);
        if(icon != null) data.addProperty("icon", icon);
        data.add("urls", getUrlsJSON());
        return data;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public JsonArray getUrlsJSON(){
        JsonArray array = new JsonArray();
        for(String url : urls){
            if(!url.isBlank()) array.add(url);
        }
        return array;
    }
}
