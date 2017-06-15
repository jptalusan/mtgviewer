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
                instance = MyDatabaseOpenHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db?.createTable("AllSets", ifNotExists = true,
                columns = *arrayOf("id" to INTEGER,
                "name" to TEXT,
                "manaCost" to TEXT,
                "imageUrl" to TEXT,
                "power" to TEXT,
                "toughness" to TEXT,
                "type" to TEXT,
                "artist" to TEXT,
                "flavor" to BLOB,
                "text" to BLOB)
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db?.dropTable("LEA", true)
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(getApplicationContext())