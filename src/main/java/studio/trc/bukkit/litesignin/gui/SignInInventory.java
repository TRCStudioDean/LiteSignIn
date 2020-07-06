package studio.trc.bukkit.litesignin.gui;

import java.util.Date;
import java.util.List;

import org.bukkit.inventory.Inventory;

import studio.trc.bukkit.litesignin.util.SignInDate;

/**
 * The Sign-in gui.
 * @author Dean
 */
public class SignInInventory
{
    private final int month;
    private final int year;
    private final Inventory inv;
    private final List<SignInGUIColumn> buttons;

    public SignInInventory(Inventory inv, List<SignInGUIColumn> buttons) {
        this.inv = inv;
        this.buttons = buttons;
        SignInDate today = SignInDate.getInstance(new Date());
        month = today.getMonth();
        year = today.getYear();
    }

    public SignInInventory(Inventory inv, List<SignInGUIColumn> buttons, int month) {
        this.inv = inv;
        this.buttons = buttons;
        this.month = month;
        year = SignInDate.getInstance(new Date()).getYear();
    }

    public SignInInventory(Inventory inv, List<SignInGUIColumn> buttons, int month, int year) {
        this.inv = inv;
        this.buttons = buttons;
        this.month = month;
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
    
    public int getNextPageMonth() {
        if (month == 12) {
            return 1;
        } else {
            return month + 1;
        }
    }
    
    public int getNextPageYear() {
        if (month != 12) {
            return year;
        }
        return year + 1;
    }
    
    public int getPreviousPageMonth() {
        if (month == 1) {
            return 12;
        } else {
            return month - 1;
        }
    }
    
    public int getPreviousPageYear() {
        if (month != 1) {
            return year;
        }
        return year - 1;
    }

    public Inventory getInventory() {
        return inv;
    }

    public List<SignInGUIColumn> getButtons() {
        return buttons;
    }
}
