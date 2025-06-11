package studio.trc.bukkit.litesignin.message.color;

import java.awt.Color;
import java.util.List;

import lombok.Getter;

import net.md_5.bungee.api.ChatColor;

import studio.trc.bukkit.litesignin.message.tag.TagContentExtractor;
import studio.trc.bukkit.litesignin.message.tag.TagContentInfo;


public class RainbowColor
    implements FunctionalColor
{
    @Getter
    private static final RainbowColor instance = new RainbowColor();
    
    @Override
    public String coloring(String content) {
        String original = content;
        List<TagContentInfo> colorTags = TagContentExtractor.getTagContentsInfo(content, "rainbow");
        colorTags.addAll(TagContentExtractor.getTagContentsInfo(content, "rainbow", true, false));
        for (TagContentInfo tagContent : colorTags) {
            boolean reverse;
            String text = tagContent.getContent();
            String offsetX;
            String offsetY;
            if (tagContent.getAttribute() != null) {
                reverse = tagContent.getAttribute().substring(0, 1).equals("!");
                String[] parameters = (reverse ? tagContent.getAttribute().substring(1) : tagContent.getAttribute()).split(":", 2);
                if (parameters.length == 2) {
                    offsetX = parameters[0].isEmpty() ? null : parameters[0];
                    offsetY = parameters[1];
                } else {
                    offsetX = parameters[0].isEmpty() ? null : parameters[0];
                    offsetY = null;
                }
            } else {
                reverse = false;
                offsetX = null;
                offsetY = null;
            }
            ChatColor[] colors = makeRainbow(text.length(), offsetX != null ? Float.valueOf(offsetX) : 0F, offsetY != null ? Float.valueOf(offsetY) : 20F, reverse);
            content = tagContent.replace(content, (tagContent.getCloseTag() != null ? "<previousColor>" : "") + ColorUtils.coloring(text, colors, ColorUtils.getPreviousTypeface(original, tagContent.getStartPosition())) + (tagContent.getCloseTag() != null ? "</previousColor>" : ""));
        }
        return content;
    }
    
    public ChatColor[] makeRainbow(int depth, float offsetX, float offsetY, boolean reverse) {
        ChatColor[] colors = new ChatColor[depth];
        if (offsetY == 0) offsetX = offsetY = 1; //Prevent division from being zero
        float offset = reverse ? -1 * offsetX / offsetY : offsetX / offsetY;
        for (int i = 0; i < depth; i++) {
            if (ColorUtils.isSupportsRGBVersions()) {
                colors[i] = ChatColor.of(Color.getHSBColor(1F / depth * i + offset, 1F, 1F));
            } else {
                colors[i] = ChatColor.getByChar(ColorUtils.toNearestColor(Color.getHSBColor(1F / depth * i + offset, 1F, 1F)));
            }
        }
        return colors;
    }
}
