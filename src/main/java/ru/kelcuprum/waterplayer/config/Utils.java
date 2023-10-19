package ru.kelcuprum.waterplayer.config;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {
    private static final int codes = 22;
    public static Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final Map<String, String> formatCodes = IntStream.range(0, codes)
            .boxed()
            .collect(Collectors.toMap(List.of(new String[]{
                    "&4", "&c", "&6", "&e", "&z", "&a", "&b", "&3", "&1", "&9", "&d", "&5", "&f", "&7", "&8", "&0",
                    "&r", "&l", "&o", "&n", "&m", "&k"
            })::get, List.of(new String[]{
                    "§4", "§c", "§6", "§e", "§z", "§a", "§b", "§3", "§1", "§9", "§d", "§5", "§f", "§7", "§8", "§0",
                    "§r", "§l", "§o", "§n", "§m", "§k"
            })::get));

    public static String clearFormatCodes(String text) {
        return text.replaceAll("([§&][a-f0-9k-orz])", "").replaceAll("#[a-fA-F0-9]{6}", "");
    }

    public static String fixFormatCodes(String text) {
        for (String formatCode : formatCodes.keySet()) {
            text = text.replaceAll(formatCode, formatCodes.get(formatCode));
        }
        return text;
    }

    public static String parseRGB(String from) {
        Matcher matcher = pattern.matcher(from);
        while (matcher.find()) {
            String hexCode = from.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch)
                builder.append("§" + c);
            from = from.replace(hexCode, builder.toString());
            matcher = pattern.matcher(from);
        }
        String pref = Utils.fixFormatCodes(from);
        return pref;
    }
}
