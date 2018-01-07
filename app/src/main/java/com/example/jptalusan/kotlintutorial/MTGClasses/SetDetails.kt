package com.example.jptalusan.kotlintutorial.MTGClasses

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by jptalusan on 1/6/18.
 */
data class SetDetails (
        val name: String,
        val code: String,
        val gathererCode: String = "",
        val magicCardsInfoCode: String = "",
        val releaseDate: String,
        val cards: List<Cards>
)

data class Cards (
        val artist: String,
        val flavor: String = "",
        val manaCost: String = "",
        val multiverseid: Double = 0.0,
        val name: String,
        val number: String = "",
        val power: String = "",
        val rarity: String,
        val text: String = "",
        val toughness: String = "",
        val mciNumber: Int = 0,
        val variations: List<Int> = arrayListOf(),
        val types: List<String> = arrayListOf(),
        val colors: List<String> = listOf("Colorless")
)

data class CardsWithExpansion (
        val setCode: String,
        val magicCardsInfoCode: String = "",
        val artist: String,
        val flavor: String = "",
        val manaCost: String = "",
        val multiverseid: String = "",
        val name: String,
        val number: String = "",
        val power: String = "",
        val toughness: String = "",
        val rarity: String,
        val text: String = "",
        val mciNumber: String = "",
        val types: String = "",
        val variations: String = "",
        val colors: String = "Colorless"

) : Parcelable {
    constructor(parcelIn: Parcel) : this(
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString()
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<CardsWithExpansion> {
            override fun createFromParcel(`in`: Parcel): CardsWithExpansion {
                return CardsWithExpansion(`in`)
            }

            override fun newArray(size: Int): Array<CardsWithExpansion?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(setCode)
        dest.writeString(magicCardsInfoCode)
        dest.writeString(artist)
        dest.writeString(flavor)
        dest.writeString(manaCost)
        dest.writeString(multiverseid)
        dest.writeString(name)
        dest.writeString(number)
        dest.writeString(power)
        dest.writeString(toughness)
        dest.writeString(rarity)
        dest.writeString(text)
        dest.writeString(mciNumber)
        dest.writeString(types)
        dest.writeString(variations)
        dest.writeString(colors)
    }


    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return (this.text + this.power + this.toughness + this.types + this.flavor)
    }
}