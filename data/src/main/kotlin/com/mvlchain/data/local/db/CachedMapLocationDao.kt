package com.mvlchain.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedMapLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedMapLocationEntity)

    @Query("SELECT * FROM cached_map_locations WHERE cache_key = :key LIMIT 1")
    suspend fun getByKey(key: String): CachedMapLocationEntity?

    @Query("UPDATE cached_map_locations SET cached_nickname = NULL WHERE cached_nickname IS NOT NULL")
    suspend fun clearCachedNicknames()
}
