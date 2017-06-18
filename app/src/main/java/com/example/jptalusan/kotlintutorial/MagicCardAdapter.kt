package com.example.jptalusan.kotlintutorial

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.magic_card_list_item.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class MagicCardAdapter(var cardList: List<MagicCard>)
    : RecyclerView.Adapter<MagicCardAdapter.ViewHolder>() {

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        var v = LayoutInflater.from(parent!!.context).inflate(R.layout.magic_card_list_item, parent, false)

        return ViewHolder(v).listen { position, _ ->
//            parent!!.context.toast(cardList[position].name)
            selectedPosition = position
            notifyDataSetChanged()

            parent!!.context.startActivity<MagicCardImage>("url" to cardList[position].imageUrl)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

        if(selectedPosition == position) {
            holder!!.itemView.backgroundColor = Color.parseColor("#000000CC")
        } else {
            holder!!.itemView.backgroundColor = Color.parseColor("#ffffff")
        }

        (holder as ViewHolder)
                .bindData(
                        cardList[position]
                )


    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindData(magicCard: MagicCard)
        {
            itemView.name.text = magicCard.name;
            itemView.type.text = magicCard.type;
            itemView.cost.text = magicCard.manaCost;
            itemView.text.text = magicCard.text;
            var stat: String? = ""
            if (magicCard.type.contains("Creature")) {
                stat = magicCard.power + "/" + magicCard.toughness
            }
            itemView.stats.text = stat
            itemView.rarity.text = magicCard.rarity;
        }
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}