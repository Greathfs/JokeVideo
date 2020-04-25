package com.hfs.libnetwork.cache;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "cache")
public class Cache implements Serializable {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String key;

    @ColumnInfo(name = "data")
    public byte[] data;
}
