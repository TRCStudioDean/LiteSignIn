package studio.trc.bukkit.litesignin.gui;

import org.bukkit.inventory.ItemStack;
import studio.trc.bukkit.litesignin.util.SignInDate;

/**
 * Buttons in gui.
 * @author TRCStudioDean
 */
public class SignInGUIColumn
{
    private final int key;
    private final ItemStack is;
    private final boolean isKey;
    
    private SignInDate date;
    private KeyType keyType;
    private String buttonName = null;
    
    /**
     * When this button is the primary key
     * @param is ItemStack's instance
     * @param key Location number in Inventory GUI
     * @param date Now date
     * @param keyType key's type
     */
    public SignInGUIColumn(ItemStack is, int key, SignInDate date, KeyType keyType) {
        this.key = key;
        this.is = is;
        this.date = date;
        this.keyType = keyType;
        isKey = true;
    }
    
    /**
     * When this button is others
     * @param is ItemStack's instance
     * @param key Location number in Inventory GUI
     * @param buttonName Button name
     */
    public SignInGUIColumn(ItemStack is, int key, String buttonName) {
        this.key = key;
        this.is = is;
        this.buttonName = buttonName;
        isKey = false;
    }
  
    public int getKeyPostion() {
        return key;
    }
    
    public boolean isKey() {
        return isKey;
    }
    
    public SignInDate getDate() {
        return date;
    }
    
    public String getButtonName() {
        return buttonName;
    }
  
    public ItemStack getItemStack() {
        return is;
    }
    
    public KeyType getKeyType() {
        return keyType;
    }
    
    public static enum KeyType {
        
        ALREADY_SIGNIN("Already-SignIn"),
        
        COMMING_SOON("Comming-Soon"),
        
        NOTHING_SIGNIN("Nothing-SignIn"),
        
        MISSED_SIGNIN("Missed-SignIn");
        
        private final String sectionName;
        
        private KeyType(String sectionName) {
            this.sectionName = sectionName;
        }
        
        public String getSectionName() {
            return sectionName;
        }
    }
}
