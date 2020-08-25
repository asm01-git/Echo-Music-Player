package com.internshala.echo.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.Song
import com.internshala.echo.adapters.MainScreenAdapter

class EchoDatabase:SQLiteOpenHelper {
    /*List for storing the favorite songs*/
    var context:Context?=null
    object s{
        var playLists=ArrayList<String>()
        val DB_NAME = "FavoriteDatabase"
        val COLUMN_PLAYLIST_NAME="PlaylistName"
        var TABLE_NAME = "PlayListsTable"
        val COLUMN_ID = "SongID"
        const val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
        var DB_VERSION=1
        var playListsStored:Boolean=false
    }
    override fun onCreate(db: SQLiteDatabase?) {
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
    constructor(context: Context?):super(context,s.DB_NAME,null,s.DB_VERSION){
        this.context=context
    }
    fun createPlaylistsTable(){
        val db=this.writableDatabase
        try {
            db?.execSQL("CREATE TABLE IF NOT EXISTS " + s.TABLE_NAME + "( " + s.COLUMN_PLAYLIST_NAME + " STRING);")
            queryPlaylistsTable()
            s.playListsStored=true
        }
        catch(e:Exception){
            e.printStackTrace()
            Log.e("tag",e.cause.toString())
            Toast.makeText(context,e.message,Toast.LENGTH_LONG).show()
        }
    }
    fun createPlayList(name:String){
        if(s.playLists.contains(name))
            MainScreenAdapter.makeToast("Playlist Already Exists!",context)
        else {
            try {
                val db = this.writableDatabase
                val playListEntry = ContentValues()
                playListEntry.put(s.COLUMN_PLAYLIST_NAME, name)
                db.insert(s.TABLE_NAME, null, playListEntry)
                db?.execSQL(
                    "CREATE TABLE " + name + "( " + s.COLUMN_ID +
                            " INTEGER," + s.COLUMN_SONG_ARTIST + " STRING," + s.COLUMN_SONG_TITLE + " STRING,"
                            + s.COLUMN_SONG_PATH + " STRING);"
                )
                makeToast("Playlist "+name+" Created successfully!")
            }
            catch (e:Exception){
                e.printStackTrace()
                makeToast(e.message as String)
            }
            s.playLists.add(name)
        }
    }
    fun storeAsFavorite(song:Song, playList: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(s.COLUMN_ID, song.songID)
        contentValues.put(s.COLUMN_SONG_ARTIST, song.songArtist)
        contentValues.put(s.COLUMN_SONG_TITLE, song.songTitle)
        contentValues.put(s.COLUMN_SONG_PATH, song.songData)
        db.insert(playList, null, contentValues)
        db.close()
    }
    /*This method asks the database for the list of Songs stored as favorite*/
    fun queryPlaylist(playlist:String): ArrayList<Song>? {
        var _songList = ArrayList<Song>()
        try {
            val db = this.readableDatabase
            val query_params = "SELECT * FROM "+playlist
            var cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()) {
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(s.COLUMN_ID))
                    var _artist =
                        cSor.getString(cSor.getColumnIndexOrThrow(s.COLUMN_SONG_ARTIST))
                    var _title =
                        cSor.getString(cSor.getColumnIndexOrThrow(s.COLUMN_SONG_TITLE))
                    var _songPath =
                        cSor.getString(cSor.getColumnIndexOrThrow(s.COLUMN_SONG_PATH))
                    _songList.add(Song(_id.toLong(), 0, _title, _artist, _songPath))
                }
                while (cSor.moveToNext())
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return _songList
    }
    fun queryPlaylistsTable(){
        try {
            val db = this.readableDatabase
            val csor = db?.rawQuery("Select * from " + s.TABLE_NAME, null)
            if (csor?.moveToFirst() as Boolean) {
                do {
                    s.playLists.add(csor.getString(csor.getColumnIndexOrThrow(s.COLUMN_PLAYLIST_NAME)))
                } while (csor.moveToNext())
            }
            else{
                MainScreenAdapter.makeToast("No playlists found!Go to favourites tab and create one!",context)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
            MainScreenAdapter.makeToast(e.message as String,context)
        }
    }
    fun checkifIdExists(_id: Long,playListToBeChecked:String): Boolean {
        var storeId = -1
        try {
            val db = this.readableDatabase
            val query_params = "SELECT * FROM "+playListToBeChecked+" WHERE "+s.COLUMN_ID+" = "+_id
            val cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()) {
                do {
                    storeId = cSor.getInt(cSor.getColumnIndexOrThrow(s.COLUMN_ID))
                } while (cSor.moveToNext())
            } else {
                return false
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
        return storeId != -1
    }

    fun deleteFavourite(_id: Long, playListName:String) {
        val db = this.writableDatabase
        db.delete(playListName, s.COLUMN_ID + " = " + _id, null)
        db.close()
    }
    fun deletePlaylist(playlistName: String){
        val db=this.writableDatabase
        db.delete(s.TABLE_NAME,s.COLUMN_PLAYLIST_NAME+" = "+playlistName,null)
        db.execSQL("Drop Table if exists $playlistName")
        db.close()
    }
    fun checkSize(): Int {
        var counter = 0
        val db = this.readableDatabase
        var query_params = "SELECT * FROM " + s.TABLE_NAME
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                counter = counter + 1
            } while (cSor.moveToNext())
        } else {
            return 0
        }
        return counter
    }
    private fun makeToast(s:String){
        Toast.makeText(context,s,Toast.LENGTH_LONG).show()
    }
}