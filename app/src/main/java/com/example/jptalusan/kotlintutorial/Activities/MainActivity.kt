package com.example.jptalusan.kotlintutorial.Activities

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.db.*
import android.support.v7.widget.DividerItemDecoration
import android.widget.ArrayAdapter
import android.widget.TextView
import android.support.v4.view.GravityCompat
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaButtonReceiver.handleIntent
import android.view.*
import android.support.v7.widget.SearchView
import android.widget.ShareActionProvider
import com.example.jptalusan.kotlintutorial.*
import com.example.jptalusan.kotlintutorial.Databases.SetList
import com.example.jptalusan.kotlintutorial.Databases.allSets
import com.example.jptalusan.kotlintutorial.Databases.database
import com.example.jptalusan.kotlintutorial.Databases.setListDatabase
import com.example.jptalusan.kotlintutorial.MTGClasses.CardsWithExpansion
import com.example.jptalusan.kotlintutorial.MTGClasses.Set
import com.example.jptalusan.kotlintutorial.MTGClasses.SetDetails
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

//TODO: Add viewpagers for variations and text info first then swipe to image1, image2 etc...
class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        var color = when(v?.id) {
            R.id.red -> "Red"
            R.id.white -> "White"
            R.id.blue -> "Blue"
            R.id.black -> "Black"
            R.id.green -> "Green"
            else -> { "Colorless" }
        }
        if (expansion != null) {
            updateAdapters(filterByColor(expansion!!, color))
        }
    }

    val TAG = "MTGViewer"
    val PREFS_FILENAME = "com.teamtreehouse.colorsarefun.prefs"
    var mDrawerToggle: ActionBarDrawerToggle? = null
    var expansion: String? = null
    var noDuplicates = false
    var raresOnly = false
    var artistSearch = false
    var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        println("MainActivity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(my_toolbar)

        red.setOnClickListener(this)
        white.setOnClickListener(this)
        blue .setOnClickListener(this)
        black.setOnClickListener(this)
        green.setOnClickListener(this)
        colorless.setOnClickListener(this)

        startWithLEA()

        mDrawerToggle = object : ActionBarDrawerToggle(this, drawer_layout, R.string.app_name, R.string.app_name) {
            override fun onDrawerClosed(drawerView: View) {
                drawer_layout.setDrawerTitle(Gravity.START, "Expansion Sets")
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                drawer_layout.setDrawerTitle(Gravity.START, "Expansion Sets")
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }
        }
//
        (mDrawerToggle as ActionBarDrawerToggle).toolbarNavigationClickListener = View.OnClickListener {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START)
            } else {
                drawer_layout.openDrawer(GravityCompat.START)
            }
        }
        drawer_layout.addDrawerListener(mDrawerToggle as ActionBarDrawerToggle)
        (mDrawerToggle as ActionBarDrawerToggle).syncState()

//        //Populating the drawer
        val setList = getSetList()
        val setNameList: MutableList<String> = mutableListOf("")
        setList.forEach { setNameList.add(it.name) }
        left_drawer.adapter = ArrayAdapter<String>(this, R.layout.drawer_list_item, setNameList)
//
        drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, 0)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        magicCardsRecyclerView.addItemDecoration(DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL))

        //Add listener to when i press back from the fragment and get current position
