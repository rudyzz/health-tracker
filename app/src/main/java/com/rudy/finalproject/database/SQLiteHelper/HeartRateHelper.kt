package com.rudy.finalproject.database.SQLiteHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.rudy.finalproject.database.HeartRateData
import java.lang.Exception
import kotlin.collections.ArrayList

class HeartRateHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "heartRate.db"
        private const val TBL_HEART_RATE = "tbl_heart_rate"
        private const val HEART_RATE = "heartRate"
        private const val TIME = "time"
        private const val TAG = "HeartRateHelper"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblHeartRate = ("CREATE TABLE " + TBL_HEART_RATE + " ( " + HEART_RATE + " INTEGER, " + TIME + " TEXT PRIMARY KEY);")
        Log.d(TAG, "create table")
        db?.execSQL(createTblHeartRate)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL(("DROP TABLE IF EXISTS $TBL_HEART_RATE"))
        onCreate(db)
    }

    fun insertHeartRate(st: HeartRateData): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(HEART_RATE, st.heartRate)
        contentValues.put(TIME, st.time)

        val success = db.insert(TBL_HEART_RATE, null, contentValues)
        db.close()
        Log.d(TAG, "insert")
        return success
    }

    fun getLatestHeartRate(): String{
        val query = "SELECT * FROM $TBL_HEART_RATE ORDER BY $TIME DESC LIMIT 1"
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
            return cursor.getDouble(cursor.getColumnIndexOrThrow(HEART_RATE)).toString()
        }
        else return "--"
    }

    fun getAllHeartRates(): ArrayList<HeartRateData>{
        val stList: ArrayList<HeartRateData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_HEART_RATE"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var heartRate: Int
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                heartRate = cursor.getInt(cursor.getColumnIndexOrThrow(HEART_RATE))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val heartRateData = HeartRateData(heartRate = heartRate, time = time)
                stList.add(heartRateData)
            } while (cursor.moveToNext())
        }

        return stList
    }

    fun getFilteredHeartRates(startTime: Long): ArrayList<HeartRateData>{
        val stList: ArrayList<HeartRateData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_HEART_RATE WHERE TIME >= $startTime"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var heartRate: Int
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                heartRate = cursor.getInt(cursor.getColumnIndexOrThrow(HEART_RATE))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val heartRateData = HeartRateData(heartRate = heartRate, time = time)
                stList.add(heartRateData)
            } while (cursor.moveToNext())
        }

        return stList
    }

}