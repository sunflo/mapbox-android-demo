package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfJoins;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Use the Android system {@link android.view.View.OnTouchListener} to draw
 * an area and perform a search for features in that area.
 */
public class FingerDrawJavaActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private FeatureCollection searchPointFeatureCollection;
  private GeoJsonSource drawSource;
  private List<Point> freehandDrawLineLayerPointList;
  private List<Feature> startAndEndSymbolLayerFeatureList;
  private List<Feature> searchDataSymbolLayerFeatureList;
  private List<List<Point>> polygonList;
  private boolean drawingStarted = false;
  private final String searchDataSymbolLayerrSourceId = "searchDataSymbolLayerrSourceId";
  private final String freehandDrawLineLayerSourceId = "freehandDrawLineLayerSourceId";
  private final String markerSymbolLayerSourceId = "markerSymbolLayerSourceId";
  private final String mapMatchedLineLayerSourceId = "mapMatchedLineLayerSourceId";
  private final String freehandDrawFillLayerSourceId = "freehandDrawFillLayerSourceId";
  private final String freehandDrawLineLayerId = "freehandDrawLineLayerId";
  private final String freehandDrawFillLayerId = "freehandDrawFillLayerId";
  private final String mapMatchedLineLayerId = "mapMatchedLineLayerId";
  private final String markerSymbolLayerId = "markerSymbolLayerId";
  private final String searchDataSymbolLayerId = "searchDataSymbolLayerId";
  private final String touchPointMarkerId = "touchPointMarkerId";
  private final String searchDataMarkerId = "searchDataMarkerId";
  private final float LINE_WIDTH = 5f;

  /**
   * Customize search UI with these booleans
   */
  private final boolean fillSearchAreaWithPolygonWhileDrawing = true;
  private final boolean fillSearchAreaWithPolygon = true;
  private final boolean drawFirstLocationMarkerWhenDrawingIsDone = false;
  private final boolean drawSecondLocationMarkerWhenDrawingIsDone = false;
  private final boolean closePolygonSearchArea = true;
  private final boolean showSearchDataLocations = true;

  private View.OnTouchListener customOnTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

      LatLng latLngTouchCoordinate = mapboxMap.getProjection().fromScreenLocation(new PointF(motionEvent.getX(), motionEvent.getY()));

      Point touchPoint = Point.fromLngLat(latLngTouchCoordinate.getLongitude(), latLngTouchCoordinate.getLatitude());

      if (drawFirstLocationMarkerWhenDrawingIsDone) {
        // Add a SymbolLayer icon where the drawing starts
        addTouchPointSymbolLayerMarkerIcon(touchPoint);
      }

      if (freehandDrawLineLayerPointList != null) {

        // Draw the line as drawing on the map happens
        freehandDrawLineLayerPointList.add(touchPoint);
        drawSource = mapboxMap.getStyle().getSourceAs(freehandDrawLineLayerSourceId);
        drawSource.setGeoJson(LineString.fromLngLats(freehandDrawLineLayerPointList));

        if (fillSearchAreaWithPolygonWhileDrawing) {
          drawSearchFillArea();
        }

        // Take certain actions when the drawing is done
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
          if (closePolygonSearchArea) {
            freehandDrawLineLayerPointList.add(freehandDrawLineLayerPointList.get(0));
          }

          // Add a SymbolLayer icon where the drawing ends
          if (drawSecondLocationMarkerWhenDrawingIsDone) {
            drawingStarted = false;
            addTouchPointSymbolLayerMarkerIcon(touchPoint);
          }

          if (!fillSearchAreaWithPolygonWhileDrawing && fillSearchAreaWithPolygon) {
            drawSearchFillArea();
          }

          if (closePolygonSearchArea) {
            FeatureCollection pointsInSearchAreaFeatureCollection =
                TurfJoins.pointsWithinPolygon(searchPointFeatureCollection,
                    FeatureCollection.fromFeature(Feature.fromGeometry(
                        Polygon.fromLngLats(polygonList))));
            if (VISIBLE.equals(mapboxMap.getStyle().getLayer(
                searchDataSymbolLayerId).getVisibility().getValue())) {
              Toast.makeText(FingerDrawJavaActivity.this, String.format(
                  getString(R.string.search_result_size),
                  pointsInSearchAreaFeatureCollection.features().size()), Toast.LENGTH_SHORT).show();
            }
          }

          enableMapMovement();

        }
      }
      return true;
    }
  };

  private void drawSearchFillArea() {
    // Create and show a FillLayer polygon where the search area is
    GeoJsonSource fillSource = mapboxMap.getStyle().getSourceAs(freehandDrawFillLayerSourceId);
    polygonList = new ArrayList<>();
    polygonList.add(freehandDrawLineLayerPointList);
    fillSource.setGeoJson(Polygon.fromLngLats(polygonList));
  }

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

            if (showSearchDataLocations) {
              new LoadGeoJson(FingerDrawJavaActivity.this).execute();
            } else {
              setUpExample(null);
            }

            findViewById(R.id.clear_map_for_new_draw_fab)
                .setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {

                    freehandDrawLineLayerPointList = new ArrayList<>();
                    polygonList = new ArrayList<>();
                    freehandDrawLineLayerPointList = new ArrayList<>();

                    drawSource = mapboxMap.getStyle().getSourceAs(freehandDrawLineLayerSourceId);
                    drawSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));


                    GeoJsonSource fillSource = mapboxMap.getStyle().getSourceAs(freehandDrawFillLayerSourceId);
                    fillSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));

                    mapboxMap.easeCamera(CameraUpdateFactory
                        .newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(35.087497, -106.651261))
                            .zoom(11.679132)
                            .build()));

                    enabledMapDrawing();
                  }
                });
          }
        });
      }
    });
  }

  private void enableMapMovement() {
    mapView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
      }
    });
    mapboxMap.getUiSettings().setAllGesturesEnabled(true);
  }

  private void enabledMapDrawing() {
    mapView.setOnTouchListener(customOnTouchListener);
    mapboxMap.getUiSettings().setAllGesturesEnabled(false);
  }

  private void addTouchPointSymbolLayerMarkerIcon(Point touchPoint) {
    if (!drawingStarted) {
      startAndEndSymbolLayerFeatureList.add(Feature.fromGeometry(touchPoint));
      GeoJsonSource markerSource = mapboxMap.getStyle().getSourceAs(markerSymbolLayerSourceId);
      markerSource.setGeoJson(FeatureCollection.fromFeatures(startAndEndSymbolLayerFeatureList));
      drawingStarted = true;
    }
  }

  private void setUpExample(FeatureCollection searchDatafeatureCollection) {

    searchPointFeatureCollection = searchDatafeatureCollection;

    Style style = mapboxMap.getStyle();

    if (style != null) {
      freehandDrawLineLayerPointList = new ArrayList<>();
      startAndEndSymbolLayerFeatureList = new ArrayList<>();
      searchDataSymbolLayerFeatureList = new ArrayList<>();

      style.addImage(touchPointMarkerId, BitmapFactory.decodeResource(
          FingerDrawJavaActivity.this.getResources(), R.drawable.red_marker));

      style.addImage(searchDataMarkerId, BitmapFactory.decodeResource(
          FingerDrawJavaActivity.this.getResources(), R.drawable.blue_marker_view));

      style.addSource(new GeoJsonSource(searchDataSymbolLayerrSourceId, searchDatafeatureCollection));
      style.addSource(new GeoJsonSource(freehandDrawLineLayerSourceId));
      style.addSource(new GeoJsonSource(markerSymbolLayerSourceId));
      style.addSource(new GeoJsonSource(mapMatchedLineLayerSourceId));
      style.addSource(new GeoJsonSource(freehandDrawFillLayerSourceId));

      style.addLayer(new SymbolLayer(markerSymbolLayerId, markerSymbolLayerSourceId).withProperties(
          iconImage(touchPointMarkerId),
          iconAllowOverlap(true),
          iconOffset(new Float[]{0f, -8f}),
          iconIgnorePlacement(true))
      );

      style.addLayer(new SymbolLayer(searchDataSymbolLayerId, searchDataSymbolLayerrSourceId).withProperties(
          iconImage(searchDataMarkerId),
          iconAllowOverlap(true),
          iconOffset(new Float[]{0f, -8f}),
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

      style.addLayerBelow(new FillLayer(freehandDrawFillLayerId, freehandDrawFillLayerSourceId).withProperties(
          fillColor(Color.RED),
          fillOpacity(.4f)), freehandDrawLineLayerId
      );

      enabledMapDrawing();

      if (showSearchDataLocations) {
        findViewById(R.id.show_search_data_points_fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Layer dataLayer = mapboxMap.getStyle().getLayer(searchDataSymbolLayerId);
            dataLayer.setProperties(
                VISIBLE.equals(dataLayer.getVisibility().getValue()) ? visibility(NONE) : visibility(VISIBLE));
          }
        });
      }

      Toast.makeText(FingerDrawJavaActivity.this,
          getString(R.string.draw_instruction), Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Use an AsyncTask to retrieve GeoJSON data from a file in the assets folder.
   */
  private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<FingerDrawJavaActivity> weakReference;

    LoadGeoJson(FingerDrawJavaActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        FingerDrawJavaActivity activity = weakReference.get();
        if (activity != null) {
          InputStream inputStream = activity.getAssets().open("albuquerque_locations.geojson");
          return FeatureCollection.fromJson(convertStreamToString(inputStream));
        }
      } catch (Exception exception) {
        Timber.e("Exception Loading GeoJSON: %s", exception.toString());
      }
      return null;
    }

    static String convertStreamToString(InputStream is) {
      Scanner scanner = new Scanner(is).useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }

    @Override
    protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);
      FingerDrawJavaActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.setUpExample(featureCollection);
      }
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
