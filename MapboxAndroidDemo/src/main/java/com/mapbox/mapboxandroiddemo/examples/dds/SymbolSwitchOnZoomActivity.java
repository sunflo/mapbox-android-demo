package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Use the SymbolLayer's setMinZoom and setMaxZoom methods to create the effect of SymbolLayer icons switching
 * based on the map camera's zoom level.
 */
public class SymbolSwitchOnZoomActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final float ZOOM_LEVEL_FOR_SWITCH = 12;
  private static final String BLUE_PERSON_ICON_ID = "blue-car-icon-marker-icon-id";
  private static final String BLUE_PIN_ICON_ID = "blue-marker-icon-marker-icon-id";
  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_zoom_based_icon_switch);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    initLayerIcons();
    addDataToMap();
    Toast.makeText(this, R.string.zoom_map_in_and_out_icon_switch_instruction,
      Toast.LENGTH_SHORT).show();
  }

  /**
   * Add images to the map so that the SymbolLayers can reference the images.
   */
  private void initLayerIcons() {
    Bitmap bluePersonIcon = BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.ic_person));

    Bitmap bluePinIcon = BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.blue_marker));

    mapboxMap.addImage(BLUE_PERSON_ICON_ID, bluePersonIcon);
    mapboxMap.addImage(BLUE_PIN_ICON_ID, bluePinIcon);
  }

  /**
   * Add the GeoJsonSource and SymbolLayers to the map.
   */
  private void addDataToMap() {
    // Add a new source from the GeoJSON data
    mapboxMap.addSource(
      new GeoJsonSource("source-id",
        FeatureCollection.fromFeatures(new Feature[] {
          Feature.fromGeometry(Point.fromLngLat(
            9.205394983291626,
            45.47661043757903)),
          Feature.fromGeometry(Point.fromLngLat(
            9.223880767822266,
            45.47623240235297)),
          Feature.fromGeometry(Point.fromLngLat(
            9.15530204772949,
            45.4706650227671)),
          Feature.fromGeometry(Point.fromLngLat(
            9.153714179992676,
            45.48625229963004)),
          Feature.fromGeometry(Point.fromLngLat(
            9.158306121826172,
            45.482731998239636)),
          Feature.fromGeometry(Point.fromLngLat(
            9.188523888587952,
            45.4923746929562)),
          Feature.fromGeometry(Point.fromLngLat(
            9.20929491519928,
            45.45314676076135)),
          Feature.fromGeometry(Point.fromLngLat(
            9.177778959274292,
            45.45569808340158))
        })
      )
    );
    mapboxMap.addLayer(createLayer(BLUE_PERSON_ICON_ID, false));
    mapboxMap.addLayer(createLayer(BLUE_PIN_ICON_ID, true));
  }

  /**
   * This method creates a SymbolLayer which is ready to be added to the map.
   *
   * @param iconId The unique id of the image which the SymbolLayer should use for its symbol icon
   * @param setMin Whether or not a minimum or maximum zoom level should be set on the SymbolLayer.
   * @return a completed SymbolLayer which is ready to add to the map.
   */
  private SymbolLayer createLayer(String iconId, boolean setMin) {
    SymbolLayer singleLayer = new SymbolLayer(iconId + "symbol-layer-id", "source-id");
    singleLayer.setProperties(
      iconImage(iconId),
      iconIgnorePlacement(true),
      iconAllowOverlap(true));
    if (setMin) {
      singleLayer.setMinZoom(ZOOM_LEVEL_FOR_SWITCH);
    } else {
      singleLayer.setMaxZoom(ZOOM_LEVEL_FOR_SWITCH);
    }
    return singleLayer;
  }

  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    mapView.onStop();
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
