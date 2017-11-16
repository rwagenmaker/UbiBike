package pt.ulisboa.tecnico.cmov.ubibike.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Trajectory;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;

public class ShowRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private PolylineOptions pathPoints = new PolylineOptions();
    private int routePosition;
    private int distanceTravelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        Toolbar toolbar = (Toolbar) findViewById(R.id.route_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String position = intent.getStringExtra(RoutesActivity.ROUTE_POSITION);
        routePosition = Integer.parseInt(position);
        distanceTravelled = (int) DataHolder.getInstance().getTrajectoriesUpdated().get(routePosition).getDistance();
        toolbar.setTitle("Route with " + distanceTravelled + "m");

        pathPoints.width(10).color(Color.parseColor("#3498db")).geodesic(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(ShowRouteActivity.this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(ShowRouteActivity.this).addApi(AppIndex.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final Trajectory trajectory = DataHolder.getInstance().getTrajectoriesUpdated().get(routePosition);
        final int trajectoryLength = trajectory.getTrajectoryLat().size();

        for (int i = 0; i < trajectory.getTrajectoryLat().size(); i++)
            pathPoints.add(new LatLng(Double.parseDouble(trajectory.getTrajectoryLong().get(i)), Double.parseDouble(trajectory.getTrajectoryLat().get(i))));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Double.parseDouble(trajectory.getTrajectoryLong().get(trajectoryLength/2)), Double.parseDouble(trajectory.getTrajectoryLat().get(trajectoryLength/2))), 14));

                mMap.addPolyline(pathPoints);
            }
        });
    }
}
