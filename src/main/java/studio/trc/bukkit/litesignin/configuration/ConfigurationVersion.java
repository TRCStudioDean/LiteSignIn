package studio.trc.bukkit.litesignin.configuration;

import lombok.Getter;

public enum ConfigurationVersion
{
    GUI_SETTINGS_V2(ConfigurationType.GUI_SETTINGS, null, "GUISettings-OLDVERSION.yml",
    VersionType.V1_7_R1, VersionType.V1_7_R2, VersionType.V1_7_R3, VersionType.V1_7_R4,
    VersionType.V1_8_R1, VersionType.V1_8_R2, VersionType.V1_8_R3,
    VersionType.V1_9_R1, VersionType.V1_9_R2,
    VersionType.V1_10_R1,
    VersionType.V1_11_R1,
    VersionType.V1_12_R1),
    
    GUI_SETTINGS_V1(ConfigurationType.GUI_SETTINGS, null, "GUISettings-NEWVERSION.yml"),
    
    REWARD_SETTINGS_V2(ConfigurationType.REWARD_SETTINGS, null, "RewardSettings-OLDVERSION.yml",
    VersionType.V1_7_R1, VersionType.V1_7_R2, VersionType.V1_7_R3, VersionType.V1_7_R4,
    VersionType.V1_8_R1, VersionType.V1_8_R2, VersionType.V1_8_R3,
    VersionType.V1_9_R1, VersionType.V1_9_R2,
    VersionType.V1_10_R1,
    VersionType.V1_11_R1,
    VersionType.V1_12_R1),
    
    REWARD_SETTINGS_V1(ConfigurationType.REWARD_SETTINGS, null, "RewardSettings-NEWVERSION.yml");
    
    @Getter
    private final String folder;
    @Getter
    private final String fileName;
    @Getter
    private final ConfigurationType type;
    @Getter
    private final VersionType[] versions;

    private ConfigurationVersion(ConfigurationType type, String folder, String fileName, VersionType... versions) {
        this.type = type;
        if (folder != null) {
            this.folder = folder + "/";
        } else {
            this.folder = "";
        }
        this.fileName = fileName;
        this.versions = versions;
    }
    
    public static enum VersionType {
        V1_7_R1, V1_7_R2, V1_7_R3, V1_7_R4,
        V1_8_R1, V1_8_R2, V1_8_R3,
        V1_9_R1, V1_9_R2,
        V1_10_R1,
        V1_11_R1,
        V1_12_R1,
        V1_13_R1, V1_13_R2,
        V1_14_R1,
        V1_15_R1,;
    }
}
