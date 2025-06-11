package studio.trc.bukkit.litesignin.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;

public enum ConfigurationType
{
    /**
     * Config.yml
     */
    CONFIG("Config.yml", "", new YamlConfiguration(), false),
    
    /**
     * Messages.yml
     */
    MESSAGES("Messages.yml", "", new YamlConfiguration(), true),
    
    /**
     * GUISettings.yml
     */
    GUI_SETTINGS("GUISettings.yml", "", new YamlConfiguration(), true, ConfigurationVersion.GUI_SETTINGS_V2, ConfigurationVersion.GUI_SETTINGS_V1),
    
    /**
     * RewardSettings.yml
     */
    REWARD_SETTINGS("RewardSettings.yml", "", new YamlConfiguration(), false, ConfigurationVersion.REWARD_SETTINGS_V2, ConfigurationVersion.REWARD_SETTINGS_V1),
    
    /**
     * CustomItems.yml
     */
    CUSTOM_ITEMS("CustomItems.yml", "", new YamlConfiguration(), false),
    
    /**
     * WoodSignSettings.yml
     */
    WOOD_SIGN_SETTINGS("WoodSignSettings.yml", "", new YamlConfiguration(), false);
    
    @Getter
    private final boolean universal;
    @Getter
    private final String fileName;
    @Getter
    private final String folder;
    @Getter
    private final YamlConfiguration config;
    @Getter
    private final ConfigurationVersion[] versions;

    private ConfigurationType(String fileName, String folder, YamlConfiguration config, boolean universal, ConfigurationVersion... versions) {
        this.fileName = fileName;
        this.folder = folder;
        this.universal = universal;
        this.config = config;
        this.versions = versions;
    }
    
    public void saveResource() {
        File dataFolder = new File("plugins/LiteSignIn/" + folder);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try {
            File configFile = new File(dataFolder, fileName);
            if (!configFile.exists()) {
                configFile.createNewFile();
                if (!universal) {
                    InputStream is = Main.class.getResourceAsStream("/Languages/" + (universal ? "Universal" : "") + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/" + getLocalFilePath());
                    byte[] bytes = new byte[is.available()];
                    for (int len = 0; len != bytes.length; len += is.read(bytes, len, bytes.length - len));
                    try (OutputStream out = new FileOutputStream(configFile)) {
                        out.write(bytes);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void saveConfig() {
        try {
            config.save("plugins/LiteSignIn/" + folder + fileName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public boolean reloadConfig() {
        try (InputStreamReader configFile = new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + folder + fileName), LiteSignInProperties.getMessage("Charset"))) {
            config.load(configFile);
            if (universal) {
                saveLanguageConfig(this);
            }
            return true;
        } catch (IOException | InvalidConfigurationException ex) {
            File oldFile = new File("plugins/LiteSignIn/" + folder + fileName + ".old");
            File file = new File("plugins/LiteSignIn/" + folder + fileName);
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{file}", folder + fileName);
            LiteSignInProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);
            saveResource();
            try (InputStreamReader newConfig = new InputStreamReader(new FileInputStream(file), LiteSignInProperties.getMessage("Charset"))) {
                config.load(newConfig);
                LiteSignInProperties.sendOperationMessage("ConfigurationRepair", MessageUtil.getDefaultPlaceholders());
            } catch (IOException | InvalidConfigurationException ex1) {
                ex1.printStackTrace();
            }
        }
        return false;
    }
    
    public String getLocalFilePath() {
        if (versions.length == 0) {
            return folder + fileName;
        } else {
            try {
                String nms = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                ConfigurationVersion specialVersion = Arrays.stream(versions).filter(version -> version.getVersions().length != 0 ? Arrays.stream(version.getVersions()).anyMatch(type -> nms.equalsIgnoreCase(type.name())) : true).findFirst().get();
                return specialVersion.getFolder() + specialVersion.getFileName();
            } catch (Exception ex) {
                ConfigurationVersion specialVersion = Arrays.stream(versions).filter(version -> version.getVersions().length == 0).findFirst().get();
                return specialVersion.getFolder() + specialVersion.getFileName();
            }
        }
    }
    
    public RobustConfiguration getRobustConfig() {
        return ConfigurationUtil.getConfig(this);
    }
    
    public static void saveLanguageConfig(ConfigurationType type) {
        String language = MessageUtil.getLanguage();
        if (!type.getRobustConfig().getConfig().contains(language)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/Languages/Universal/" + type.getLocalFilePath()), LiteSignInProperties.getMessage("Charset")))) {
                String line;
                StringBuilder source = new StringBuilder();
                try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + type.getFolder() + type.getFileName()), LiteSignInProperties.getMessage("Charset")))) {
                    while ((line = input.readLine()) != null) {
                        source.append(line);
                        source.append('\n');
                    }
                }
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plugins/LiteSignIn/" + type.getFolder() + type.getFileName()), LiteSignInProperties.getMessage("Charset")))) {
                    writer.append(source.toString());
                    boolean keepWriting = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(language + ":")) {
                            keepWriting = true;
                        }
                        if (!line.startsWith("    ") && !line.startsWith(language)) {
                            keepWriting = false;
                        }
                        if (keepWriting) {
                            writer.append(line);
                            writer.append('\n');
                        }
                    }
                }
                try (Reader reloader = new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + type.getFolder() + type.getFileName()), LiteSignInProperties.getMessage("Charset"))) {
                    type.config.load(reloader);
                } catch (IOException | InvalidConfigurationException ex) {
                    ex.printStackTrace();
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{file}", type.getFolder() + type.getFileName());
                    LiteSignInProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{file}", type.getFolder() + type.getFileName());
                LiteSignInProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
            }
        }
    }
    
    public static ConfigurationType getType(String filePath) {
        String[] path = filePath.split("/", -1);
        if (path.length > 1) {
            String fileName = path[path.length - 1];
            StringBuilder folder = new StringBuilder();
            for (int i = 0;i < path.length - 1;i++) {
                folder.append(path[i]);
                folder.append("/");
            }
            return Arrays.stream(ConfigurationType.values()).filter(type -> type.getFolder().equalsIgnoreCase(folder.toString()) && type.getFileName().equalsIgnoreCase(fileName)).findFirst().orElse(null);
        } else {
            String fileName = filePath;
            return Arrays.stream(ConfigurationType.values()).filter(type -> type.getFileName().equalsIgnoreCase(fileName)).findFirst().orElse(null);
        }
    }
}
