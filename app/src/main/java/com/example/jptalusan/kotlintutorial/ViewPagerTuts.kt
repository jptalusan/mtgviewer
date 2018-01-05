package com.example.jptalusan.kotlintutorial

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager

//https://github.com/codepath/android_guides/wiki/ViewPager-with-FragmentPagerAdapter
//https://www.raywenderlich.com/169885/android-fragments-tutorial-introduction-2
class ViewPagerTuts : AppCompatActivity() {

    private var adapterViewPager: SmartFragmentStatePagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_tuts)
        var viewPager = findViewById<ViewPager>(R.id.viewpager)
        adapterViewPager = MyPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapterViewPager
    }

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    class MyPagerAdapter(fragmentManager: FragmentManager) : SmartFragmentStatePagerAdapter(fragmentManager) {
        val magicCard1 = MagicCard(
                "id",
                "infocode",
                "exp",
                "testing name",
                "manacost",
                "multiverseid",
                "number",
                "power",
                "toughness",
                "type",
                "artist",
                "flavor",
                "text",
                "rarity",
                "variations"
        )

        // Returns total number of pages
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment? {
            return ImageViewPage.newInstance(magicCard1)
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence {
            return "Page " + position
        }

        //Based on number of cards in current list
        companion object {
            private val NUM_ITEMS = 1
        }

    }
}
