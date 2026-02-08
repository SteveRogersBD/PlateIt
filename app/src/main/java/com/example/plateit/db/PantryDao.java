package com.example.plateit.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY dateAdded DESC")
    List<PantryItem> getAll();

    @Insert
    void insertAll(PantryItem... items);

    @Insert
    void insert(PantryItem item);

    @Delete
    void delete(PantryItem item);

    @Query("DELETE FROM pantry_items")
    void deleteAll();
}
