package com.mapbox.mapboxandroiddemo.examples.labs

import android.graphics.PointF
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_finger_drag_draw.*


class FingerDragDrawActivity : AppCompatActivity(), View.OnTouchListener {

    companion object {
        private const val lineLayerSourceId = "source_draggable"
        private const val lineLayerId = "layer_draggable"
        private const val markerImageId = "marker_icon_draggable"
    }

    private val actionBarHeight: Int by lazy {
        supportActionBar?.height ?: 0
    }

    // View property is required for activity sanity tests
    // we perform reflection on this requires using findViewById
    private lateinit var mapboxMap: MapboxMap
    private val TAG = "FingerDragDraw"
    private var featureCollection: FeatureCollection? = null
    private var lineLayerSource: GeoJsonSource? = null
    //    private var draggableSymbolsManager: DraggableSymbolsManager? = null
    private var mapMovingEnabled: Boolean? = null
    private var pointList: MutableList<Point>? = null
    private var newPointList: MutableList<Point>? = null
    private var drawingFinished: Boolean? = null
    private var previousPoint: Point? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        setContentView(R.layout.activity_finger_drag_draw)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.OUTDOORS) {

                // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
                this.mapboxMap = mapboxMap

                mapMovingEnabled = false

                // Setting up markers icon, lineLayerSource and layer

                lineLayerSource = GeoJsonSource(lineLayerSourceId)

                it.addSource(lineLayerSource!!)

                it.addLayer(LineLayer(lineLayerId, lineLayerSourceId).withProperties(
                        PropertyFactory.iconImage(markerImageId),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true)))

                mapView.setOnTouchListener(this@FingerDragDrawActivity)


                Toast.makeText(this, this.getString(R.string.draw_instruction), Toast.LENGTH_LONG).show()

                lock_map_movement_toggle_fab.setOnClickListener {


                }
            }
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        Log.d(TAG, "touchPoint motionEvent.x = " + motionEvent.x)
        Log.d(TAG, "touchPoint motionEvent.y = " + motionEvent.y)

        if (!mapMovingEnabled!!) {
            val latLngTouchCoordinate = mapboxMap.projection.fromScreenLocation(PointF(motionEvent.x, motionEvent.y))
            Log.d(TAG, "latLngTouchCoordinate  = " + latLngTouchCoordinate)

            val touchPoint = Point.fromLngLat(latLngTouchCoordinate.longitude, latLngTouchCoordinate.latitude)

            Log.d(TAG, "touchPoint lat = " + touchPoint.latitude())

            Log.d(TAG, "touchPoint long = " + touchPoint.longitude())
            Log.d(TAG, "touchPoint motionEvent.x = " + motionEvent.x)
            Log.d(TAG, "touchPoint motionEvent.y = " + motionEvent.y)

            when (motionEvent.action) {

                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "ACTION_DOWN")
                    Log.d(TAG, "ACTION_DOWN touchPoint = " + touchPoint)

                    pointList?.add(touchPoint)
                    Log.d(TAG, "ACTION_MOVE pointList size = " + pointList?.size)

                    val newLineStringToDraw = LineString.fromLngLats(pointList!!)

                    val clickLocationSource = mapboxMap.style?.getSourceAs<GeoJsonSource>(lineLayerSourceId)

                    clickLocationSource?.setGeoJson(newLineStringToDraw)

                }

                MotionEvent.ACTION_MOVE -> if (drawingFinished!! && !mapMovingEnabled!!) {
                    Log.d(TAG, "ACTION_MOVE")

                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "ACTION_UP")

//                    Log.d(TAG, "ACTION_UP pointList size = " + pointList?.size)
//                    drawingFinished = false
                }

            }

            /* pointList?.add(touchPoint)

             val newLineStringToDraw = LineString.fromLngLats(pointList!!)

             val clickLocationSource = mapboxMap.style?.getSourceAs<GeoJsonSource>(lineLayerSourceId)

             clickLocationSource?.setGeoJson(newLineStringToDraw)
*/
//             mapView.setOnTouchListener(this@FingerDragDrawActivity)
        }

        return false
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            mapView.onSaveInstanceState(it)
        }
    }
}
