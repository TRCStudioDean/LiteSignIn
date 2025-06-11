package studio.trc.bukkit.litesignin.message.tag;

import lombok.Getter;

public class TagContentInfo 
{
    @Getter
    private final String content;
    @Getter
    private final String openTag;
    @Getter
    private final String closeTag;
    @Getter
    private final int startPosition;
    @Getter
    private final int endPosition;
    @Getter
    private String attribute = null;

    public TagContentInfo(String content, String openTag, String closeTag, int startPosition, int endPosition) {
        this.content = content;
        this.openTag = openTag;
        this.closeTag = closeTag;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    public String replace(String text, String replacement) {
        return text.replace((attribute == null ? openTag : openTag.substring(0, openTag.length() - 1) + ":" + attribute + ">") + content + (closeTag == null ? "" : closeTag), replacement);
    }
    
    public String getTagName() {
        return openTag.substring(1, openTag.length() - 1).split(":", -1)[0];
    }
    
    public TagContentInfo setAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }
}
