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
    GUI_SETTINGS("GUISettings.yml"),
    
    /**
     * RewardSettings.yml
     */
    REWARD_SETTINGS("RewardSettings.yml"),
    
    /**
     * CustomItems.yml
     */
    CUSTOM_ITEMS("CustomItems.yml"),
    
    /**
     * WoodSignSettings.yml
     */
    WOOD_SIGN_SETTINGS("WoodSignSettings.yml");
    
    private final String fileName;
    
    private ConfigurationType(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}
