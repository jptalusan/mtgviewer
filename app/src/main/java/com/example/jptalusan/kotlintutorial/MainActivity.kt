package com.example.jptalusan.kotlintutorial

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.db.*
import android.support.v7.widget.DividerItemDecoration
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.support.v4.view.GravityCompat
import android.app.SearchManager
import android.support.v4.widget.SearchViewCompat.setSearchableInfo
import android.view.*
import android.support.v7.widget.SearchView
import android.widget.ShareActionProvider
import org.jetbrains.anko.searchManager
import android.content.Context.SEARCH_SERVICE
import android.support.v4.view.MenuItemCompat
import kotlinx.coroutines.experimental.selects.select


//TODO: Add viewpagers for variations and text info first then swipe to image1, image2 etc...
class MainActivity : AppCompatActivity() {
    val TAG = "MTGViewer"
    var prefs: SharedPreferences? = null
    val PREFS_FILENAME = "com.teamtreehouse.colorsarefun.prefs"
    val setListCode: MutableList<String> = mutableListOf("")
    var mDrawerToggle: ActionBarDrawerToggle? = null
    internal var mShareActionProvider: ShareActionProvider? = null
    var expansion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        println("MainActivity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(my_toolbar)
        Log.d(TAG, "TEST")

        mDrawerToggle = object : ActionBarDrawerToggle(this, drawer_layout, R.string.app_name, R.string.app_name) {
            override fun onDrawerClosed(drawerView: View) {
                drawer_layout.setDrawerTitle(Gravity.START, "Expansion Sets")
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                drawer_layout.setDrawerTitle(Gravity.START, "Expansion Sets")
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        }

        (mDrawerToggle as ActionBarDrawerToggle).toolbarNavigationClickListener = View.OnClickListener {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START)
            } else {
                drawer_layout.openDrawer(GravityCompat.START)
            }
        }
        drawer_layout.addDrawerListener(mDrawerToggle as ActionBarDrawerToggle)
//        (mDrawerToggle as ActionBarDrawerToggle).syncState()

        val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)

        val setList: MutableList<String> = mutableListOf("")
        expansion = getRandomExpansionSet()
        for (s in getSetsList()) {
            val setName = prefs.getString(s, "")
            if (expansion == s) {
                supportActionBar!!.title = setName
            }
            Log.d(TAG, setName)
            setList.add(setName)
            setListCode.add(s)
        }

        magicCardsRecyclerView.layoutManager = LinearLayoutManager(this)
        magicCardsRecyclerView.hasFixedSize()
        magicCardsRecyclerView.adapter = MagicCardAdapter(getSet(expansion!!))
        magicCardsRecyclerView.addItemDecoration(DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL))

        left_drawer.adapter = ArrayAdapter<String>(this, R.layout.drawer_list_item, setList)

        drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, 0)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        left_drawer.setOnItemClickListener({
            _: AdapterView<*>, view: View, i: Int, _: Long ->
            if (view is TextView) {
                expansion = view.text.toString()
                Log.d(TAG, expansion)
                supportActionBar!!.title = expansion
                magicCardsRecyclerView.layoutManager = LinearLayoutManager(this)
                magicCardsRecyclerView.hasFixedSize()
                val setCode = setListCode[i]
                magicCardsRecyclerView.adapter = MagicCardAdapter(getSet(setCode))
                magicCardsRecyclerView.addItemDecoration(DividerItemDecoration(this,
                        DividerItemDecoration.VERTICAL))
            }
            drawer_layout.closeDrawer(left_drawer)
        })
    }

    private fun getRandomExpansionSet() =
            database.use {
                select(allSets, "expansion").distinct().orderBy("RANDOM()").limit(1).exec {
                    parseSingle(StringParser)
                }
            }

    private fun getSet(setName: String) =
            database.use {
                select(allSets).whereSimple("expansion=?", setName).orderBy("name ASC").exec {
                    parseList(rowParser)
                }
            }

    private fun getSetsList() =
            database.use {
                select(allSets, "expansion").distinct().exec {
                    parseList(StringParser)
                }
            }
    val rowParser = classParser<MagicCard>()
    //TODO:
    //Contains string: SELECT * FROM 'AllSets' where manaCost like '%U%' LIMIT 0,30

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /* Called whenever we call invalidateOptionsMenu() */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // If the nav drawer is open, hide action items related to the content view
        val drawerOpen = drawer_layout.isDrawerOpen(left_drawer)
        menu.findItem(R.id.action_search).isVisible = !drawerOpen
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if ((mDrawerToggle as ActionBarDrawerToggle).onOptionsItemSelected(item)) {
            return true
        }
        // Handle action buttons
        when (item.itemId) {
            R.id.action_search -> {
                Log.d(TAG, "Search")
                return true
            }
            R.id.action_rare -> {
                if (item.isChecked) {
                    println("Checked")
                    item.isChecked = false
                    magicCardsRecyclerView.adapter = MagicCardAdapter(getSet(expansion!!))
                } else {
                    println("unchecked")
                    item.isChecked = true
                    updateListAdapter(expansion!!, "Rare")
                }
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

    fun updateListAdapter(setName: String?, rarity: String) {
        val newList =
                database.use {
                    select(allSets).whereSimple("expansion=? and rarity=?", setName!!, rarity).orderBy("name ASC").exec {
                        parseList(rowParser)
                    }
                }
        magicCardsRecyclerView.adapter = MagicCardAdapter(newList)

    }
}

class MyRowParser : RowParser<Triple<Int, String, String>> {
    override fun parseRow(columns: Array<Any?>): Triple<Int, String, String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
//    override fun parseRow(columns: Array<Any>): Triple<Int, String, String> {
//        return Triple(columns[0] as Int, columns[1] as String, columns[2] as String)
//    }
}