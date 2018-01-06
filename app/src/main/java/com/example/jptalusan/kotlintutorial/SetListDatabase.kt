package com.example.jptalusan.kotlintutorial

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.SyncStateContract.Helpers.update
import org.jetbrains.anko.db.*

class SetListDatabase(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MTGSetListDatabase", null, 1) {
    companion object {
        private var instance: SetListDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): SetListDatabase {
            if (instance == null) {
                instance = SetListDatabase(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(SetList, ifNotExists = true,
                columns = *arrayOf(
                        "name" to TEXT,
                        "code" to TEXT,
                        "releaseDate" to TEXT,
                        "block" to TEXT,
                        "downloaded" to TEXT)
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable(SetList, true)
    }


}

// Access property for Context
val Context.setListDatabase: SetListDatabase
    get() = SetListDatabase(applicationContext)

val SetList: String
    get() = "SetList"

