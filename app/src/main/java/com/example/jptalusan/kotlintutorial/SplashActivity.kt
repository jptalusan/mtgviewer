package com.example.jptalusan.kotlintutorial

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.jetbrains.anko.db.*
import org.jetbrains.anko.intentFor
import org.json.JSONObject
import org.json.JSONStringer
import java.io.IOException

class SplashActivity : AppCompatActivity() {
    val TAG = "Splash"
    var url: String? = null
    var prefs: SharedPreferences? = null
    val PREFS_FILENAME = "com.teamtreehouse.colorsarefun.prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //TODO: check if database is present so as not to parse again
        if (!doesDatabaseExist(this, database.databaseName)) {
            parseJSONFile("allsets")
        }

        startActivity(intentFor<MainActivity>())
        finish()
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

                if (mciNumber != "0")
                    url = "http://magiccards.info/scans/en/$infoCode/$mciNumber.jpg"
                else
                    url = "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=$multiverseid&type=card"


                database.use {
                    insert(allSets,
                            "id" to mciNumber,
                            "expansion" to expansionCode,
                            "name" to cardName,
                            "manaCost" to manaCost,
                            "imageUrl" to url,
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
