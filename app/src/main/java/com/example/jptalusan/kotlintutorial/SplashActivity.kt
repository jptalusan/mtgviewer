package com.example.jptalusan.kotlintutorial

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.jetbrains.anko.db.*
import org.json.JSONObject
import org.json.JSONStringer
import java.io.IOException
import android.net.NetworkInfo
import android.net.ConnectivityManager
import android.provider.SyncStateContract.Helpers.insert
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.widget.Toast
import com.fasterxml.jackson.annotation.JsonView
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.coroutines.experimental.selects.select
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//Preload the list of sets (download if not existing, update if old version or continue)
class SplashActivity : AppCompatActivity() {
    val TAG = "Splash"
    var url: String? = null
    var prefs: SharedPreferences? = null
    val PREFS_FILENAME = "com.teamtreehouse.colorsarefun.prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        if (isNetworkConnected(this) && !doesDatabaseExist(this, setListDatabase.databaseName)) {
            println("Downloading fresh copy")
            downloadSetList()

        } else if (!isNetworkConnected(this) && !doesDatabaseExist(this, setListDatabase.databaseName)) {
            toast("Connect to the internet first.")
        } else if (!isNetworkConnected(this) && doesDatabaseExist(this, setListDatabase.databaseName)) {
            println("Continue with current database")
        } else if (isNetworkConnected(this) && doesDatabaseExist(this, setListDatabase.databaseName)) {
            val mapper = jacksonObjectMapper()
            Thread({
                val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
                val currVer = prefs!!.getString("version", "")

                //Do some Network Request
                //Check if the version has changed if yes, download new one
                val version = URL("https://mtgjson.com/json/version-full.json").readText()
                val ver = mapper.readValue<ObjectNode>(version)

                val newVer = ver.get("version").toString()
                println(newVer)
                prefs.edit().putString("version", newVer).apply()

                if (versionCompare(newVer, currVer) > 0) {
                    //Download again
                    setListDatabase.use {
                        dropTable(SetList, true)
                    }
                    downloadSetList()
                } else {
                    println("Still up to date")

                    startActivity(intentFor<MainActivity>())
                    finish()
                }
            }).start()
        } else {
            //do nothing
        }
        //end klaxon test

    }

    fun downloadSetList() {

        val mapper = jacksonObjectMapper()
        Thread({
            val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
            //Do some Network Request
            //Check if the version has changed if yes, download new one
            val version = URL("https://mtgjson.com/json/version-full.json").readText()
            val ver = mapper.readValue<ObjectNode>(version)

            val currVer = prefs!!.getString("version", "")
            val newVer = ver.get("version").toString()
            println(newVer)
            prefs.edit().putString("version", newVer).apply()

            val result = URL("https://mtgjson.com/json/SetList.json").readText()
            val stringBuilder = StringBuilder(result)

            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            val sets = mapper.readValue<List<Set>>(stringBuilder.toString())

            println("Conversion finished!")
            sets.sortedWith(CompareSetReleaseDates).forEach {
                println(it.name)
                setListDatabase.use {
                    insert(SetList,
                            "name" to it.name,
                            "code" to it.code,
                            "releaseDate" to it.releaseDate,
                            "block" to it.block,
                            "downloaded" to "False")
                }
            }
            startActivity(intentFor<MainActivity>())
            finish()
        }).start()
    }

    private fun doesDatabaseExist(context: Context, dbName: String): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        return dbFile.exists()
    }
}
