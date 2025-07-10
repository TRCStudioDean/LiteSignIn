package studio.trc.bukkit.litesignin.message.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;

import studio.trc.bukkit.litesignin.message.tag.TagContentExtractor;
import studio.trc.bukkit.litesignin.message.tag.TagContentInfo;

/**
 * Text coloring & "Pseudo MiniMessage" processing
 * @author Mercy
 */
public class ColorUtils 
{
    @Getter
    private static final Map<Color, Character> colorRGBValues = new HashMap<>();
    @Getter
    private static final Map<String, String> colorAndTypefaceNames = new HashMap<>();
    @Getter
    private static final Map<String, String> colorNames = new HashMap<>();
    @Getter
    private static final Map<String, String> typefaceNames = new HashMap<>();
    @Getter
    private static final List<FunctionalColor> colors = new ArrayList<>();
    
    static {
        colorRGBValues.put(new Color(0), '0');
        colorRGBValues.put(new Color(170), '1');
        colorRGBValues.put(new Color(43520), '2');
        colorRGBValues.put(new Color(43690), '3');
        colorRGBValues.put(new Color(11141120), '4');
        colorRGBValues.put(new Color(11141290), '5');
        colorRGBValues.put(new Color(16755200), '6');
        colorRGBValues.put(new Color(11184810), '7');
        colorRGBValues.put(new Color(5592405), '8');
        colorRGBValues.put(new Color(5592575), '9');
        colorRGBValues.put(new Color(5635925), 'a');
        colorRGBValues.put(new Color(5636095), 'b');
        colorRGBValues.put(new Color(16733525), 'c');
        colorRGBValues.put(new Color(16733695), 'd');
        colorRGBValues.put(new Color(16777045), 'e');
        colorRGBValues.put(new Color(16777215), 'f');
        //Colors
        colorNames.put("black", "§0");
        colorNames.put("dark_blue", "§1");
        colorNames.put("dark_green", "§2");
        colorNames.put("dark_aqua", "§3");
        colorNames.put("dark_red", "§4");
        colorNames.put("dark_purple", "§5");
        colorNames.put("gold", "§6");
        colorNames.put("gray", "§7");
        colorNames.put("dark_gray", "§8");
        colorNames.put("blue", "§9");
        colorNames.put("green", "§a");
        colorNames.put("aqua", "§b");
        colorNames.put("red", "§c");
        colorNames.put("light_purple", "§d");
        colorNames.put("yellow", "§e");
        colorNames.put("white", "§f");
        //Reset
        colorNames.put("reset", "§r");
        //Typefaces (Decorations)
        typefaceNames.put("bold", "§l");
        typefaceNames.put("italic", "§o");
        typefaceNames.put("underline", "§n");
        typefaceNames.put("strikethrough", "§m");
        typefaceNames.put("obfuscated", "§k");
        typefaceNames.put("b", "§l");
        typefaceNames.put("u", "§n");
        typefaceNames.put("em", "§o");
        typefaceNames.put("i", "§o");
        typefaceNames.put("st", "§m");
        typefaceNames.put("obf", "§k");
        //Connect two maps
        colorAndTypefaceNames.putAll(colorNames);
        colorAndTypefaceNames.putAll(typefaceNames);
        //Functional colors
        colors.add(TagColor.getInstance());
        colors.add(RainbowColor.getInstance());
        colors.add(GradientColor.getInstance());
        colors.add(TransitionColor.getInstance());
    }
    
    /**
     * Get the closest classic color to it
     * @param color java.awt.Color instance
     * @return Hex color -> Classic color
     */
    public static char toNearestColor(Color color) {
        double min = -1;
        char result = 'r';
        for (Color targetColor : colorRGBValues.keySet()) {
            double redDistance = Math.pow(targetColor.getRed() - color.getRed(), 2);
            double greenDistance = Math.pow(targetColor.getGreen() - color.getGreen(), 2);
            double blueDistance = Math.pow(targetColor.getBlue() - color.getBlue(), 2);
            double distance = Math.abs(Math.sqrt(redDistance + greenDistance + blueDistance));
            if (min == -1 || distance < min) {
                min = distance;
                result = colorRGBValues.get(targetColor);
            }
        }
        return result;
    }
    
    /**
     * Get the closest classic color to it
     * @param hexadecimalColor String color
     * @return Hex color -> Classic color
     */
    public static char toNearestColor(String hexadecimalColor) {
        if (isHexadecimalSequence(hexadecimalColor, 1, 6)) {
            return toNearestColor(new Color(Integer.parseInt(hexadecimalColor.substring(1), 16)));
        }
        return 'r';
    }
    
    /**
     * 1.15 below: Classic color
     * 1.16 above: Hex color & classic color
     * @return 
     */
    public static boolean isSupportsRGBVersions() {
        return !Bukkit.getBukkitVersion().startsWith("1.7") && !Bukkit.getBukkitVersion().startsWith("1.8") && !Bukkit.getBukkitVersion().startsWith("1.9") && !Bukkit.getBukkitVersion().startsWith("1.10") &&
                !Bukkit.getBukkitVersion().startsWith("1.11") && !Bukkit.getBukkitVersion().startsWith("1.12") && !Bukkit.getBukkitVersion().startsWith("1.13") && !Bukkit.getBukkitVersion().startsWith("1.14") && !Bukkit.getBukkitVersion().startsWith("1.15");
    }
    
