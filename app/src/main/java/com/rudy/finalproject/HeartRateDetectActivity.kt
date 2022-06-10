package com.rudy.finalproject

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color.GREEN
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.rudy.finalproject.database.HeartRateData
import com.rudy.finalproject.database.HeightData
import com.rudy.finalproject.database.SQLiteHelper.HeartRateHelper
import java.util.concurrent.atomic.AtomicBoolean


class HeartRateDetectActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HeartRateDetectActivity"

        private var currentType = TYPE.GREEN
        @JvmStatic
        fun getCurrent(): TYPE {
            return currentType
        }
    }
    enum class TYPE { GREEN, RED }
    private var processing = AtomicBoolean(false)
    private lateinit var preview: SurfaceView
    private lateinit var previewHolder: SurfaceHolder
    private lateinit var camera: Camera
    private lateinit var image: View
    private lateinit var text: TextView
    private lateinit var wakeLock: WakeLock

    private var averageIndex = 0
    private val averageArraySize = 4
    private val averageArray = IntArray(averageArraySize)



    private var beatsIndex = 0
    private val beatsArraySize = 3
    private var beatsArray = IntArray(beatsArraySize)
    private var beats = 0.0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_detect)

        preview = findViewById<View>(R.id.preview) as SurfaceView
        previewHolder = preview.holder
        previewHolder.addCallback(surfaceCallback)
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        image = findViewById(R.id.image)
        text = findViewById(R.id.text)

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Final Project:DoNotDimScreen")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA)
            val permissions: MutableList<String> = ArrayList()
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA)
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toTypedArray(), 111)
            }
        }

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            111 -> {
                var i = 0
                while (i < permissions.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        println("Permissions --> " + "Permission Granted: " + permissions[i])
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        println("Permissions --> " + "Permission Denied: " + permissions[i])
                    }
                    i++
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
        Log.d(TAG, "1")
        camera = Camera.open()
        Log.d(TAG, "2")
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        wakeLock.release()
        camera.setPreviewCallback(null)
        camera.stopPreview()
        camera.release()
    }

    private val previewCallback =
        PreviewCallback { data, cam ->

            /**
             * {@inheritDoc}
             */
            /**
             * {@inheritDoc}
             */
            if (data == null) throw NullPointerException()
            val size = cam.parameters.previewSize ?: throw NullPointerException()
            if (!processing.compareAndSet(
                    false,
                    true
                )
            ) return@PreviewCallback
            val width = size.width
            val height = size.height
            val imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width)
            // Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false)
                return@PreviewCallback
            }
            var averageArrayAvg = 0
            var averageArrayCnt = 0
            for (i in averageArray.indices) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i]
                    averageArrayCnt++
                }
            }
            val rollingAverage = if (averageArrayCnt > 0) averageArrayAvg / averageArrayCnt else 0
            var newType = currentType
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED
                if (newType != currentType) {
                    beats++
                    // Log.d(TAG, "BEAT!! beats="+beats);
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN
            }
            if (averageIndex == averageArraySize) averageIndex = 0
            averageArray[averageIndex] = imgAvg
            averageIndex++

            // Transitioned from one state to another to the same
            if (newType != currentType) {
                currentType = newType
                image.postInvalidate()
            }
            val endTime = System.currentTimeMillis()
            val totalTimeInSecs = (endTime - startTime) / 1000.0
            if (totalTimeInSecs >= 10) {
                val bps = beats / totalTimeInSecs
                val dpm = (bps * 60.0).toInt()
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis()
                    beats = 0.0
                    processing.set(false)
                    return@PreviewCallback
                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);
                if (beatsIndex == beatsArraySize) beatsIndex =
                    0
                beatsArray[beatsIndex] = dpm
                beatsIndex++
                var beatsArrayAvg = 0
                var beatsArrayCnt = 0
                for (i in beatsArray.indices) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i]
                        beatsArrayCnt++
                    }
                }
                val beatsAvg = beatsArrayAvg / beatsArrayCnt
                text.text = beatsAvg.toString()

                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage("Do you want to add this data? ${beatsAvg.toString()} bpm")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                            dialog, id ->
                        val heartRate = beatsAvg
                        val time = System.currentTimeMillis()
                        val heartRateHelper = HeartRateHelper(this)
                        val hrt = HeartRateData(heartRate, time)
                        val status = heartRateHelper.insertHeartRate(hrt)
                        Log.d("HeartRate", "$heartRate, $time")
                        if (status > -1) {
                            Toast.makeText(this, "New Heart Rate Added", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show()
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

                startTime = System.currentTimeMillis()
                beats = 0.0
            }
            processing.set(false)
        }

    private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        /**
         * {@inheritDoc}
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera.setPreviewDisplay(previewHolder)
                camera.setPreviewCallback(previewCallback)
            } catch (t: Throwable) {
                Log.e("PreviewDemo", "Exception in setPreviewDisplay()", t)
            }
        }

        /**
         * {@inheritDoc}
         */
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            val parameters = camera.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            val size = getSmallestPreviewSize(width, height, parameters)
            if (size != null) {
                parameters?.setPreviewSize(size.width, size.height)
                Log.d(
                    TAG,
                    "Using width=" + size.width + " height=" + size.height
                )
            }
            camera.parameters = parameters
            camera.startPreview()
        }

        /**
         * {@inheritDoc}
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // Ignore
        }
    }

    private fun getSmallestPreviewSize(
        width: Int,
        height: Int,
        parameters: Camera.Parameters
    ): Camera.Size? {
        var result: Camera.Size? = null
        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height
                    if (newArea < resultArea) result = size
                }
            }
        }
        return result
    }

}