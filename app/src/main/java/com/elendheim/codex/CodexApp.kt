package com.elendheim.codex

import android.app.Application
import com.elendheim.codex.codex.db.CodexDatabase
import com.elendheim.codex.codex.repo.CodexRepository

// The Application holds the single repository for the whole app. A tiny manual
// container like this keeps things simple, no dependency injection framework needed
// for an app this size.
class CodexApp : Application() {

    lateinit var repository: CodexRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = CodexDatabase.get(this)
        repository = CodexRepository(db.codexDao())
    }
}
