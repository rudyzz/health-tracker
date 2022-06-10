package com.rudy.finalproject.database.SQLiteHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.rudy.finalproject.database.HeightData
import java.lang.Exception
import kotlin.collections.ArrayList

class HeightHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "height.db"
        private const val TBL_HEIGHT = "tbl_height"
        private const val HEIGHT = "height"
        private const val TIME = "time"
        private const val TAG = "HeightHelper"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblHeight = ("CREATE TABLE " + TBL_HEIGHT + " ( " + HEIGHT + " REAL, " + TIME + " TEXT PRIMARY KEY);")
        Log.d("HeightHelper", "create table")
        db?.execSQL(createTblHeight)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL(("DROP TABLE IF EXISTS $TBL_HEIGHT"))
        onCreate(db)
    }

    fun insertHeight(ht: HeightData): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HEIGHT, ht.height)
        contentValues.put(TIME, ht.time)

        val success = db.insert(TBL_HEIGHT, null, contentValues)
        db.close()
        Log.d("HeightHelper", "insert")
        return success
    }

    fun getLatestHeight(): String{
        val query = "SELECT * FROM $TBL_HEIGHT ORDER BY $TIME DESC LIMIT 1"
        val db = this.readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(query, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(query)
            return "--"
        }
        if(cursor.moveToFirst()) {
            return cursor.getDouble(cursor.getColumnIndexOrThrow(HEIGHT)).toString()
        }
        else return "--"
    }

    fun getAllHeights(): ArrayList<HeightData>{
        val htList: ArrayList<HeightData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_HEIGHT"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var height: Double
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                height = cursor.getDouble(cursor.getColumnIndexOrThrow(HEIGHT))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val heightData = HeightData(height = height, time = time)
                htList.add(heightData)
            } while (cursor.moveToNext())
        }

        return htList
    }

    fun getFilteredHeights(startTime: Long): ArrayList<HeightData>{
        val htList: ArrayList<HeightData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_HEIGHT WHERE TIME >= $startTime"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var height: Double
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                height = cursor.getDouble(cursor.getColumnIndexOrThrow(HEIGHT))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val heightData = HeightData(height = height, time = time)
                htList.add(heightData)
            } while (cursor.moveToNext())
        }

        return htList
    }

}