package studio.trc.bukkit.litesignin.message;

import lombok.Getter;

import net.md_5.bungee.api.chat.BaseComponent;

public class MessageSection
{
    @Getter
    private final int startsWith;
    @Getter
    private final int endsWith;
    @Getter
    private final BaseComponent bungeeComponent;
    @Getter
    private final Object adventureComponent;
    @Getter
    private final String text;
    @Getter
    private final String placeholder;
    
    public MessageSection(String text, String placeholder, int startsWith, int endsWith) {
        this.text = text;
        this.startsWith = startsWith;
        this.endsWith = endsWith;
        this.placeholder = placeholder;
        bungeeComponent = null;
        adventureComponent = null;
    }

    public MessageSection(BaseComponent bungeeComponent, String placeholder, int startsWith, int endsWith) {
        this.startsWith = startsWith;
        this.endsWith = endsWith;
        this.placeholder = placeholder;
        this.bungeeComponent = bungeeComponent;
        text = null;
        adventureComponent = null;
    }

    public MessageSection(Object adventureComponent, String placeholder, int startsWith, int endsWith) {
        this.startsWith = startsWith;
        this.endsWith = endsWith;
        this.placeholder = placeholder;
        this.adventureComponent = adventureComponent;
        text = null;
        bungeeComponent = null;
    }

    public boolean isPlaceholder() {
        return placeholder != null;
    }

    @Override
    public String toString() {
        return text;
    }
}