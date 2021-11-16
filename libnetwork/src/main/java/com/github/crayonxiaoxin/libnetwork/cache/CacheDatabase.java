package com.github.crayonxiaoxin.libnetwork.cache;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.crayonxiaoxin.libcommon.AppGlobals;

@Database(entities = {Cache.class}, version = 1)
public abstract class CacheDatabase extends RoomDatabase {
    private static CacheDatabase database;

    static {
        // 内存数据库，进程被杀，数据会丢失
        // Room.inMemoryDatabaseBuilder()
        database = Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase.class, "ppjoke_cache")
                // 是否允许在主线程查询，默认不允许
                //.allowMainThreadQueries()
                // room 的日志模式
                //.setJournalMode()
                // 数据库升级异常后的回滚
                //.fallbackToDestructiveMigration()
                // 数据库升级异常后指定版本的回滚
                //.fallbackToDestructiveMigrationFrom()
                // 数据库升级
                //.addMigrations()
                .build();
    }

    public abstract CacheDao getCacheDao();

    public static CacheDatabase get() {
        return database;
    }
}
