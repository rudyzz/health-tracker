package com.rudy.finalproject.database.SQLiteHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.rudy.finalproject.database.WeightData
import java.lang.Exception
import kotlin.collections.ArrayList

class WeightHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "weight.db"
        private const val TBL_WEIGHT = "tbl_weight"
        private const val WEIGHT = "weight"
        private const val TIME = "time"
        private const val TAG = "WeightHelper"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblWeight = ("CREATE TABLE " + TBL_WEIGHT + " ( " + WEIGHT + " REAL, " + TIME + " TEXT PRIMARY KEY);")
        Log.d(TAG, "create table")
        db?.execSQL(createTblWeight)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL(("DROP TABLE IF EXISTS $TBL_WEIGHT"))
        onCreate(db)
    }

    fun insertWeight(ht: WeightData): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(WEIGHT, ht.weight)
        contentValues.put(TIME, ht.time)

        val success = db.insert(TBL_WEIGHT, null, contentValues)
        db.close()
        Log.d(TAG, "insert")
        return success
    }

    fun getLatestWeight(): String{
        val query = "SELECT * FROM $TBL_WEIGHT ORDER BY $TIME DESC LIMIT 1"
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
            return cursor.getDouble(cursor.getColumnIndexOrThrow(WEIGHT)).toString()
        }
        else return "--"
    }

    fun getAllWeights(): ArrayList<WeightData>{
        val wtList: ArrayList<WeightData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_WEIGHT"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var weight: Double
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(WEIGHT))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val weightData = WeightData(weight = weight, time = time)
                wtList.add(weightData)
            } while (cursor.moveToNext())
        }

        return wtList
    }

    fun getFilteredWeights(startTime: Long): ArrayList<WeightData>{
        val wtList: ArrayList<WeightData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_WEIGHT WHERE TIME >= $startTime"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var weight: Double
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(WEIGHT))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val weightData = WeightData(weight = weight, time = time)
                wtList.add(weightData)
            } while (cursor.moveToNext())
        }

        return wtList
    }

}