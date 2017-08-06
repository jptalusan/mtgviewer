package com.example.jptalusan.kotlintutorial

import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns

data class MagicCard(
        val id: String,
        val infoCode: String,
        val expansion: String,
        val name: String,
        val manaCost: String,
        val multiverseid: String,
        val number: String,
        val power: String,
        val toughness: String,
        val type: String,
        val artist: String,
        val flavor: String,
        val text: String,
        val rarity: String,
        val variations: String) : Parcelable {

    protected constructor(parcelIn: Parcel) : this(
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
        val CREATOR = object : Parcelable.Creator<MagicCard> {
            override fun createFromParcel(`in`: Parcel): MagicCard {
                return MagicCard(`in`)
            }

            override fun newArray(size: Int): Array<MagicCard?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(infoCode)
        dest.writeString(expansion)
        dest.writeString(name)
        dest.writeString(manaCost)
        dest.writeString(multiverseid)
        dest.writeString(number)
        dest.writeString(power)
        dest.writeString(toughness)
        dest.writeString(type)
        dest.writeString(artist)
        dest.writeString(flavor)
        dest.writeString(text)
        dest.writeString(rarity)
        dest.writeString(variations)
    }

    override fun describeContents() = 0

    override fun toString(): String {
        val output = """
        |ID: $id
        |MultiverseId: $multiverseid
        |Expansion: $expansion
        |InfoCode: $infoCode
        |Number: $number
        |Name: $name
        """.trimMargin()
        return output
    }
}