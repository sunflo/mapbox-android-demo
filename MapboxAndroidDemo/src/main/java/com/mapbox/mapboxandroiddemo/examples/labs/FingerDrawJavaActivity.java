package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.api.matching.v5.MapboxMapMatching;
import com.mapbox.api.matching.v5.models.MapMatchingMatching;
import com.mapbox.api.matching.v5.models.MapMatchingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_WALKING;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class FingerDrawJavaActivity extends AppCompatActivity implements View.OnTouchListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private String TAG = "Mbgl-FingerDrawJavaActivity";
  //  private boolean mapMovingEnabled = true;
  private List<Point> freehandDrawLineLayerPointList;
  private List<Feature> startAndEndSymbolLayerFeatureList;
  private boolean drawingFinished;
  private boolean drawingStarted = false;
  private final String freehandDrawLineLayerSourceId = "freehandDrawLineLayerSourceId";
  private final String markerSymbolLayerSourceId = "markerSymbolLayerSourceId";
  private final String mapMatchedLineLayerSourceId = "mapMatchedLineLayerSourceId";
  private final String freehandDrawLineLayerId = "freehandDrawLineLayerId";
  private final String mapMatchedLineLayerId = "mapMatchedLineLayerId";
  private final String markerSymbolLayerId = "markerSymbolLayerId";
  private final String markerImageId = "markerImageId";
  private final float LINE_WIDTH = 8f;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_finger_drag_draw);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            FingerDrawJavaActivity.this.mapboxMap = mapboxMap;

            freehandDrawLineLayerPointList = new ArrayList<>();
            startAndEndSymbolLayerFeatureList = new ArrayList<>();

            mapboxMap.getUiSettings().setAllGesturesEnabled(false);

            style.addImage(markerImageId, BitmapFactory.decodeResource(
              FingerDrawJavaActivity.this.getResources(), R.drawable.red_marker));

            style.addSource(new GeoJsonSource(freehandDrawLineLayerSourceId));
            style.addSource(new GeoJsonSource(markerSymbolLayerSourceId));
            style.addSource(new GeoJsonSource(mapMatchedLineLayerSourceId));

            style.addLayer(new SymbolLayer(markerSymbolLayerId, markerSymbolLayerSourceId).withProperties(
              iconImage(markerImageId),
              iconAllowOverlap(true),
              iconOffset(new Float[] {0f, -8f}),
              iconIgnorePlacement(true))
            );

            style.addLayerBelow(new LineLayer(freehandDrawLineLayerId, freehandDrawLineLayerSourceId).withProperties(
              lineWidth(LINE_WIDTH),
              lineJoin(LINE_JOIN_ROUND),
              lineOpacity(1f),
              lineColor(Color.parseColor("#a0861c"))), markerSymbolLayerId
            );

            style.addLayer(new LineLayer(mapMatchedLineLayerId, mapMatchedLineLayerSourceId).withProperties(
              lineWidth(LINE_WIDTH),
              lineJoin(LINE_JOIN_ROUND),
              lineOpacity(1f),
              lineColor(Color.parseColor("#275ff9")))
            );

            mapView.setOnTouchListener(FingerDrawJavaActivity.this);


           /* final FloatingActionButton lockMapMovementFab = findViewById(R.id.lock_map_movement_toggle_fab);
            lockMapMovementFab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                Log.d(TAG, "onClick: mapMovingEnabled before button action = " + mapMovingEnabled);

                if (mapMovingEnabled) {
                  lockMapMovementFab.setImageDrawable(
                    ContextCompat.getDrawable(FingerDrawJavaActivity.this,
                      R.drawable.ic_lock));
                  Toast.makeText(FingerDrawJavaActivity.this,
                    getString(R.string.map_locked), Toast.LENGTH_SHORT).show();
                  mapMovingEnabled = false;
                } else {
                  lockMapMovementFab.setImageDrawable(
                    ContextCompat.getDrawable(FingerDrawJavaActivity.this,
                      R.drawable.ic_lock_open));
                  Toast.makeText(FingerDrawJavaActivity.this,
                    getString(R.string.map_unlocked), Toast.LENGTH_SHORT).show();
                  mapMovingEnabled = true;
                }
                mapboxMap.getUiSettings().setAllGesturesEnabled(mapMovingEnabled);
                Log.d(TAG, "onClick: mapMovingEnabled after button action = " + mapMovingEnabled);

              }
            });*/
          }
        });
      }
    });
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    LatLng latLngTouchCoordinate = mapboxMap.getProjection().fromScreenLocation(new PointF(motionEvent.getX(), motionEvent.getY()));

    Point touchPoint = Point.fromLngLat(latLngTouchCoordinate.getLongitude(), latLngTouchCoordinate.getLatitude());

    addSymbolLayerMarkerIcon(touchPoint);
    drawingStarted = true;

    if (freehandDrawLineLayerPointList != null) {
      freehandDrawLineLayerPointList.add(touchPoint);

      GeoJsonSource clickLocationSource = mapboxMap.getStyle().getSourceAs(freehandDrawLineLayerSourceId);
      clickLocationSource.setGeoJson(LineString.fromLngLats(freehandDrawLineLayerPointList));

      switch (motionEvent.getAction()) {

        case MotionEvent.ACTION_UP:
          drawingStarted = false;
          addSymbolLayerMarkerIcon(touchPoint);
            /*
          List<Point> matchPointList = new ArrayList();

          for (int x = 0; x < 100; x++) {
            matchPointList.add(freehandDrawLineLayerPointList.listIterator().get(
              freehandDrawLineLayerPointList.size() / x
            ));
          }

        if (freehandDrawLineLayerPointList.size() > 0) {
            requestMapMatched(matchPointList);
          }*/
          break;
      }
    }

   /*
    switch (motionEvent.getAction()) {
      case MotionEvent.ACTION_DOWN:
        Log.d(TAG, "ACTION_DOWN freehandDrawLineLayerPointList.size() before = " + freehandDrawLineLayerPointList.size());
        freehandDrawLineLayerPointList.add(touchPoint);
        Log.d(TAG, "ACTION_DOWN freehandDrawLineLayerPointList.size() after = " + freehandDrawLineLayerPointList.size());
        break;

      case MotionEvent.ACTION_MOVE:
        Log.d(TAG, "ACTION_MOVE freehandDrawLineLayerPointList.size() size before = " + freehandDrawLineLayerPointList.size());
        freehandDrawLineLayerPointList.add(touchPoint);
        Log.d(TAG, "ACTION_MOVE freehandDrawLineLayerPointList.size() size after = " + freehandDrawLineLayerPointList.size());
        break;
      case MotionEvent.ACTION_UP:

        Log.d(TAG, "ACTION_UP freehandDrawLineLayerPointList size before = " + freehandDrawLineLayerPointList.size());
      *//*  LineString newLineStringToDraw = LineString.fromLngLats(freehandDrawLineLayerPointList);
        GeoJsonSource clickLocationSource = mapboxMap.getStyle().getSourceAs(freehandDrawLineLayerSourceId);
        clickLocationSource.setGeoJson(newLineStringToDraw);
*//*
        break;
    }*/

            /* freehandDrawLineLayerPointList?.add(touchPoint)

             val newLineStringToDraw = LineString.fromLngLats(freehandDrawLineLayerPointList!!)

             val clickLocationSource = mapboxMap.style?.getSourceAs<GeoJsonSource>(freehandDrawLineLayerSourceId)

             clickLocationSource?.setGeoJson(newLineStringToDraw)
*//*
//             mapView.setOnTouchListener(FingerDrawJavaActivity.this)
    }
*/
    return true;
  }

  private void addSymbolLayerMarkerIcon(Point touchPoint) {
    if (!drawingStarted) {
      startAndEndSymbolLayerFeatureList.add(Feature.fromGeometry(touchPoint));
      GeoJsonSource markerSource = mapboxMap.getStyle().getSourceAs(markerSymbolLayerSourceId);
      markerSource.setGeoJson(FeatureCollection.fromFeatures(startAndEndSymbolLayerFeatureList));
      drawingStarted = true;
    }
  }

  private void requestMapMatched(List<Point> points) {

    try {
      // Setup the request using a client.
      MapboxMapMatching client = MapboxMapMatching.builder()
        .accessToken(Objects.requireNonNull(getString(R.string.access_token)))
        .profile(PROFILE_WALKING)
        .coordinates(points)
        .build();

      // Execute the API call and handle the response.
      client.enqueueCall(new Callback<MapMatchingResponse>() {
        @Override
        public void onResponse(@NonNull Call<MapMatchingResponse> call,
                               @NonNull Response<MapMatchingResponse> response) {
          if (response.code() == 200) {
            drawMapMatched(Objects.requireNonNull(response.body()).matchings());
          } else {
            // If the response code does not response "OK" an error has occurred.
            Log.e(TAG, "MapboxMapMatching failed with " + response.code());
          }
        }

        @Override
        public void onFailure(Call<MapMatchingResponse> call, Throwable throwable) {
          Log.e(TAG, "MapboxMapMatching error: ", throwable);
        }
      });
    } catch (ServicesException servicesException) {
      Log.e(TAG, "MapboxMapMatching error: ", servicesException);
    }
  }

  private void drawMapMatched(@NonNull List<MapMatchingMatching> matchings) {
    Style style = mapboxMap.getStyle();
    if (style != null && !matchings.isEmpty()) {
      GeoJsonSource matchLineSource = style.getSourceAs(mapMatchedLineLayerSourceId);
      matchLineSource.setGeoJson(Feature.fromGeometry(LineString.fromPolyline(
        Objects.requireNonNull(matchings.get(0).geometry()), PRECISION_6)));
    }
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
