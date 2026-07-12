# Keep kotlinx.serialization generated serializers so JSON export and import
# keep working in the shrunk release build.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the serializer for every class marked @Serializable.
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room generates code against these model classes, keep their names.
-keep class com.elendheim.codex.codex.model.** { *; }
