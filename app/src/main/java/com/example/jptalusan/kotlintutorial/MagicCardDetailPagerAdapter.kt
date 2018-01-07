package com.example.jptalusan.kotlintutorial

import android.graphics.Movie
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.example.jptalusan.kotlintutorial.MTGClasses.CardsWithExpansion

private const val MAX_VALUE = 250

class MagicCardDetailPagerAdapter(fragmentManager: FragmentManager, private val cards: List<CardsWithExpansion>) :
        FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return MagicCardDetailFragment.newInstance(cards[position])// % cards.size])
    }

    override fun getCount(): Int {
        return cards.size// * MAX_VALUE
    }
}