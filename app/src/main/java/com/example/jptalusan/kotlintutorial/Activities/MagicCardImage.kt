package com.example.jptalusan.kotlintutorial.Activities

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.example.jptalusan.kotlintutorial.MTGClasses.CardsWithExpansion
import com.example.jptalusan.kotlintutorial.MagicCardDetailPagerAdapter
import com.example.jptalusan.kotlintutorial.R


//https://stackoverflow.com/questions/11879315/pageradapter-start-position
//Check this! below
//https://www.raywenderlich.com/169774/viewpager-tutorial-android-getting-started-kotlin
//TODO: Study intents https://medium.com/@workingkills/you-wont-believe-this-one-weird-trick-to-handle-android-intent-extras-with-kotlin-845ecf09e0e9
class MagicCardImage : AppCompatActivity() {

    var magicCardList: List<CardsWithExpansion>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_tuts)

        magicCardList = intent.getParcelableArrayListExtra("magicCardList")
        val selectedPos = intent.getIntExtra("position", 0)
        println("passed card: ${magicCardList!![selectedPos]}")

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        val adapter = MagicCardDetailPagerAdapter(supportFragmentManager, magicCardList!!)
        viewPager.adapter = adapter

        viewPager.currentItem = selectedPos
        viewPager.offscreenPageLimit = 2

    }
}