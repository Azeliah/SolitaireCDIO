package com.cdio.solitaire.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.*
import android.view.Surface.ROTATION_90
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cdio.solitaire.R
import com.cdio.solitaire.databinding.FragmentCameraBinding
import com.cdio.solitaire.imageanalysis.CardDataCreationModel
import com.cdio.solitaire.imageanalysis.SolitaireAnalysisModel
import com.cdio.solitaire.ml.RankModel
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/** Helper type alias used for analysis use case callbacks */
typealias CardAnalyzerListener = () -> Unit

class CameraFragment : Fragment(), SensorEventListener {

    private lateinit var thisContext: Context

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

    private lateinit var broadcastManager: LocalBroadcastManager

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var statusMessage: TextView? = null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    // For showing rotation on camera fragment
    private lateinit var rotationTextView: TextView
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Exit fullscreen mode
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            it.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        // Stop listening to rotation changes
        sensorManager.unregisterListener(this)

        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        thisContext = container?.context!!
        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Go into fullscreen mode
        activity?.let {
            it.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            (it as AppCompatActivity).supportActionBar?.hide()
        }

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        broadcastManager = LocalBroadcastManager.getInstance(view.context)

        rotationTextView = view.findViewById(R.id.rotation_indicator)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)

        view.findViewById<Button?>(R.id.back_button).setOnClickListener {
            requireActivity().onBackPressed()
        }

        statusMessage = view.findViewById(R.id.status_message)
        statusMessage?.text = "Test message"

        // Set up the camera and its use cases
        setUpCamera()

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Rebind the camera with the updated display metrics
        bindCameraUseCases()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val xAxis = it.values[0]
            val yAxis = it.values[1]
            val zAxis = it.values[2]

            if (yAxis > 0.2) {
                statusMessage?.text = "Tilt phone more up"
            } else if (yAxis < -0.2) {
                statusMessage?.text = "Tilt phone more down"
            } else {
                statusMessage?.text = "Hold phone still"
            }

            rotationTextView.text = getString(R.string.rotation_indicator_text, xAxis, yAxis, zAxis)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Accuracy changed to $accuracy")
        // TODO: Should we do something here?
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(ROTATION_90)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request a specific resolution
            .setTargetResolution(Size(4032,1816))
            .setTargetRotation(ROTATION_90)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, CardAnalyzer(thisContext) {
                    Log.d(TAG, "CardAnalyzerListener called")
                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    /**
     * Our custom image analysis class.
     *
     * Analyzes the image for valid cards and tries to construct information about the current deck.
     * The deck is returned to any potential listeners added in constructor or with onFrameAnalyzed.
     */
    private class CardAnalyzer(context: Context, listener: CardAnalyzerListener? = null) : ImageAnalysis.Analyzer {
        private val context = context
        private val listeners =
            ArrayList<CardAnalyzerListener>().apply { listener?.let { add(it) } }

        /**
         * Used to add listeners that will be called with each image analyzed
         */
        fun onFrameAnalyzed(listener: CardAnalyzerListener) = listeners.add(listener)

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(image: ImageProxy) {
            System.loadLibrary("opencv_java4")

            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Convert Camerax ImageProxy to a Mat object and extract card icons in the image
            val javaImage: Image? = image.image
            val bitmap = javaImage?.toBitmap()
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            val solitaireAnalysis = SolitaireAnalysisModel()
            val bitmapArr = solitaireAnalysis.extractSolitaire(mat)

            // Todo add code for ML and GameMoves

            if (bitmapArr != null) {
                Log.d(TAG, "Success. A complete solitaire game was found!")

                // Todo remove when no longer needed or make debug only
                /*
                val date = System.currentTimeMillis().toString()
                for (i in bitmapArr.indices) {
                    saveToStorage(date, i , bitmapArr[i])
                }
                 */
            } else {
                Log.e(TAG, "Failure. No complete solitaire game was found!")
            }
            mat.release()

            image.close()
        }

        /**
         * Helper function to save the output from CardExtraction to storage.
         *
         * <p> This is supposed to be used for debugging only; the bitmap should be passed
         * to our ML model in the future.
         */
        fun saveToStorage(timeStamp: String, index: Int, bitmapImage: Bitmap) {
            val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/solitare_" + timeStamp)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val file = File(directory, timeStamp + "_" + index.toString() + ".jpeg")
            if (!file.exists()) {
                file.createNewFile();
            }
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.fd.sync()
                fos.flush()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Extension method to convert Image-object to bitmap
         * <p> Source: https://stackoverflow.com/questions/56772967/converting-imageproxy-to-bitmap
         */
        fun Image.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer
            val vuBuffer = planes[2].buffer
            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()
            val nv21 = ByteArray(ySize + vuSize)
            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}
