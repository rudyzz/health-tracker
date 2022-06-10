package com.rudy.finalproject

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.rudy.finalproject.adapter.StepsAdapter
import com.rudy.finalproject.database.SQLiteHelper.StepsHelper
import com.rudy.finalproject.database.StepsData
import java.util.*
import kotlin.math.min

class StepsDetailActivity : AppCompatActivity() {

    private lateinit var stepsHelper: StepsHelper
    private lateinit var mySteps: ArrayList<StepsData>
    private var series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var timeSeries: LineGraphSeries<DataPoint> = LineGraphSeries()

    private lateinit var stepsGraph: GraphView
    private lateinit var stepsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_detail)

        stepsHelper = StepsHelper(this)
        mySteps = stepsHelper.getAllSteps()

        val scaleWeek: Button = findViewById(R.id.scale_week)
        val scaleMonth: Button = findViewById(R.id.scale_month)
        val scaleYear: Button = findViewById(R.id.scale_year)
        stepsGraph = findViewById(R.id.steps_graph)
        stepsRecyclerView = findViewById<RecyclerView>(R.id.stepsRecyclerView)

        //Set GraphView
        val viewPort = stepsGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(1.0)
        viewPort.setMaxX(5.0)
        viewPort.isYAxisBoundsManual = true
        viewPort.setMinY(0.0)
        viewPort.setMaxY(1000.0)
        refreshGraph()

        // Set RecyclerView
        stepsRecyclerView.adapter = StepsAdapter(this, mySteps)
        stepsRecyclerView.setHasFixedSize(true)

        scaleWeek.setOnClickListener { refreshGraphInTimeScale("WEEK") }
        scaleMonth.setOnClickListener { refreshGraphInTimeScale("MONTH") }
        scaleYear.setOnClickListener { refreshGraphInTimeScale("YEAR") }

    }

    private fun refreshGraph() {
        mySteps = stepsHelper.getAllSteps()
        var count = mySteps.size
        count = min(count, 5)
        series = LineGraphSeries()
        for (i in count downTo 1) {
            series.appendData(
                DataPoint((6 - i).toDouble(), mySteps[mySteps.lastIndex - i + 1].steps.toDouble()),
                true,
                5
            )
        }
        stepsGraph.removeAllSeries()
        stepsGraph.addSeries(series)
        stepsRecyclerView.adapter = StepsAdapter(this, mySteps)
    }

    private fun refreshGraphInTimeScale(option: String) {
        val scale: Int = when (option) {
            "WEEK" -> 7
            "MONTH" -> 30
            else -> 365
        }
        val now = System.currentTimeMillis()
        val startTime = now - scale.toLong() * 1000L * 60L * 60L * 24L
        val filteredSteps = stepsHelper.getFilteredSteps(startTime)
        val count = filteredSteps.size
        timeSeries = LineGraphSeries()
        for (i in 0 until count) {
            timeSeries.appendData(
                DataPoint(Date(filteredSteps[i].time), filteredSteps[i].steps.toDouble()),
                true,
                100
            )
        }

        val gridLabelRenderer = stepsGraph.gridLabelRenderer
        gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        gridLabelRenderer.numHorizontalLabels = 3

        val viewPort = stepsGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(startTime.toDouble())
        viewPort.setMaxX(now.toDouble())
        stepsGraph.removeAllSeries()
        stepsGraph.addSeries(timeSeries)

    }

    companion object {
        private const val TAG = "StepsDetailActivity"
    }

}
