package com.mvlchain.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slot_nicknames")
data class SlotNicknameEntity(
    @PrimaryKey @ColumnInfo(name = "slot") val slot: String,
    @ColumnInfo(name = "nickname") val nickname: String,
)
