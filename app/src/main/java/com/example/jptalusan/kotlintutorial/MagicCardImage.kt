package com.example.jptalusan.kotlintutorial

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.magic_card_activity.*
import org.jetbrains.anko.progressDialog

//TODO: Study intents https://medium.com/@workingkills/you-wont-believe-this-one-weird-trick-to-handle-android-intent-extras-with-kotlin-845ecf09e0e9
class MagicCardImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.magic_card_activity)
        val magicCard = intent.getParcelableExtra<MagicCard>("magicCard")
        println("passed card: $magicCard")

        var defaultUrl = "https://img.scryfall.com/cards/normal/en/${magicCard.expansion.toLowerCase()}/${magicCard.number}.jpg"
        if (magicCard.number.isEmpty()) {
            defaultUrl = "https://magiccards.info/scans/en/${magicCard.infoCode}/${magicCard.id}.jpg"
        }
        println(defaultUrl)
        Picasso.with(applicationContext)
                .load(defaultUrl)
                .placeholder(R.drawable.testing)
                .error(R.mipmap.ic_launcher_round)
                .into(imageView)

        //Infocode to switch to magiccards.info --> "pro"
    }
}