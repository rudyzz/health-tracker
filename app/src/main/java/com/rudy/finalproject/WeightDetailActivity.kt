package com.rudy.finalproject

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.rudy.finalproject.adapter.WeightAdapter
import com.rudy.finalproject.database.SQLiteHelper.WeightHelper
import com.rudy.finalproject.database.WeightData
import java.util.*
import kotlin.math.min

class WeightDetailActivity : AppCompatActivity() {

    private lateinit var weightHelper: WeightHelper
    private lateinit var myWeight: ArrayList<WeightData>
    private var series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var timeSeries: LineGraphSeries<DataPoint> = LineGraphSeries()

    private lateinit var weightGraph: GraphView
    private lateinit var weightRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_detail)

        weightHelper = WeightHelper(this)
        myWeight = weightHelper.getAllWeights()

        val newWeight: ImageButton = findViewById(R.id.new_weight)
        val scaleWeek: Button = findViewById(R.id.scale_week)
        val scaleMonth: Button = findViewById(R.id.scale_month)
        val scaleYear: Button = findViewById(R.id.scale_year)
        weightGraph = findViewById(R.id.weight_graph)
        weightRecyclerView = findViewById<RecyclerView>(R.id.weightRecyclerView)

        //Set GraphView
        val viewPort = weightGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(1.0)
        viewPort.setMaxX(5.0)
        viewPort.isYAxisBoundsManual = true
        viewPort.setMinY(0.0)
        viewPort.setMaxY(300.0)
        refreshGraph()

        // Set RecyclerView
        weightRecyclerView.adapter = WeightAdapter(this, myWeight)
        weightRecyclerView.setHasFixedSize(true)

        newWeight.setOnClickListener { addWeight() }

        scaleWeek.setOnClickListener { refreshGraphInTimeScale("WEEK") }
        scaleMonth.setOnClickListener { refreshGraphInTimeScale("MONTH") }
        scaleYear.setOnClickListener { refreshGraphInTimeScale("YEAR") }

    }

    private fun addWeight(){
        val dialogBuilder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.setRawInputType(Configuration.KEYBOARD_12KEY)
        dialogBuilder.setMessage("Add your new weight here")
            .setView(editText)
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id ->
                val weight = editText.text.toString()
                //val time = Calendar.getInstance().time.toString()
                val time = System.currentTimeMillis()
                if (weight.isEmpty()) {
                    Toast.makeText(this,
                        "Please enter a valid weight",
                        Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val wt = WeightData(weight.toDouble(), time)
                    val status = weightHelper.insertWeight(wt)
                    Log.d("Weight", "$weight, $time")
                    if (status > -1) {
                        Toast.makeText(this, "New Weight Added", Toast.LENGTH_SHORT).show()
                        editText.setText("")
                        refreshGraph()
                    } else{
                        Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show()
                    }
                }

            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Add New Data")
        // show alert dialog
        alert.show()
    }

    private fun refreshGraph() {
        myWeight = weightHelper.getAllWeights()
        var count = myWeight.size
        count = min(count, 5)
        series = LineGraphSeries()
        for (i in count downTo 1) {
            series.appendData(
                DataPoint((6 - i).toDouble(), myWeight[myWeight.lastIndex - i + 1].weight),
                true,
                5
            )
        }
        weightGraph.removeAllSeries()
        weightGraph.addSeries(series)
        weightRecyclerView.adapter = WeightAdapter(this, myWeight)
    }

    private fun refreshGraphInTimeScale(option: String) {
        val scale: Int = when (option) {
            "WEEK" -> 7
            "MONTH" -> 30
            else -> 365
        }
        val now = System.currentTimeMillis()
        val startTime = now - scale.toLong() * 1000L * 60L * 60L * 24L
        val filteredWeight = weightHelper.getFilteredWeights(startTime)
        val count = filteredWeight.size
        timeSeries = LineGraphSeries()
        for (i in 0 until count) {
            timeSeries.appendData(
                DataPoint(Date(filteredWeight[i].time), filteredWeight[i].weight),
                true,
                100
            )
        }

        val gridLabelRenderer = weightGraph.gridLabelRenderer
        gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        gridLabelRenderer.numHorizontalLabels = 3

        val viewPort = weightGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(startTime.toDouble())
        viewPort.setMaxX(now.toDouble())
        weightGraph.removeAllSeries()
        weightGraph.addSeries(timeSeries)

    }

    companion object {
        private const val TAG = "WeightDetailActivity"
    }

}
