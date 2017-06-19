package com.example.jptalusan.kotlintutorial

import android.provider.BaseColumns

data class MagicCard(
        val id: String,
        val expansion: String,
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
        return "$expansion\n$name\n$rarity\n$manaCost\n$power/$toughness\n$text\n$flavor"
    }
}