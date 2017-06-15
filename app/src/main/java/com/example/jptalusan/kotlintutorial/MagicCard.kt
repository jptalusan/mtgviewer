package com.example.jptalusan.kotlintutorial

import android.provider.BaseColumns

class MagicCard(
        val id: Int,
        val name: String,
        val manaCost: String,
        val imageUrl: String,
        val power: String,
        val toughness: String,
        val type: String,
        val artist: String,
        val flavor: String,
        val text: String,
        val rarity: String,
        val variations: String) {

    override fun toString(): String {
        return "$name\n$rarity\n$manaCost\n $power/$toughness\n $text\n$flavor"
    }
}