package com.example.jptalusan.kotlintutorial.MTGClasses

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//Tips for serializing
class StringToDateTime : JsonSerializer<String>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: String?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        gen!!.writeObject(df.parse(value!!))
    }
}

data class Set (
        var name: String,
        var code: String,
//        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
//        @JsonSerialize(`as` = StringToDateTime::class)
        var releaseDate: String,
        var block: String = "",
        var downloaded: String = "False"
) : Parcelable {


    constructor(parcelIn: Parcel) : this(
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString(),
            parcelIn.readString()
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Set> {
            override fun createFromParcel(`in`: Parcel): Set {
                return Set(`in`)
            }

            override fun newArray(size: Int): Array<Set?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(code)
        dest.writeString(releaseDate)
        dest.writeString(block)
        dest.writeString(downloaded)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
class CompareSetReleaseDates {
    companion object : Comparator<Set> {
        private val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        override fun compare(a: Set, b: Set): Int = when {
            df.parse(a.releaseDate) > df.parse(b.releaseDate) -> 1
            df.parse(a.releaseDate) < df.parse(b.releaseDate) -> -1
            else -> 0
        }
    }
}