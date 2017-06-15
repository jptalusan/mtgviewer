package com.example.jptalusan.kotlintutorial

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.IOException
import com.bumptech.glide.Glide
import org.jetbrains.anko.db.*
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.content.ClipData.Item
import android.database.sqlite.SQLiteQueryBuilder
import java.util.*

//TODO: Add viewpagers for variations and text info first then swipe to image1, image2 etc...
class MainActivity : AppCompatActivity() {
    var url: String? = null
    val TAG = "MTGViewer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "TEST")
        //TODO: check if database is present so as not to parse again
        if (!doesDatabaseExist(this, database.databaseName)) {
            parseJSONFile("allsets")
        } else {
            Glide.with(this).load(getARandomRow("AllSets").imageUrl).into(imageView);
        }

        random.setOnClickListener(View.OnClickListener {
            val tables = getTableNames()
            val rng = Random()
            val index = rng.nextInt(tables.size)
            val randomTableName = tables.get(index)

            val result = getARandomRareRow(randomTableName)
            if (result.count() > 0)
                Glide.with(this).load(result[0].imageUrl).into(imageView);

//            val result = getARandomRow(randomTableName)
//            Glide.with(this).load(result.imageUrl).into(imageView);
        })
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
        Log.d(TAG, input.length().toString())
        for (i in 0..(input.names().length() - 1)) {
            val expansionCode = input.names().getString(i)
            var tempExpansionCode = expansionCode
            //Problem with table names beginning with a number
            if (Character.isDigit(expansionCode[0])) {
                val temp = convertNumberToName(tempExpansionCode[0])
                tempExpansionCode = temp + tempExpansionCode.substring(1)
            }

            if (tempExpansionCode.equals("ALL")) {
                tempExpansionCode = "ALLI"
            }

            Log.d(TAG, input.getJSONObject(expansionCode).getString("name"))

            var infoCode: String? = ""
            if (input.getJSONObject(expansionCode).has("magicCardsInfoCode")) {
                infoCode = input.getJSONObject(expansionCode).getString("magicCardsInfoCode")
            } else {
                continue
            }

            if (tableExists(database.readableDatabase, tempExpansionCode)) {
                Log.d(TAG, "Table: $tempExpansionCode already exists...")
                continue
            } else {
                Log.d(TAG, "Creating new table: $tempExpansionCode")
            }

            //Create table here
            database.use {
                createTable(tempExpansionCode, true,
                        "id" to INTEGER,
                        "name" to TEXT,
                        "manaCost" to TEXT,
                        "imageUrl" to TEXT,
                        "power" to TEXT,
                        "toughness" to TEXT,
                        "type" to TEXT,
                        "artist" to TEXT,
                        "flavor" to BLOB,
                        "text" to BLOB,
                        "rarity" to TEXT,
                        "variations" to TEXT)
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

//                Log.d(TAG, cardName)

                if (cards.getJSONObject(j).has("mciNumber")) {
                    mciNumber = cards.getJSONObject(j).getString("mciNumber")
                    mciNumber = mciNumber.split("/")[mciNumber.split("/").size - 1]
                } else {
                    if (cards.getJSONObject(j).has("multiverseid")) {
                        multiverseid = cards.getJSONObject(j).getString("multiverseid")
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
                    insert(tempExpansionCode,
                            "id" to mciNumber,
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
        //Another method for reading the JSON
//        val LEA = input.getJSONObject("LEA")
//        Log.d("TAG", LEA.toString())

//        val keys = input.keys()
//        while (keys.hasNext()) {
//            val key = keys.next()
//            Log.d("TAG", key.toString())
//            val tag = input.get(key)
//            if (tag is JSONObject) {
//                val name = tag.getString("name")
//                Log.d("TAG", name)
////                Log.d("TAG", input.get(key).toString())
//
//            }
//        }
    }

    //TODO: check if other sets have number at the start
    fun convertNumberToName(digit: Char) =
            when (digit) {
                '1' -> "First"
                '2' -> "Second"
                '3' -> "Third"
                '4' -> "Fourth"
                '5' -> "Fifth"
                '6' -> "Sixth"
                '7' -> "Seventh"
                '8' -> "Eight"
                '9' -> "Nine"
                else -> "Tenth"
            }

    fun tableExists(db: SQLiteDatabase?, tableName: String?): Boolean {
        if (tableName == null || db == null || !db.isOpen) {
            return false
        }
        val cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", arrayOf("table", tableName))
        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    private fun doesDatabaseExist(context: Context, dbName: String): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        return dbFile.exists()
    }

    private fun querySetToArrayList(setName: String) =
        database.use {
            select(setName).exec {
                val rowParser = classParser<MagicCard>()
//                val rowParser = rowParser {
//                    id: Int,
//                    name: String,
//                    manaCost: String,
//                    imageUrl: String,
//                    power: String,
//                    toughness: String,
//                    type: String,
//                    artist: String,
//                    flavor: String,
//                    text: String ->
//                    MagicCard(id, name, manaCost, imageUrl, power, toughness, type, artist, flavor, text)
//                }
                parseList(rowParser)
            }
        }

    private fun getARandomRow() =
            database.use {
                select("AllSets").orderBy("RANDOM()").limit(1).exec {
                    val rowParser = classParser<MagicCard>()
                    parseSingle(rowParser)
                }
            }

    private fun getTableNames() =
            database.use {
                select("sqlite_master", "name").whereSimple("type=?", "table").exec {
                    parseList(StringParser)
                }
            }

    private fun getARandomRareRow(setName: String) =
            database.use {
                select(setName).whereSimple("rarity=? or rarity=?", "Rare", "Mythic Rare").orderBy("RANDOM()").limit(1).exec {
                    val rowParser = classParser<MagicCard>()
                    parseList(rowParser)
                }
            }
}
