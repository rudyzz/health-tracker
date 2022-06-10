package com.rudy.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.rudy.finalproject.adapter.HeartRateAdapter
import com.rudy.finalproject.database.HeartRateData
import com.rudy.finalproject.database.SQLiteHelper.HeartRateHelper
import java.util.*
import kotlin.math.min

class HeartRateDetailActivity : AppCompatActivity() {

    private lateinit var heartRateHelper: HeartRateHelper
    private lateinit var myHeartRate: ArrayList<HeartRateData>
    private var series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var timeSeries: LineGraphSeries<DataPoint> = LineGraphSeries()

    private lateinit var heartRateGraph: GraphView
    private lateinit var heartRateRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_detail)

        heartRateHelper = HeartRateHelper(this)
        myHeartRate = heartRateHelper.getAllHeartRates()

        val newHeartRate: ImageButton = findViewById(R.id.new_heart_rate)
        val scaleWeek: Button = findViewById(R.id.scale_week)
        val scaleMonth: Button = findViewById(R.id.scale_month)
        val scaleYear: Button = findViewById(R.id.scale_year)
        heartRateGraph = findViewById(R.id.heart_rate_graph)
        heartRateRecyclerView = findViewById<RecyclerView>(R.id.heartRateRecyclerView)

        //Set GraphView
        val viewPort = heartRateGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(1.0)
        viewPort.setMaxX(5.0)
        viewPort.isYAxisBoundsManual = true
        viewPort.setMinY(40.0)
        viewPort.setMaxY(150.0)
        refreshGraph()

        // Set RecyclerView
        heartRateRecyclerView.adapter = HeartRateAdapter(this, myHeartRate)
        heartRateRecyclerView.setHasFixedSize(true)

        newHeartRate.setOnClickListener { addHeartRate() }

        scaleWeek.setOnClickListener { refreshGraphInTimeScale("WEEK") }
        scaleMonth.setOnClickListener { refreshGraphInTimeScale("MONTH") }
        scaleYear.setOnClickListener { refreshGraphInTimeScale("YEAR") }

    }

    override fun onResume() {
        super.onResume()
        refreshGraph()
        heartRateRecyclerView.adapter = HeartRateAdapter(this, myHeartRate)
    }

    private fun addHeartRate() {
        val intent = Intent(this, HeartRateDetectActivity::class.java)
        startActivity(intent)
    }

    private fun refreshGraph() {
        myHeartRate = heartRateHelper.getAllHeartRates()
        var count = myHeartRate.size
        count = min(count, 5)
        series = LineGraphSeries()
        for (i in count downTo 1) {
            series.appendData(
                DataPoint((6 - i).toDouble(), myHeartRate[myHeartRate.lastIndex - i + 1].heartRate.toDouble()),
                true,
                5
            )
        }
        heartRateGraph.removeAllSeries()
        heartRateGraph.addSeries(series)
        heartRateRecyclerView.adapter = HeartRateAdapter(this, myHeartRate)
    }

    private fun refreshGraphInTimeScale(option: String) {
        val scale: Int = when (option) {
            "WEEK" -> 7
            "MONTH" -> 30
            else -> 365
        }
        val now = System.currentTimeMillis()
        val startTime = now - scale.toLong() * 1000L * 60L * 60L * 24L
        val filteredHeartRate = heartRateHelper.getFilteredHeartRates(startTime)
        val count = filteredHeartRate.size
        timeSeries = LineGraphSeries()
        for (i in 0 until count) {
            timeSeries.appendData(
                DataPoint(Date(filteredHeartRate[i].time), filteredHeartRate[i].heartRate.toDouble()),
                true,
                100
            )
        }

        val gridLabelRenderer = heartRateGraph.gridLabelRenderer
        gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        gridLabelRenderer.numHorizontalLabels = 3

        val viewPort = heartRateGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(startTime.toDouble())
        viewPort.setMaxX(now.toDouble())
        heartRateGraph.removeAllSeries()
        heartRateGraph.addSeries(timeSeries)

    }

    companion object {
        private const val TAG = "HeartRateDetailActivity"
    }

}
