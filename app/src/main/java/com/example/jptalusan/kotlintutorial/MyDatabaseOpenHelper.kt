package com.example.jptalusan.kotlintutorial

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MTGDatabase", null, 1) {
    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(allSets, ifNotExists = true,
                columns = *arrayOf(
                        "setCode" to TEXT,
                        "magicCardsInfoCode" to TEXT,
                        "artist" to TEXT,
                        "flavor" to BLOB,
                        "manaCost" to TEXT,
                        "multiverseid" to TEXT,
                        "name" to TEXT,
                        "number" to TEXT,
                        "power" to TEXT,
                        "toughness" to TEXT,
                        "rarity" to TEXT,
                        "text" to BLOB,
                        "mciNumber" to TEXT,
                        "types" to TEXT,
                        "variations" to TEXT
                )
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable(allSets, true)
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(getApplicationContext())

val allSets: String
        get() = "AllSets"