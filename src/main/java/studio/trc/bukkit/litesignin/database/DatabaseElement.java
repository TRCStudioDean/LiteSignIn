package studio.trc.bukkit.litesignin.database;

import lombok.Getter;

public class DatabaseElement
{
    @Getter
    private final String field;
    @Getter
    private final String type;
    @Getter
    private final boolean isNull;
    @Getter
    private final boolean primaryKey;
    
    public DatabaseElement(String field, String type, boolean isNull, boolean primaryKey) {
        this.field = field;
        this.type = type;
        this.isNull = isNull;
        this.primaryKey = primaryKey;
    }
}
