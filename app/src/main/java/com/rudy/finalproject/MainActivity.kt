package com.rudy.finalproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.timepicker.TimeFormat
import com.rudy.finalproject.database.SQLiteHelper.HeartRateHelper
import com.rudy.finalproject.database.SQLiteHelper.HeightHelper
import com.rudy.finalproject.database.SQLiteHelper.StepsHelper
import com.rudy.finalproject.database.SQLiteHelper.WeightHelper
import com.rudy.finalproject.database.StepsData
import org.w3c.dom.Text
import java.text.DateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var running = false
    private var stepsToday: Int = 0
    private lateinit var lastStepsCountingDate: String
    private lateinit var formattedDate : String
    private lateinit var stepsText: TextView


    private lateinit var heightText: TextView
    private lateinit var weightText: TextView
    private lateinit var heartRateText: TextView

    private lateinit var heightHelper: HeightHelper
    private lateinit var weightHelper: WeightHelper
    private lateinit var stepsHelper: StepsHelper
    private lateinit var heartRateHelper: HeartRateHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshTime()

        // SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Database Helper
        heightHelper = HeightHelper(this)
        weightHelper = WeightHelper(this)
        stepsHelper = StepsHelper(this)
        heartRateHelper = HeartRateHelper(this)

        heightText = findViewById(R.id.height_text)
        weightText = findViewById(R.id.weight_text)
        stepsText = findViewById(R.id.steps_text)
        heartRateText = findViewById(R.id.heart_rate_text)

        heightText.text = "Height: " + heightHelper.getLatestHeight()
        weightText.text = "Weight: " + weightHelper.getLatestWeight()
        stepsText.text = "Steps Today: " + stepsToday.toString()
        heartRateText.text = "Heart Rate: " + heartRateHelper.getLatestHeartRate()

        lastStepsCountingDate = stepsHelper.getLastStepsCountingDate()

        val heightDetail: ImageButton = findViewById(R.id.height_detail)
        heightDetail.setOnClickListener {
            val intent = Intent(this, HeightDetailActivity::class.java)
            startActivity(intent)
        }
        val weightDetail: ImageButton = findViewById(R.id.weight_detail)
        weightDetail.setOnClickListener {
            val intent = Intent(this, WeightDetailActivity::class.java)
            startActivity(intent)
        }
        val stepsDetail: ImageButton = findViewById(R.id.steps_detail)
        stepsDetail.setOnClickListener {
            val intent = Intent(this, StepsDetailActivity::class.java)
            startActivity(intent)
        }
        val heartRateDetail: ImageButton = findViewById(R.id.heart_rate_detail)
        heartRateDetail.setOnClickListener {
            val intent = Intent(this, HeartRateDetailActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        refreshTime()

        running = true
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(this, "No Step Counter Sensor!", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }

        heightText.text = "Height: " + heightHelper.getLatestHeight()
        weightText.text = "Weight: " + weightHelper.getLatestWeight()
        heartRateText.text = "Heart Rate: " + heartRateHelper.getLatestHeartRate()

        //lastStepsCountingDate = stepsHelper.getLastStepsCountingDate()
        Log.d(TAG, "$lastStepsCountingDate")
        if (lastStepsCountingDate != formattedDate) {
            stepsHelper.insertSteps(StepsData(stepsToday, getDaysAgo(1).time))
            stepsToday = 0
            stepsText.text = "Steps Today: " + stepsToday.toString()
            lastStepsCountingDate = formattedDate
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running and ((lastStepsCountingDate == "--") or (lastStepsCountingDate == formattedDate))) {
            if (event != null) {
                stepsToday += 1
                stepsText.text = "Steps Today: $stepsToday"
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    fun refreshTime() {
        val rightNow = Calendar.getInstance()
        formattedDate = DateFormat.getDateInstance(DateFormat.FULL).format(rightNow.time)
        val hour = rightNow.get(Calendar.HOUR_OF_DAY)
        val splitDate = formattedDate.split(',')

        val today: TextView = findViewById(R.id.today)
        val date: TextView = findViewById(R.id.date)
        val greetings: TextView = findViewById(R.id.greetings)
        greetings.text = when(hour) {
            in 6..11 -> "Good Morning!"
            in 12..18 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
        today.text = "Today is ${splitDate[0].trim()}"
        date.text = "${splitDate[1].trim()}, ${splitDate[2].trim()}"
    }

    fun getDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}