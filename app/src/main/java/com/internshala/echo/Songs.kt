package com.internshala.echo

import android.os.Parcel
import android.os.Parcelable
class Songs(var songID:Long,var dateAdded:Long,var songTitle:String,var artist:String,var songData:String):
    Parcelable{
    override fun writeToParcel(dest: Parcel?, flags: Int) {

    }

    override fun describeContents(): Int {
       return 0
    }
object Statified{
    var nameComparator= Comparator<Songs> { song1, song2 ->
        var s1=song1.songTitle.toUpperCase()
        var s2=song2.songTitle.toUpperCase()
        s1.compareTo(s2)
    }
    var dateComparator=Comparator<Songs> { song1, song2 ->
        var s1=song1.dateAdded.toDouble()
        var s2=song2.dateAdded.toDouble()
        s1.compareTo(s2)

    }
}
}