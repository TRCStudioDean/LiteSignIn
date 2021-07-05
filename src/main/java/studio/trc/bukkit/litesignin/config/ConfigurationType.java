package studio.trc.bukkit.litesignin.config;

public enum ConfigurationType
{
    /**
     * Config.yml
     */
    CONFIG("Config.yml"),
    
    /**
     * Messages.yml
     */
    MESSAGES("Messages.yml"),
    
    /**
     * GUISettings.yml
     */
    GUISETTINGS("GUISettings.yml"),
    
    /**
     * RewardSettings.yml
     */
    REWARDSETTINGS("RewardSettings.yml"),
    
    /**
     * CustomItems.yml
     */
    CUSTOMITEMS("CustomItems.yml"),
    
    /**
     * WoodSignSettings.yml
     */
    WOODSIGNSETTINGS("WoodSignSettings.yml");
    
    private final String fileName;
    
    private ConfigurationType(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}
