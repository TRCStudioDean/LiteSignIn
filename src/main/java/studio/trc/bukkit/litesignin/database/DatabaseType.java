package studio.trc.bukkit.litesignin.database;

import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;

public enum DatabaseType 
{
    SQLITE, 
    
    MYSQL;
    
    public static String getTableSyntax(DatabaseType type) {
        switch (type) {
            case MYSQL: {
                return MySQLEngine.getInstance().getTableSyntax(DatabaseTable.PLAYER_DATA);
            }
            case SQLITE: {
                return SQLiteEngine.getInstance().getTableSyntax(DatabaseTable.PLAYER_DATA);
            }
        }
        return null;
    }
}
