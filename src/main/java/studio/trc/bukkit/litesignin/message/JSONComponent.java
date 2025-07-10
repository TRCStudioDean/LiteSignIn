package studio.trc.bukkit.litesignin.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import studio.trc.bukkit.litesignin.util.AdventureUtils;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;

public class JSONComponent
{
    @Getter
    private final String text;
    @Getter
    private final String clickAction;
    @Getter
    private final String clickContent;
    @Getter
    private final List<String> hoverContent;
    
    private BaseComponent bungeeComponent = null;
    private Object adventureComponent = null;

    public JSONComponent(String text, List<String> hoverContent, String clickAction, String clickContent) {
        this.text = text;
        this.hoverContent = hoverContent;
        this.clickAction = clickAction;
        this.clickContent = clickContent;
    }
    
    public BaseComponent getBungeeComponent() {
        if (bungeeComponent == null) {
            net.md_5.bungee.api.chat.HoverEvent hoverEvent = null;
            net.md_5.bungee.api.chat.ClickEvent clickEvent = null;
            try {
                if (!hoverContent.isEmpty()) {
                    List<BaseComponent> hoverText = new ArrayList<>();
                    int end = 0;
                    for (String hover : hoverContent) {
                        end++;
                        hoverText.add(new TextComponent(MessageUtil.doBasicProcessing(hover)));
                        if (end != hoverContent.size()) {
                            hoverText.add(new TextComponent("\n"));
                        }
                    }
                    hoverEvent = new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[0]));
                }
                if (clickAction != null) {
                    clickEvent = new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.valueOf(clickAction), MessageUtil.doBasicProcessing(clickContent));
                }
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
                ex.printStackTrace();
            }
            bungeeComponent = new TextComponent(MessageUtil.doBasicProcessing(text));
            if (hoverEvent != null) bungeeComponent.setHoverEvent(hoverEvent);
            if (clickEvent != null) bungeeComponent.setClickEvent(clickEvent);
        }
        return bungeeComponent;
    }
    
    public Object getAdventureComponent() {
        if (adventureComponent == null) {
            HoverEvent hoverEvent = null;
            ClickEvent clickEvent = null;
            try {
                if (!hoverContent.isEmpty()) {
                    hoverEvent = AdventureUtils.showText(String.join("\n", hoverContent.stream().map(hover -> MessageUtil.doBasicProcessing(hover)).collect(Collectors.toList())));
                }
                if (clickAction != null) {
                    clickEvent = AdventureUtils.getClickEvent(clickAction, MessageUtil.doBasicProcessing(clickContent));
                }
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
                ex.printStackTrace();
            }
            Component component = AdventureUtils.serializeText(MessageUtil.doBasicProcessing(text));
            if (hoverEvent != null) component = AdventureUtils.setHoverEvent(component, hoverEvent);
            if (clickEvent != null) component = AdventureUtils.setClickEvent(component, clickEvent);
            adventureComponent = component;
        }
        return adventureComponent;
    }
    
    public BaseComponent getBungeeComponent(Map<String, String> placeholders) {
        net.md_5.bungee.api.chat.HoverEvent hoverEvent = null;
        net.md_5.bungee.api.chat.ClickEvent clickEvent = null;
        try {
            if (!hoverContent.isEmpty()) {
                List<BaseComponent> hoverText = new ArrayList<>();
                int end = 0;
                for (String hover : hoverContent) {
                    end++;
                    hoverText.add(new TextComponent(MessageUtil.replacePlaceholders(hover, placeholders)));
                    if (end != hoverContent.size()) {
                        hoverText.add(new TextComponent("\n"));
                    }
                }
                hoverEvent = new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, hoverText.toArray(new BaseComponent[0]));
            }
            if (clickAction != null) {
                clickEvent = new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.valueOf(clickAction), MessageUtil.replacePlaceholders(clickContent, placeholders));
            }
        } catch (Exception ex) {
            placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
            LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
            ex.printStackTrace();
        }
        BaseComponent processedComponent = new TextComponent(MessageUtil.doBasicProcessing(text));
        if (hoverEvent != null) processedComponent.setHoverEvent(hoverEvent);
        if (clickEvent != null) processedComponent.setClickEvent(clickEvent);
        return processedComponent;
    }
    
    public Object getAdventureComponent(Map<String, String> placeholders) {
        try {
            HoverEvent hoverEvent = null;
            ClickEvent clickEvent = null;
            try {
                if (!hoverContent.isEmpty()) {
                    hoverEvent = AdventureUtils.showText(String.join("\n", hoverContent.stream().map(hover -> MessageUtil.replacePlaceholders(hover, placeholders)).collect(Collectors.toList())));
                }
                if (clickAction != null) {
                    clickEvent = AdventureUtils.getClickEvent(clickAction, MessageUtil.replacePlaceholders(clickContent, placeholders));
                }
            } catch (Exception ex) {
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
                ex.printStackTrace();
            }
            Component component = AdventureUtils.serializeText(MessageUtil.replacePlaceholders(text, placeholders));
            if (hoverEvent != null) component = AdventureUtils.setHoverEvent(component, hoverEvent);
            if (clickEvent != null) component = AdventureUtils.setClickEvent(component, clickEvent);
            return component;
        } catch (Exception ex) {
            placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
            LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
            ex.printStackTrace();
        }
        return null;
    }
}
