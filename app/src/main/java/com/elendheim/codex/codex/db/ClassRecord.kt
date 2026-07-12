package com.elendheim.codex.codex.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elendheim.codex.codex.model.EntityClass

// The Room row for one classification tier. Same simple mirror pattern as the
// dossier record, with mapping helpers to and from the domain model.
@Entity(tableName = "classes")
data class ClassRecord(
    @PrimaryKey val key: String,
    val label: String,
    val colorHex: String,
    val meaning: String,
    // Kept so the user's tier order in settings survives restarts.
    val sortIndex: Int
)

fun ClassRecord.toDomain(): EntityClass = EntityClass(
    key = key,
    label = label,
    colorHex = colorHex,
    meaning = meaning
)

fun EntityClass.toRecord(sortIndex: Int): ClassRecord = ClassRecord(
    key = key,
    label = label,
    colorHex = colorHex,
    meaning = meaning,
    sortIndex = sortIndex
)
