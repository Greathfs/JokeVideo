package com.hfs.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface CacheDao extends BaseDao<Cache>{

    //如果是一对多,这里可以写List<Cache>
    @Query("select *from cache where `key`=:key")
    Cache getCache(String key);
}
