package studio.trc.bukkit.litesignin.reward.util;

import lombok.Getter;

public class SignInGroup
{
    @Getter
    private final String groupName;
    
    public SignInGroup(String groupName) {
        this.groupName = groupName;
    }
}
