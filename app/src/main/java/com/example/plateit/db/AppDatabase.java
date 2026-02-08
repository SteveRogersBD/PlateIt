package com.example.plateit.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { PantryItem.class }, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PantryDao pantryDao();
}
