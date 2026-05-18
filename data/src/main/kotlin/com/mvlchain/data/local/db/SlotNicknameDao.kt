package com.mvlchain.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SlotNicknameDao {

    @Query("SELECT nickname FROM slot_nicknames WHERE slot = :slot LIMIT 1")
    fun observeNicknameRows(slot: String): Flow<List<String>>

    @Query("SELECT nickname FROM slot_nicknames WHERE slot = :slot LIMIT 1")
    suspend fun getNicknameRaw(slot: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SlotNicknameEntity)

    @Query("DELETE FROM slot_nicknames WHERE slot = :slot")
    suspend fun deleteSlot(slot: String)
}