    /**
     * Coloring of text
     * @param text
     * @return 
     */
    public static String toColor(String text) {
        if (text == null) return null;
        try {
            //Preliminary coloring
            String content = ChatColor.translateAlternateColorCodes('&', text);
            
            //Functional coloring (Tag identification)
            for (FunctionalColor function : colors) content = function.coloring(content);
            TagContentInfo previousColor;
            while ((previousColor = TagContentExtractor.getTagContentInfo(content, "<previousColor>", "</previousColor>", false)) != null) {
                String originalText = content;
                content = previousColor.replace(content, previousColor.getContent() + ColorUtils.getPreviousColorAndTypeface(content, previousColor.getStartPosition()));
                if (content.equals(originalText)) break;
            }
            
            //Hexadecimal coloring
            List<String> hexadecimalColors = getHexadecimalColors(content);
            for (String color : hexadecimalColors) {
                content = content.replace(color, isSupportsRGBVersions() ? ChatColor.of(color).toString() : ChatColor.getByChar(toNearestColor(color)).toString());
            }
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return text;
        }
    }

    /**
     * Retrieve the contents of the color array and independently color each character of the string.
     * @param text Text
     * @param colors Color array
     * @param previousTypeface Previous typeface of text
     * @return 
     */
    public static String coloring(String text, ChatColor[] colors, String previousTypeface) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder builder = new StringBuilder();
        String colorName = "";
        String[] characters = text.split("");
        for (int charIndex = 0, colorIndex = 0; charIndex < characters.length; charIndex++) {
            if ((characters[charIndex].equals("&") || characters[charIndex].equals("§")) && charIndex + 1 < characters.length) {
                if (characters[charIndex + 1].equalsIgnoreCase("r")) {
                    colorName = "";
                } else {
                    colorName+=characters[charIndex];
                    colorName+=characters[charIndex + 1];
                }
                charIndex++;
            } else {
                builder.append(colors[colorIndex++]).append(previousTypeface).append(colorName).append(characters[charIndex]);
            }
        }
        return builder.toString();
    }
    
    /**
     * Get the most recent color and typeface before this content
     * @param content Text
     * @param start Start index
     * @return 
     */
    public static String getPreviousColorAndTypeface(String content, int start) {
        String colorName = "§r";
        String previousText = content.substring(0, start);
        for (String color : getColorAndTypefaceSymbols(previousText)) {
            if (isTypefaceSymbol(color, 1)) {
                colorName += color;
            } else {
                colorName = color;
            }
        }
        return colorName;
    }
    
    /**
     * Get the most recent typeface before this content
     * @param content Text
     * @param start Start index
     * @return 
     */
    public static String getPreviousTypeface(String content, int start) {
        String typefaceName = "";
        String previousText = content.substring(0, start);
        for (String color : getColorAndTypefaceSymbols(previousText)) {
            if (isColorSymbol(color, 1)) {
                typefaceName = "";
            } else {
                typefaceName += color;
            }
        }
        return typefaceName;
    }
    
    /**
     * String -> Color
     * @param colorName
     * @return 
     */
    public static Color getColor(String colorName) {
        if (colorNames.containsKey(colorName.toLowerCase())) {
            return colorRGBValues.keySet().stream().filter(color -> colorRGBValues.get(color).toString().equals(colorNames.get(colorName.toLowerCase()).substring(1))).findFirst().get();
        } else if (isHexadecimalSequence(colorName, 1, 6)) {
            return new Color(Integer.parseInt(colorName.substring(1), 16));
        } else {
            return colorRGBValues.keySet().stream().filter(color -> colorRGBValues.get(color).toString().equals(colorName.replace("&", "").replace("§", ""))).findFirst().orElse(new Color(16777215));
        }
    }
    
    private static List<String> getHexadecimalColors(String text) {
        List<String> hexadecimalColors = new ArrayList<>();
        if (text == null || text.length() < 7) {
            return hexadecimalColors;
        }
        int i = 0;
        while (i <= text.length() - 7) {
            if (text.charAt(i) == '#' && isHexadecimalSequence(text, i + 1, 6)) {
                hexadecimalColors.add(text.substring(i, i + 7));
                i += 7;
            } else {
                i++;
            }
        }
        return hexadecimalColors;
    }
    
    private static List<String> getColorSymbols(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.length() < 2) {
            return result;
        }
        int i = 0;
        while (i <= text.length() - 1) {
            if (text.charAt(i) == '§' && isColorSymbol(text, i + 1)) {
                result.add(text.substring(i, i + 2));
                i += 2;
            } else {
                i++;
            }
        }
        return result;
    }
    
    private static List<String> getTypefaceSymbols(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.length() < 2) {
            return result;
        }
        int i = 0;
        while (i <= text.length() - 1) {
            if (text.charAt(i) == '§' && isTypefaceSymbol(text, i + 1)) {
                result.add(text.substring(i, i + 2));
                i += 2;
            } else {
                i++;
            }
        }
        return result;
    }
    
    private static List<String> getColorAndTypefaceSymbols(String text) {
        List<String> result = new ArrayList<>();
        result.addAll(getColorSymbols(text));
        result.addAll(getTypefaceSymbols(text));
        return result;
    }
    
    private static boolean isHexadecimalSequence(String text, int start, int length) {
        if (start + length > text.length()) {
            return false;
        }
        for (int i = start; i < start + length; i++) {
            char c = text.charAt(i);
            if (!((c >= '0' && c <= '9') || 
                  (c >= 'a' && c <= 'f') || 
                  (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isColorSymbol(String text, int index) {
        char c = Character.toLowerCase(text.charAt(index));
        return ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || c == 'r');
    }
    
    private static boolean isTypefaceSymbol(String text, int index) {
        char c = Character.toLowerCase(text.charAt(index));
        return c == 'l' || c == 'o' ||
            c == 'n' || c == 'm' ||
            c == 'k';
    }
}
