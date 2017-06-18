package com.example.jptalusan.kotlintutorial

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.magic_card_activity.*

/**
 * Created by jptalusan on 6/16/17.
 */
//TODO: Study intents https://medium.com/@workingkills/you-wont-believe-this-one-weird-trick-to-handle-android-intent-extras-with-kotlin-845ecf09e0e9
class MagicCardImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.magic_card_activity)
        val card = intent.getStringExtra("url")

        Picasso.with(this)
                .load(card)
                .placeholder(R.drawable.testing)
                .error(R.mipmap.ic_launcher_round)
                .into(imageView)
    }
}