//        magicCardsRecyclerView.scrollToPosition()

        left_drawer.setOnItemClickListener({
            _, view, i, _ ->
            if (view is TextView) {
                updateAdapters(arrayListOf())
                progress.visibility = View.VISIBLE
                supportActionBar!!.title = view.text.toString()
                magicCardsRecyclerView.layoutManager = LinearLayoutManager(this)
                magicCardsRecyclerView.hasFixedSize()
//                expansion = setListCode[i]
                val code = setList[i - 1].code
                expansion = code
                println(code)
                if (!checkIfAlreadyDownloaded(code)) {
                    saveSetDetailsToDB(code)
                } else {
//                    magicCardsRecyclerView.adapter = MagicCardAdapter(getSet(code))
                    updateAdapters(getSet(code))
                }
            }
            drawer_layout.closeDrawer(left_drawer)
        })
    }

    private fun startWithLEA() {
        updateAdapters(arrayListOf())
        progress.visibility = View.VISIBLE
        supportActionBar!!.title = "Limited Edition Alpha"
        magicCardsRecyclerView.layoutManager = LinearLayoutManager(this)
        magicCardsRecyclerView.hasFixedSize()
        val code = "LEA"
        expansion = code
        println(code)
        if (!checkIfAlreadyDownloaded(code)) {
            saveSetDetailsToDB(code)
        } else {
            updateAdapters(getSet(code))
        }
    }

    private fun saveSetDetailsToDB(code: String) {
        val mapper = jacksonObjectMapper()

        val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        doAsync {
            //Do some Network Request
            val result = URL("https://mtgjson.com/json/$code.json").readText()
            val stringBuilder = StringBuilder(result)

            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            mapper.configure( DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true )
            val setDetails = mapper.readValue<SetDetails>(stringBuilder.toString())
            println(setDetails.name)

            val cardList = setDetails.cards
            cardList.sortedBy { it.mciNumber }.forEach {
                //                println(it.name)
                database.use {
                    insert(allSets,
                            "setCode" to setDetails.code,
                            "magicCardsInfoCode" to setDetails.magicCardsInfoCode,
                            "artist" to it.artist,
                            "flavor" to it.flavor,
                            "manaCost" to it.manaCost,
                            "multiverseid" to it.multiverseid,
                            "name" to it.name,
                            "number" to it.number,
                            "power" to it.power,
                            "toughness" to it.toughness,
                            "rarity" to it.rarity,
                            "text" to it.text,
                            "mciNumber" to it.mciNumber,
                            "types" to it.types.toString(),
                            "variations" to it.variations.toString(),
                            "colors" to it.colors.toString()
                    )
                }
            }
            // Call all operation  related to network or other ui blocking operations here.
            uiThread {
                // perform all ui related operation here

                val set = getSet(code)
                updateDownloadedFlag(code)
                updateAdapters(set)
//                println(getSet(code)[0].name)
            }
        }
//        Thread({
//
//        }).start()
    }

    private fun updateDownloadedFlag(code: String) {
        setListDatabase.use {
            update(SetList, "downloaded" to "True")
                .whereSimple("code=?", code).exec()
        }
    }

    private fun getSetList() =
        setListDatabase.use {
            select(SetList).exec {
                parseList(setListParser)
        }
    }

    private fun checkIfAlreadyDownloaded(code: String): Boolean {
        val downloaded = setListDatabase.use {
            select(SetList, "downloaded").whereSimple("code=?", code).exec {
                parseList(StringParser)
            }
        }
        println(downloaded)
        if (downloaded.isNotEmpty()) {
            println(downloaded)
            return downloaded[0] == "True"
        }
        return false
    }

    private fun getSet(code: String) =
        database.use {
            if (raresOnly) {
                select(allSets).whereArgs("setCode = {exp} and rarity like {rarity}",
                        "exp" to code,
                        "rarity" to "%Rare%").orderBy("mciNumber ASC").exec {
                    parseList(cardListParser)
                }
            } else {

                select(allSets).whereSimple("setCode=?", code).orderBy("mciNumber ASC").exec {
                    parseList(cardListParser)
                }
            }
        }

    //Group by returns only distinct from the particular column
    private fun searchForNameContaining(query: String) =
        database.use {
            if (noDuplicates) {
                select(allSets).whereSimple("name like ? group by name", "%$query%").orderBy("mciNumber ASC").exec {
                    parseList(cardListParser)
                }
            } else {
                select(allSets).whereSimple("name like ?", "%$query%").orderBy("mciNumber ASC").exec {
                    parseList(cardListParser)
                }
            }
        }

    //Group by returns only distinct from the particular column
    private fun filterByColor(code: String, color: String) =
            database.use {
                if (raresOnly) {
                    select(allSets).whereArgs("setCode = {exp} and colors like {color} and rarity like {rarity}",
                            "exp" to code,
                            "color" to "%$color%",
                            "rarity" to "%Rare%").orderBy("mciNumber ASC").exec {
                        parseList(cardListParser)
                    }
                } else {
                    select(allSets).whereArgs("setCode = {exp} and colors like {color}",
                            "exp" to code,
                            "color" to "%$color%").orderBy("mciNumber ASC").exec {
                        parseList(cardListParser)
                    }
                }
            }

    private fun getAllByArtist(artistName: String) =
            database.use {
                if (noDuplicates) {
                    select(allSets).whereSimple("artist like ? group by name", "%$artistName%").orderBy("mciNumber ASC").exec {
                        parseList(cardListParser)
                    }
                } else {
                    select(allSets).whereSimple("artist like ?", "%$artistName%").orderBy("mciNumber ASC").exec {
                        parseList(cardListParser)
                    }
                }
            }

    private fun getRareOnly(code: String, rarity: String) =
            database.use {
                select(allSets).whereArgs("setCode = {exp} and (rarity = {rarity} or rarity = {mRare})",
                        "exp" to code!!,
                        "rarity" to rarity,
                        "mRare" to "Mythic Rare").orderBy("mciNumber ASC").exec {
                    parseList(cardListParser)
                }
    }

    val setListParser = classParser<Set>()
    val cardListParser = classParser<CardsWithExpansion>()

    override fun onCreateOptionsMenu(menu: Menu) : Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        menu.findItem(R.id.action_rare).isChecked = prefs.getBoolean("RareOnly", false)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView = menu.findItem(R.id.search).actionView as? SearchView
