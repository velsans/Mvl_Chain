package com.mvlchain.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedMapLocationEntity::class,
        SlotNicknameEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MvlLocationDatabase : RoomDatabase() {
    abstract fun cachedMapLocationDao(): CachedMapLocationDao
    abstract fun slotNicknameDao(): SlotNicknameDao
}
