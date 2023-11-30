package com.example.mapdrive;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    private PermissionsManager permissionsManager;

    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "DirectionsActivity";
    private static Point currentPoint, destinationPoint;

    private String geoJsonSourceLayerId = "GeoJsonSourceLayerId";
    private String symbolIconId = "SymbolIconId";

    private static final int REQUEST_CODE_AUTOCOMPLETE = 7171;

    private static final String DirectionsProfileToggleActivity = "DirectionsProfileToggleActivity";

    Button navigate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap, style);
                // Enable the traffic view by default
                trafficPlugin.setVisibility(true);

                enableLocationComponent(style);
                addDestinationSymbolLayer(style);

                mapboxMap.addOnMapClickListener(MainActivity.this);

                navigate = findViewById(R.id.navigateBtn);
                navigate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean simulateRoute = true;

                        if (currentRoute == null) {
                            return; // Route has not been set, so we ignore the button press
                        }
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(simulateRoute)
                                .build();
                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MainActivity.this, options);

//                        PendingIntent pendingIntent = null;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                            NavigationLauncher.startNavigation(MainActivity.this, options);
//                            pendingIntent.isImmutable();
//                        }else {
//                            pendingIntent = PendingIntent.getActivity(MainActivity.this,
//                                    0, new Intent(MainActivity.this, NavigationLauncher.class).addFlags(
//                                            Intent.FLAG_ACTIVITY_SINGLE_TOP),
//                                    PendingIntent.FLAG_UPDATE_CURRENT);
//                        }

                    }
                });

                setSource(style);
                setLayer(style);
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(Style loadedMapStyle) {
        /*Check if permissions are enabled and if not requested*/
        if (PermissionsManager.areLocationPermissionsGranted(this)){
            //Activate the MapboxMap LocationComponent to show user location
            //Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);

            //set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            cameraPosition();
        }else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> list) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted){
            enableLocationComponent(mapboxMap.getStyle());
        }else{
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void cameraPosition(){
        assert locationComponent.getLastKnownLocation() != null;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()))
                .zoom(16)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE){
            /*Retrieve selected location's CarmenFeature*/
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            /*Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above
             * Then retrieve and update the source designated for showing a selected location's symbol layer icon*/
            if (mapboxMap != null){
                Style style = mapboxMap.getStyle();
                if (style != null){
                    GeoJsonSource source = style.getSourceAs(geoJsonSourceLayerId);
                    if (source != null){
                        source.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    /*Move map camera to selected location*/
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                    ((Point) selectedCarmenFeature.geometry()).longitude() )).zoom(16)
                            .build()), 4000);
                }
            }
        }
    }

    private void addDestinationSymbolLayer(Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id", BitmapFactory.decodeResource(this.getResources(),
                com.mapbox.mapboxsdk.R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true));

        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

        assert locationComponent.getLastKnownLocation() != null;
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");

        if (source != null){
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);

        return true;
    }

    private void setLayer(Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geoJsonSourceLayerId).withProperties(iconImage(symbolIconId),
                iconOffset(new Float[]{0f, -8f})));
    }

    private void setSource(Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geoJsonSourceLayerId));
    }



    private void getRoute(Point origin, Point destination) {
        assert Mapbox.getAccessToken() != null;
        NavigationRoute.builder(this).accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        Timber.tag(TAG).d("Response code : %s", response.code());
                        if (response.body() == null){
                            Timber.tag(TAG).d("No routes found, make sure you set the right user and access token.");
                            return;
                        }else if (response.body().routes().size() < 1){
                            Timber.tag(TAG).e("No routes found.");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        //Draw the route on the map
                        if (navigationMapRoute != null){
                            navigationMapRoute.removeRoute();
                        }else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, com.mapbox.services.android.navigation.ui.v5.R.style.NavigationMapRoute);
                        }

                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Timber.tag(TAG).e("Error: %s", t.getMessage());
                    }
                });
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
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}