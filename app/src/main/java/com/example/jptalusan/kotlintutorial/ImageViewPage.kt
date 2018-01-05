package com.example.jptalusan.kotlintutorial

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView



//1
class ImageViewPage : Fragment() {
    private var magicCard: MagicCard ?= null

    //2
    companion object {

        fun newInstance(magicCard: MagicCard): ImageViewPage {
            val imageViewPage = ImageViewPage()
            val args = Bundle()
            args.putParcelable("magicCard", magicCard)
            imageViewPage.arguments = args
            return imageViewPage
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicCard = arguments.getParcelable("magicCard")
    }

    //3
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.image_view_pager, container, false)
        val tvLabel = view?.findViewById(R.id.textview) as TextView
        tvLabel.text = magicCard?.name
        //inflate here
        return view
    }

}