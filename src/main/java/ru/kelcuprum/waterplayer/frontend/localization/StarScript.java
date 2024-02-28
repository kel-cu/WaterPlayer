package ru.kelcuprum.waterplayer.frontend.localization;

import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.StandardLib;
import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;

public class StarScript {
    public static Starscript ss = new Starscript();

    static Minecraft mc = Minecraft.getInstance();

    public static void init() {
        StandardLib.init(ss);
        ss.set("minecraft", new ValueMap()
                        .set("version", SharedConstants.getCurrentVersion().getName())
                        .set("loader", mc.getVersionType())
        );
        // Player
        ss.set("player", new ValueMap()
                .set("volume", () -> Value.number(Music.getVolume()))
                .set("speaker_icon", () -> Value.string(Music.getSpeakerVolume()))
        );
        ss.set("format", new ValueMap()
                .set("time", () -> Value.string(Music.getIsLive() ? WaterPlayer.localization.getLocalization("format.live", true)
                        : WaterPlayer.localization.getLocalization("format.time", true)))
                .set("title", () -> Value.string(WaterPlayer.localization.getLocalization("format.title", true)))
                .set("author", () -> Value.string(WaterPlayer.localization.getLocalization("format.author", true)))
        );
        ss.set("track", new ValueMap()
                .set("title", () -> Value.string(Music.getTitle()))
                .set("author", () -> Value.string(Music.getAuthor()))
                .set("time", new ValueMap()
                        .set("position", () -> Value.string(getTimestamp(Music.getPosition())))
                        .set("duration", () -> Value.string(getTimestamp(Music.getDuration())))
                )
        );
    }
    // Helpers
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

    public static Script compile(String source) {
        Parser.Result result = Parser.parse(source);

        if (result.hasErrors()) {
            for (Error error : result.errors) WaterPlayer.log(error.message, Level.ERROR);
            return null;
        }

        return Compiler.compile(result);
    }

    public static Section runSection(Script script, StringBuilder sb) {
        try {
            return ss.run(script, sb);
        }
        catch (StarscriptError error) {
            error.printStackTrace();
            return null;
        }
    }
    public static String run(Script script, StringBuilder sb) {
        Section section = runSection(script, sb);
        return section != null ? section.toString() : "oops...";
    }

    public static Section runSection(Script script) {
        return runSection(script, new StringBuilder());
    }
    public static String run(Script script) {
        try {
            return run(script, new StringBuilder());
        } catch (Exception ex){
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
            return "oops...";
        }
    }

}
