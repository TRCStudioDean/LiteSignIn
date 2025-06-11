package studio.trc.bukkit.litesignin.message.color;

import java.util.List;
import java.util.Map;

import lombok.Getter;

import net.md_5.bungee.api.ChatColor;

import studio.trc.bukkit.litesignin.message.tag.TagContentExtractor;
import studio.trc.bukkit.litesignin.message.tag.TagContentInfo;

public class TagColor 
    implements FunctionalColor
{
    @Getter
    private static final TagColor instance = new TagColor();
    
    @Override
    public String coloring(String content) {
        Map<String, String> colorAndTypefaceNames = ColorUtils.getColorAndTypefaceNames();
        // Tag with color name
        List<TagContentInfo> colorTags = TagContentExtractor.getTagContentsInfo(content, "color");
        for (TagContentInfo tagContent : colorTags) {
            if (tagContent.getAttribute() == null) continue;
            String text = tagContent.getContent();
            String color = tagContent.getAttribute().toLowerCase();
            if (colorAndTypefaceNames.containsKey(color)) { //Example: <color:red>
                content = tagContent.replace(content, (tagContent.getCloseTag() != null ? "<previousColor>" : "") + colorAndTypefaceNames.get(color) + text + (tagContent.getCloseTag() != null ? "</previousColor>" : ""));
            } else if (color.startsWith("#")) { //Example: <color:#FF0000>
                content = tagContent.replace(content, (tagContent.getCloseTag() != null ? "<previousColor>" : "") + (ColorUtils.isSupportsRGBVersions() ? ChatColor.of(color).toString() : ChatColor.getByChar(ColorUtils.toNearestColor(color))).toString() + text + (tagContent.getCloseTag() != null ? "</previousColor>" : ""));
            }
        }
        // Color name as tag
        for (String symbols : colorAndTypefaceNames.keySet()) {
            List<TagContentInfo> contents = TagContentExtractor.getTagContentsInfo(content, symbols, true, false);
            for (TagContentInfo tagContent : contents) {
                String text = tagContent.getContent();
                String color = tagContent.getTagName();
                content = tagContent.replace(content, (tagContent.getCloseTag() != null ? "<previousColor>" : "") + colorAndTypefaceNames.get(color) + text + (tagContent.getCloseTag() != null ? "</previousColor>" : ""));
            }
        }
        return content;
    }
}
