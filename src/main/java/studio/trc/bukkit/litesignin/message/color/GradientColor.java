package studio.trc.bukkit.litesignin.message.color;

import java.awt.Color;
import java.util.List;

import lombok.Getter;

import net.md_5.bungee.api.ChatColor;
import studio.trc.bukkit.litesignin.message.tag.TagContentExtractor;
import studio.trc.bukkit.litesignin.message.tag.TagContentInfo;


public class GradientColor 
    implements FunctionalColor
{
    @Getter
    private static final GradientColor instance = new GradientColor();
    
    @Override
    public String coloring(String content) {
        String original = content;
        List<TagContentInfo> colorTags = TagContentExtractor.getTagContentsInfo(content, "gradient");
        for (TagContentInfo tagContent : colorTags) {
            if (tagContent.getAttribute() == null) continue;
            String[] colorList = tagContent.getAttribute().split(":", -1);
            String text = tagContent.getContent();
            ChatColor[] colors = makeGradient(colorList, text.length());
            content = tagContent.replace(content, (tagContent.getCloseTag() != null ? "<previousColor>" : "") + ColorUtils.coloring(text, colors, ColorUtils.getPreviousTypeface(original, tagContent.getStartPosition())) + (tagContent.getCloseTag() != null ? "</previousColor>" : ""));
        }
        return content;
    }
    
    public static ChatColor[] makeGradient(String[] containsColors, int depth) {
        ChatColor[] result = new ChatColor[depth];
        Color[] colors = new Color[depth];
        if (depth == 0 || containsColors.length == 0) return result;
        if (containsColors.length == 1) {
            Color color = ColorUtils.getColor(containsColors[0]);
            for (int i = 0;i < depth;i++) {
                colors[i] = color;
            }
        } else if (depth <= containsColors.length) {
            for (int i = 0;i < depth;i++) {
                int index = (int) ((containsColors.length - 1) * (i / (double) (depth - 1)));
                colors[i] = ColorUtils.getColor(containsColors[index]);
            }
        } else {
            for (int i = 0;i < depth;i++) {
                float ratio = (float) i / (depth - 1);
                if (ratio <= 0) {
                    colors[i] = ColorUtils.getColor(containsColors[0]);
                } else if (ratio >= 1) {
                    colors[i] = ColorUtils.getColor(containsColors[containsColors.length - 1]);
                } else {
                    float segment = 1.0F / (containsColors.length - 1);
                    int index = (int) (ratio / segment);
                    float localRatio = (ratio - segment * index) / segment;
                    Color c1 = ColorUtils.getColor(containsColors[index]);
                    Color c2 = ColorUtils.getColor(containsColors[index + 1]);
                    colors[i] = new Color(
                        (int) (c1.getRed() + localRatio * (c2.getRed() - c1.getRed())),
                        (int) (c1.getGreen() + localRatio * (c2.getGreen() - c1.getGreen())),
                        (int) (c1.getBlue() + localRatio * (c2.getBlue() - c1.getBlue()))
                    );
                }
            }
        }
        for (int i = 0;i < result.length;i++) {
            if (ColorUtils.isSupportsRGBVersions()) {
                result[i] = ChatColor.of(colors[i]);
            } else {
                result[i] = ChatColor.getByChar(ColorUtils.toNearestColor(colors[i]));
            }
        }
        return result;
    }
}
