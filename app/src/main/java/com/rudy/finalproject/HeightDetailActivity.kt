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
import com.rudy.finalproject.adapter.HeightAdapter
import com.rudy.finalproject.database.HeightData
import com.rudy.finalproject.database.SQLiteHelper.HeightHelper
import java.util.*
import kotlin.math.min

class HeightDetailActivity : AppCompatActivity() {

    private lateinit var heightHelper: HeightHelper
    private lateinit var myHeight: ArrayList<HeightData>
    private var series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var timeSeries: LineGraphSeries<DataPoint> = LineGraphSeries()

    private lateinit var heightGraph: GraphView
    private lateinit var heightRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_height_detail)

        heightHelper = HeightHelper(this)
        myHeight = heightHelper.getAllHeights()

        val newHeight: ImageButton = findViewById(R.id.new_height)
        val scaleWeek: Button = findViewById(R.id.scale_week)
        val scaleMonth: Button = findViewById(R.id.scale_month)
        val scaleYear: Button = findViewById(R.id.scale_year)
        heightGraph = findViewById(R.id.height_graph)
        heightRecyclerView = findViewById<RecyclerView>(R.id.heightRecyclerView)

        //Set GraphView
        val viewPort = heightGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(1.0)
        viewPort.setMaxX(5.0)
        viewPort.isYAxisBoundsManual = true
        viewPort.setMinY(0.0)
        viewPort.setMaxY(10.0)
        refreshGraph()

        // Set RecyclerView
        heightRecyclerView.adapter = HeightAdapter(this, myHeight)
        heightRecyclerView.setHasFixedSize(true)

        newHeight.setOnClickListener { addHeight() }

        scaleWeek.setOnClickListener { refreshGraphInTimeScale("WEEK") }
        scaleMonth.setOnClickListener { refreshGraphInTimeScale("MONTH") }
        scaleYear.setOnClickListener { refreshGraphInTimeScale("YEAR") }

    }

    private fun addHeight(){
        val dialogBuilder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.setRawInputType(Configuration.KEYBOARD_12KEY)
        dialogBuilder.setMessage("Add your new height here")
            .setView(editText)
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id ->
                val height = editText.text.toString()
                //val time = Calendar.getInstance().time.toString()
                val time = System.currentTimeMillis()
                if (height.isEmpty()) {
                    Toast.makeText(this,
                        "Please enter a valid height",
                        Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val ht = HeightData(height.toDouble(), time)
                    val status = heightHelper.insertHeight(ht)
                    Log.d("Height", "$height, $time")
                    if (status > -1) {
                        Toast.makeText(this, "New Height Added", Toast.LENGTH_SHORT).show()
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
        myHeight = heightHelper.getAllHeights()
        var count = myHeight.size
        count = min(count, 5)
        series = LineGraphSeries()
        for (i in count downTo 1) {
            series.appendData(
                DataPoint((6 - i).toDouble(), myHeight[myHeight.lastIndex - i + 1].height),
                true,
                5
            )
        }
        heightGraph.removeAllSeries()
        heightGraph.addSeries(series)
        heightRecyclerView.adapter = HeightAdapter(this, myHeight)
    }

    private fun refreshGraphInTimeScale(option: String) {
        val scale: Int = when (option) {
            "WEEK" -> 7
            "MONTH" -> 30
            else -> 365
        }
        val now = System.currentTimeMillis()
        val startTime = now - scale.toLong() * 1000L * 60L * 60L * 24L
        val filteredHeight = heightHelper.getFilteredHeights(startTime)
        val count = filteredHeight.size
        timeSeries = LineGraphSeries()
        for (i in 0 until count) {
            timeSeries.appendData(
                DataPoint(Date(filteredHeight[i].time), filteredHeight[i].height),
            true,
            100
            )
        }

        val gridLabelRenderer = heightGraph.gridLabelRenderer
        gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
        gridLabelRenderer.numHorizontalLabels = 3

        val viewPort = heightGraph.viewport
        viewPort.isXAxisBoundsManual = true
        viewPort.setMinX(startTime.toDouble())
        viewPort.setMaxX(now.toDouble())
        heightGraph.removeAllSeries()
        heightGraph.addSeries(timeSeries)

    }

    companion object {
        private const val TAG = "HeightDetailActivity"
    }

}
