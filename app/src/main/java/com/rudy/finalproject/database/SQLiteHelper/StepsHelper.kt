package com.rudy.finalproject.database.SQLiteHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.rudy.finalproject.database.StepsData
import java.lang.Exception
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class StepsHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "steps.db"
        private const val TBL_STEPS = "tbl_steps"
        private const val STEPS = "steps"
        private const val TIME = "time"
        private const val TAG = "StepsHelper"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblSteps = ("CREATE TABLE " + TBL_STEPS + " ( " + STEPS + " INTEGER, " + TIME + " TEXT PRIMARY KEY);")
        Log.d(TAG, "create table")
        db?.execSQL(createTblSteps)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL(("DROP TABLE IF EXISTS $TBL_STEPS"))
        onCreate(db)
    }

    fun insertSteps(st: StepsData): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(STEPS, st.steps)
        contentValues.put(TIME, st.time)

        val success = db.insert(TBL_STEPS, null, contentValues)
        db.close()
        Log.d("StepsHelper", "insert")
        return success
    }


    fun getLastStepsCountingDate(): String{
        val query = "SELECT * FROM $TBL_STEPS ORDER BY $TIME DESC LIMIT 1"
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
            val time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))
            val date = Date(time)
            return DateFormat.getDateInstance(DateFormat.FULL).format(getDaysAgo(date, 1))
        } else return "--"


    }

    fun getAllSteps(): ArrayList<StepsData>{
        val stList: ArrayList<StepsData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_STEPS"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var steps: Int
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                steps = cursor.getInt(cursor.getColumnIndexOrThrow(STEPS))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val stepsData = StepsData(steps = steps, time = time)
                stList.add(stepsData)
            } while (cursor.moveToNext())
        }

        return stList
    }

    fun getFilteredSteps(startTime: Long): ArrayList<StepsData>{
        val stList: ArrayList<StepsData> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_STEPS WHERE TIME >= $startTime"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var steps: Int
        var time: Long

        if (cursor.moveToFirst()) {
            do{
                steps = cursor.getInt(cursor.getColumnIndexOrThrow(STEPS))
                time = cursor.getLong(cursor.getColumnIndexOrThrow(TIME))

                val stepsData = StepsData(steps = steps, time = time)
                stList.add(stepsData)
            } while (cursor.moveToNext())
        }

        return stList
    }

    private fun getDaysAgo(date: Date, daysAgo: Long): Date {
        return Date(date.time - (daysAgo * 1000L * 60L * 60L * 24L))
    }

}