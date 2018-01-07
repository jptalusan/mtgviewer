package com.example.jptalusan.kotlintutorial

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.jptalusan.kotlintutorial.MTGClasses.CardsWithExpansion

class MagicCardDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {

        // Creates the view controlled by the fragment
        val view = inflater.inflate(R.layout.magic_card_fragment, container, false)
        val posterImageView = view.findViewById<ImageView>(R.id.imageView)
        val progress = view.findViewById<ProgressBar>(R.id.progress)
        // Retrieve and display the movie data from the Bundle
        val args = arguments
        // Download the image and display it using Picasso
        val magicCard: CardsWithExpansion = args!!.getParcelable("magicCard")

        progress.visibility = View.VISIBLE

        Glide.with(this)
                .load(getDefaultUrl(magicCard))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        progress.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean {
                        progress.visibility = View.GONE
                        return false
                    }
                })
                .into(posterImageView)

        posterImageView.setOnClickListener {
            Toast.makeText(context, magicCard.setCode + ":" + magicCard.rarity, Toast.LENGTH_LONG).show()
//            val ft = fragmentManager.beginTransaction()
//            ft.replace(R.id.details, NewFragmentToReplace(), "NewFragmentTag")
//            ft.commit()
//            ft.addToBackStack(null)
        }

        return view
    }

    companion object {

        // Method for creating new instances of the fragment
        fun newInstance(magicCard: CardsWithExpansion): MagicCardDetailFragment {

            // Store the movie data in a Bundle object
            val args = Bundle()
            args.putParcelable("magicCard", magicCard)

            // Create a new MovieFragment and set the Bundle as the arguments
            // to be retrieved and displayed when the view is created
            val fragment = MagicCardDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private fun getDefaultUrl(magicCard: CardsWithExpansion): String {
        var defaultUrl = ""
        if (magicCard.mciNumber.isNotEmpty() && magicCard.mciNumber != ("0")) {
            if (magicCard.magicCardsInfoCode.isEmpty()) {
                defaultUrl = "https://img.scryfall.com/cards/normal/en/${magicCard.setCode.toLowerCase()}/${magicCard.mciNumber}.jpg"
            } else if (magicCard.magicCardsInfoCode.isNotEmpty()) {
                defaultUrl = "https://magiccards.info/scans/en/${magicCard.magicCardsInfoCode}/${magicCard.mciNumber}.jpg"
            }
        } else {
            var mid = magicCard.multiverseid
            mid = mid.dropLast(2)
            defaultUrl = "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=${mid}&type=card"
        }
//        println(defaultUrl)
        return defaultUrl
    }

}