package com.elendheim.codex.codex.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// A tiny key and value table for small app wide settings, for example the
// designation prefix. Keeping it in the database means one place holds all state.
@Entity(tableName = "settings")
data class SettingRecord(
    @PrimaryKey val key: String,
    val value: String
)
