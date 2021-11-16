package com.github.crayonxiaoxin.libnetwork.cache;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CacheDao {
    @Insert(onConflict = REPLACE)
    long save(Cache cache); // 只有一个参数是可以返回 long

    @Query("select * from cache where `key` = :key")
    Cache getCache(String key);

    @Delete
    int delete(Cache cache); // 返回 int 删除的行数或 void

    @Update(onConflict = REPLACE)
    int update(Cache cache); // 返回 int 更新的行数或 void
}
