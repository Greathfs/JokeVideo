package com.hfs.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

/**
 * 数据库操作基础类
 */
@Dao
public interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(T item);//插入单条数据

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItems(List<T> items);//插入list数据

    @Delete
    void deleteItem(T item);//删除item

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateItem(T item);//更新item
}
