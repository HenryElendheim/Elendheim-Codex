package com.elendheim.codex.codex.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.Entity as DomainEntity
import com.elendheim.codex.codex.model.Weakness

// The Room row for one dossier. It mirrors the domain Entity but keeps its lists as
// JSON columns through the converters. We keep the database type separate from the
// domain type so the data layer stays free to change either side independently.
@Entity(tableName = "entities")
data class EntityRecord(
    @PrimaryKey val id: String,
    val designation: String,
    val name: String,
    val classification: String,
    val threat: Int,
    val summary: String,
    val description: String,
    val abilities: List<Ability>,
    val weaknesses: List<Weakness>,
    val containment: String,
    val notes: String,
    val tags: List<String>,
    val related: List<String>,
    val image: String,
    val createdAt: Long,
    val updatedAt: Long,
    val status: String
)

// Map a database row up to the clean domain model the rest of the app uses.
fun EntityRecord.toDomain(): DomainEntity = DomainEntity(
    id = id,
    designation = designation,
    name = name,
    classification = classification,
    threat = threat,
    summary = summary,
    description = description,
    abilities = abilities,
    weaknesses = weaknesses,
    containment = containment,
    notes = notes,
    tags = tags,
    related = related,
    image = image,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = status
)

// Map a domain model down to a database row for storage.
fun DomainEntity.toRecord(): EntityRecord = EntityRecord(
    id = id,
    designation = designation,
    name = name,
    classification = classification,
    threat = threat,
    summary = summary,
    description = description,
    abilities = abilities,
    weaknesses = weaknesses,
    containment = containment,
    notes = notes,
    tags = tags,
    related = related,
    image = image,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = status
)