//        if (searchView != null)
            searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if ((mDrawerToggle as ActionBarDrawerToggle).onOptionsItemSelected(item)) {
            return true
        }

        // Handle action buttons
        when (item.itemId) {
            R.id.action_rare -> {
                if (item.isChecked) {
                    println("Checked")
                    item.isChecked = false
                    updateAdapters(getSet(expansion!!))
                } else {
                    println("unchecked")
                    item.isChecked = true
                    println(expansion)
                    updateAdapters(getRareOnly(expansion!!, "Rare"))
                }

                val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
                raresOnly = item.isChecked
                prefs.edit().putBoolean("RareOnly", raresOnly).apply()

                return true
            }
            R.id.action_remove_duplicates -> {
                if (item.isChecked) {
                    println("Checked")
                    item.isChecked = false
                } else {
                    println("unchecked")
                    item.isChecked = true
                    println(expansion)
                }
                noDuplicates = item.isChecked
                if (artistSearch) {
                    val output = query?.let { getAllByArtist(it) }
                    output?.let { updateAdapters(it) }
                } else {
                    val output = query?.let { searchForNameContaining(it) }
                    output?.let { updateAdapters(it) }
                }
                return true
            }
            R.id.action_artists -> {
                if (item.isChecked) {
                    println("Checked")
                    item.isChecked = false
                } else {
                    println("unchecked")
                    item.isChecked = true
                }
                artistSearch = item.isChecked
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        (mDrawerToggle as ActionBarDrawerToggle).syncState()
    }

    override fun onNewIntent(intent: Intent) {
        println("onNewIntent")
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        println("handleIntent")
        if (intent.action == Intent.ACTION_SEARCH) {
            query = intent.getStringExtra(SearchManager.QUERY)
            if (!artistSearch) {
                val output = query?.let { searchForNameContaining(it) }
//            output.forEach { println(it.name) }
                output?.let { updateAdapters(it) }
            } else {
                val output = query?.let { getAllByArtist(it) }
                output?.let { updateAdapters(it) }
            }
        }
    }

    private fun updateAdapters(cardList: List<CardsWithExpansion>) {
        magicCardsRecyclerView.adapter = null
        magicCardsRecyclerView.adapter = MagicCardAdapter(cardList)
        magicCardsRecyclerView.adapter.notifyDataSetChanged()
        progress.visibility = View.GONE
    }
}