# Elendheim Codex

A private archive for your own invented entities. Think of it as a classified file
cabinet that lives on your phone: you write an entry for each entity, power or
phenomenon you come up with, fill in structured fields for how it works and how to
beat it, then find it later by class, threat, tag or a plain search.

This app does three things:

- Keeps a structured archive of entities, each with abilities and weaknesses laid
  out as clean, scannable blocks.
- Lets you search, filter and sort the whole archive fast, even at hundreds of
  entries.
- Exports and imports everything as a single file, so your data is always yours and
  moves to any phone without a fuss.

It stays on your device. No accounts, no network permission, no cloud. The export
file is your backup and your escape hatch.

## Design

- Dark first. The look is near black with a soft red accent and monospaced accents,
  which already reads like a classified database.
- Built to be quick and pleasant to use, not a chore.

## Tech

- Kotlin with Jetpack Compose for the UI
- Room (SQLite) as the fast on device cache
- kotlinx.serialization for the JSON export and import format, which is the real
  long term home of your data
- Storage Access Framework for export and import, so files go exactly where you pick
- Minimum Android 8.0 (SDK 26)

The data layer under `codex/` knows nothing about the UI. The `ui/` layer renders it.

## Build

Open the project in Android Studio and run it, or from the command line:

    ./gradlew assembleDebug

## Export and import

Everything lives in one JSON file: a schema version, your class definitions and every
entry, including archived ones. Import matches by id and keeps the newer copy of any
clash, so it never blindly overwrites or duplicates. There is also a full replace for
a clean restore, behind a confirm, and a readable Markdown export for printing or
pasting anywhere.

## License

MIT. See [LICENSE](LICENSE).
