package studio.trc.bukkit.litesignin.util.woodsignscript;

import java.util.List;

import lombok.Getter;

import studio.trc.bukkit.litesignin.util.woodsignscript.WoodSignUtil.WoodSignLine;

public class WoodSign
{
    @Getter
    private final String woodSignTitle;
    @Getter
    private final WoodSignLine woodSignText;
    @Getter
    private final List<String> woodSignCommand;
    @Getter
    private final String permission;
    
    public WoodSign(String woodSignTitle, WoodSignLine woodSignText, List<String> woodSignCommand, String permission) {
        this.woodSignCommand = woodSignCommand;
        this.woodSignText = woodSignText;
        this.woodSignTitle = woodSignTitle;
        this.permission = permission;
    }
}
