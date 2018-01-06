package com.example.jptalusan.kotlintutorial

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.jptalusan.kotlintutorial.R.id.name
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.magic_card_activity.*
import org.jetbrains.anko.progressDialog
import android.widget.Toast


//https://stackoverflow.com/questions/11879315/pageradapter-start-position
//Check this! below
//https://www.raywenderlich.com/169774/viewpager-tutorial-android-getting-started-kotlin
//TODO: Study intents https://medium.com/@workingkills/you-wont-believe-this-one-weird-trick-to-handle-android-intent-extras-with-kotlin-845ecf09e0e9
class MagicCardImage : AppCompatActivity() {

    var magicCardList: List<CardsWithExpansion> ?= arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_tuts)

        magicCardList = intent.getParcelableArrayListExtra("magicCardList")
        val selectedPos = intent.getIntExtra("position", 0)
        println("passed card: ${magicCardList!![selectedPos]}")

        val images = arrayListOf("https://img.scryfall.com/cards/large/en/rix/3.jpg?1515154227", "https://img.scryfall.com/cards/large/en/rix/6.jpg?1515156347")

        val urls = mutableListOf<String>()
        magicCardList?.forEach { urls.add(getDefaultUrl(it)) }

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
//        adapterViewPager = MyPagerAdapter(this, magicCardList!!, supportFragmentManager)
        viewPager.adapter = MyCustomPagerAdapter(this, urls)
        viewPager.currentItem = selectedPos
        viewPager.offscreenPageLimit = 2
    }

    private fun getDefaultUrl(magicCard: CardsWithExpansion): String {
        val defaultUrl: String
        if (magicCard.magicCardsInfoCode.isEmpty() && magicCard.mciNumber != ("0")) {
            defaultUrl = "https://img.scryfall.com/cards/normal/en/${magicCard.setCode.toLowerCase()}/${magicCard.mciNumber}.jpg"
        } else if (magicCard.magicCardsInfoCode.isNotEmpty() && magicCard.mciNumber !=("0")) {
            defaultUrl = "https://magiccards.info/scans/en/${magicCard.magicCardsInfoCode}/${magicCard.mciNumber}.jpg"
        } else {
            var mid = magicCard.multiverseid
            mid = mid.dropLast(2)
            defaultUrl = "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=${mid}&type=card"
        }
        return defaultUrl
    }
}

class MyCustomPagerAdapter(internal var context: Context, internal var defaultUrl: List<String>) : PagerAdapter() {
    internal var layoutInflater: LayoutInflater


    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return defaultUrl.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = layoutInflater.inflate(R.layout.image_view_pager, container, false)

        val imageView = itemView.findViewById(R.id.image_view) as ImageView
//        imageView.setImageResource(images[position])

        Picasso.with(context)
                .load(defaultUrl[position])
                .placeholder(R.drawable.testing)
                .error(R.mipmap.ic_launcher_round)
                .into(imageView)

        container.addView(itemView)

        //listening to image click
        imageView.setOnClickListener(View.OnClickListener { Toast.makeText(context, "you clicked image " + (position + 1), Toast.LENGTH_LONG).show() })

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}