package com.flash.devdigest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flash.devdigest.data.local.dao.NewsDao
import com.flash.devdigest.data.local.entity.NewsEntity

@Database(
    entities = [NewsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}