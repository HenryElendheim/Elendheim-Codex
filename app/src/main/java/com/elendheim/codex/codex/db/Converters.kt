package com.elendheim.codex.codex.db

import androidx.room.TypeConverter
import com.elendheim.codex.codex.io.CodexJson
import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.StoryEntry
import com.elendheim.codex.codex.model.Weakness
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

// Room only stores simple column types, so the list fields on a dossier are kept
// as JSON text in a single column. These converters turn the lists into text on
// the way in and back into lists on the way out. JSON is our data format anyway,
// so this stays consistent with the export file.
class Converters {

    @TypeConverter
    fun abilitiesToJson(value: List<Ability>): String =
        CodexJson.format.encodeToString(ListSerializer(Ability.serializer()), value)

    @TypeConverter
    fun jsonToAbilities(value: String): List<Ability> =
        if (value.isBlank()) emptyList()
        else CodexJson.format.decodeFromString(ListSerializer(Ability.serializer()), value)

    @TypeConverter
    fun weaknessesToJson(value: List<Weakness>): String =
        CodexJson.format.encodeToString(ListSerializer(Weakness.serializer()), value)

    @TypeConverter
    fun jsonToWeaknesses(value: String): List<Weakness> =
        if (value.isBlank()) emptyList()
        else CodexJson.format.decodeFromString(ListSerializer(Weakness.serializer()), value)

    @TypeConverter
    fun storyToJson(value: List<StoryEntry>): String =
        CodexJson.format.encodeToString(ListSerializer(StoryEntry.serializer()), value)

    @TypeConverter
    fun jsonToStory(value: String): List<StoryEntry> =
        if (value.isBlank()) emptyList()
        else CodexJson.format.decodeFromString(ListSerializer(StoryEntry.serializer()), value)

    @TypeConverter
    fun stringsToJson(value: List<String>): String =
        CodexJson.format.encodeToString(ListSerializer(String.serializer()), value)

    @TypeConverter
    fun jsonToStrings(value: String): List<String> =
        if (value.isBlank()) emptyList()
        else CodexJson.format.decodeFromString(ListSerializer(String.serializer()), value)
}
