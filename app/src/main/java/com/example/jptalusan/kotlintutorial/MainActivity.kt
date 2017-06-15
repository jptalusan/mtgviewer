package com.example.jptalusan.kotlintutorial

import android.app.PendingIntent.getActivity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.IOException
import org.jetbrains.anko.db.*
import android.view.View
import com.example.jptalusan.kotlintutorial.R.id.magicCardsRecyclerView
//import com.example.jptalusan.kotlintutorial.R.id.random
import com.squareup.picasso.Picasso
import android.support.v7.widget.DividerItemDecoration
import org.jetbrains.anko.toolbar


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
            val expansion = getRandomExpansionSet()
            supportActionBar!!.title = expansion
            magicCardsRecyclerView.layoutManager = LinearLayoutManager(this)
            magicCardsRecyclerView.hasFixedSize()
            magicCardsRecyclerView.adapter = MagicCardAdapter(getSet(expansion))
            magicCardsRecyclerView.addItemDecoration(DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL))

//            Picasso.with(this)
//                    .load(getARandomRow().imageUrl)
//                    .placeholder(R.drawable.testing)
//                    .into(imageView)
        }

//        random.setOnClickListener(View.OnClickListener {
//            val result = getARandomRow()
//            Picasso.with(this)
//                    .load(result.imageUrl)
//                    .placeholder(R.drawable.testing)
//                    .into(imageView);
//        })
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

            Log.d(TAG, input.getJSONObject(expansionCode).getString("name"))

            var infoCode: String? = ""
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

    private fun getSet(setName: String) =
            database.use {
                select(allSets).whereSimple("expansion=?", setName).exec {
                    parseList(rowParser)
                }
            }

    private fun getRandomExpansionSet() =
            database.use {
                select(allSets, "expansion").distinct().orderBy("RANDOM()").limit(1).exec {
                    parseSingle(StringParser)
                }
    }
    val rowParser = classParser<MagicCard>()
    //TODO:
    //Contains string: SELECT * FROM 'AllSets' where manaCost like '%U%' LIMIT 0,30
}
