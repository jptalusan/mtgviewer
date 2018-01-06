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

class StringToDateTime : JsonSerializer<String>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: String?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        gen!!.writeObject(df.parse(value!!))
    }
}

//Preload the list of sets (download if not existing, update if old version or continue)
class SplashActivity : AppCompatActivity() {
    val TAG = "Splash"
    var url: String? = null
    var prefs: SharedPreferences? = null
    val PREFS_FILENAME = "com.teamtreehouse.colorsarefun.prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Debug
//        startActivity(intentFor<ViewPagerTuts>())
        //End debug uncomment below


        //TODO: check if database is present so as not to parse again
//        val dialog = progressDialog(message = "Please wait a bit…", title = "Fetching data")
//        dialog.setCancelable(false)
//        dialog.setCanceledOnTouchOutside(false)
//        dialog.show()
//        if (!doesDatabaseExist(this, database.databaseName)) {
//            parseJSONFile("allsets")
//        }
//        dialog.cancel()
//
//        startActivity(intentFor<MainActivity>())
//        finish()

        //klaxon test

        if (isNetworkConnected(this) && !doesDatabaseExist(this, setListDatabase.databaseName)) {
            println("Downloading fresh copy")
            downloadSetList()

        } else if (!isNetworkConnected(this) && !doesDatabaseExist(this, setListDatabase.databaseName)) {
            toast("Connect to the internet first.")
        } else if (!isNetworkConnected(this) && doesDatabaseExist(this, setListDatabase.databaseName)) { //use currently existing database
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

    fun loadJSONFromAsset(fileName: String) : String? {
        Log.d("TAG", "loadJSONFromAsset")
        try {
            val istream = resources.openRawResource(
                    resources.getIdentifier(fileName,
                            "raw", packageName))
            val size = istream.available()
            val buffer = ByteArray(size)
            istream.read(buffer)
            istream.close()
            return String(buffer)
        } catch (e: IOException) {
            print(e)
            return null
        }
    }

    fun parseJSONFile(fileName: String) {
        val input = JSONObject(loadJSONFromAsset(fileName))
//        Log.d(TAG, input.length().toString())
        for (i in 0..(input.names().length() - 1)) {
            val expansionCode = input.names().getString(i)

//            Log.d(TAG, input.getJSONObject(expansionCode).getString("name"))
            prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
            if (prefs!!.getString(expansionCode, "") == "") {
                val editor = prefs!!.edit()
                editor.putString(expansionCode, input.getJSONObject(expansionCode).getString("name"))
                editor.apply()
            }

            var infoCode: String?
            if (input.getJSONObject(expansionCode).has("magicCardsInfoCode")) {
                infoCode = input.getJSONObject(expansionCode).getString("magicCardsInfoCode")
            } else {
                continue
            }

            val cards = input.getJSONObject(expansionCode).getJSONArray("cards")
            for (j in 0..(cards.length() - 1)) {
                var power: String? = "?"
                var toughness: String? = "?"
                var flavor: String? = ""
                var manaCost: String? = "0"
                var mciNumber: String? = "0"
                var cardName: String? = ""
                var cardText: String? = ""
                var variations: String? = ""
                var multiverseid: String? = "0"
                var number: String? = ""

                if (cards.getJSONObject(j).has("name"))
                    cardName = cards.getJSONObject(j).getString("name")

                if (cards.getJSONObject(j).has("mciNumber")) {
                    mciNumber = cards.getJSONObject(j).getString("mciNumber").toString()
                    mciNumber = mciNumber.split("/")[mciNumber.split("/").size - 1]
                } else {
                    if (cards.getJSONObject(j).has("multiverseid")) {
                        multiverseid = cards.getJSONObject(j).getString("multiverseid").toString()
                    }
                }

                if (cards.getJSONObject(j).has("manaCost"))
                    manaCost = cards.getJSONObject(j).getString("manaCost")

                if (cards.getJSONObject(j).has("flavor")) {
                    flavor = cards.getJSONObject(j).getString("flavor")
                }

                if (cards.getJSONObject(j).has("text")) {
                    cardText = cards.getJSONObject(j).getString("text")
                }

                val artist = cards.getJSONObject(j).getString("artist")
                val rarity = cards.getJSONObject(j).getString("rarity")

                val type = cards.getJSONObject(j).getString("type")
                if (type.contains("Creature")) {
                    if (cards.getJSONObject(j).has("power")) {
                        power = cards.getJSONObject(j).getString("power")
                    }

                    if (cards.getJSONObject(j).has("toughness")) {
                        toughness = cards.getJSONObject(j).getString("toughness")
                    }
                }

                if (cards.getJSONObject(j).has("variations")) {
                    variations = cards.getJSONObject(j).getString("variations")
                }

                if (cards.getJSONObject(j).has("number")) {
                    number = cards.getJSONObject(j).getString("number")
                }

                database.use {
                    insert(allSets,
                            "id" to mciNumber,
                            "infoCode" to infoCode,
                            "expansion" to expansionCode,
                            "name" to cardName,
                            "manaCost" to manaCost,
                            "multiverseid" to multiverseid,
                            "number" to number,
                            "power" to power,
                            "toughness" to toughness,
                            "type" to type,
                            "artist" to artist,
                            "flavor" to flavor,
                            "text" to cardText,
                            "rarity" to rarity,
                            "variations" to variations)
                }
            }
        }
    }

    private fun doesDatabaseExist(context: Context, dbName: String): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        return dbFile.exists()
    }

    private fun getARandomRow() =
            database.use {
                select(allSets).orderBy("RANDOM()").limit(1).exec {
                    parseSingle(rowParser)
                }
            }

    private fun getARandomRareRow() =
            database.use {
                select(allSets).whereSimple("rarity=? or rarity=?", "Rare", "Mythic Rare").orderBy("RANDOM()").limit(1).exec {
                    parseList(rowParser)
                }
            }

    val rowParser = classParser<MagicCard>()
}
