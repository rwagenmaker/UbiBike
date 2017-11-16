package pt.ulisboa.tecnico.cmov.ubibike.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.domain.BikeStation;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Message;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Trajectory;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Transaction;
import pt.ulisboa.tecnico.cmov.ubibike.domain.User;
import pt.ulisboa.tecnico.cmov.ubibike.network.RequestToServer;
import pt.ulisboa.tecnico.cmov.ubibike.network.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass;
import pt.ulisboa.tecnico.cmov.ubibike.parsers.StationsParser;
import pt.ulisboa.tecnico.cmov.ubibike.parsers.TrajectoriesParser;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;

public class HomeActivity extends GeneralDrawerActivity
        implements OnMapReadyCallback, PeerListListener, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String USER_NAME = DataHolder.getInstance().getUsername();
    private static String ARGUMENTS;
    private static String ARGUMENTS_TO_INFORM;
    private static Map<String, BikeStation> INITIAL_SERVICE_STATE;

    private GoogleMap mMap;
    private PolylineOptions pathPoints = new PolylineOptions();
    private List<Polyline> actualPath = new ArrayList<>();
    private Location previousLocation;
    private Marker previousLocationMarker;
    private Trajectory userTrajectory;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private GoogleApiClient client;
    private Map<String, Marker> bicycleStationMarkers = new HashMap<>();
    private Integer numberOfBikesAvailable;

    private LocationManager lManager;
    private SimWifiP2pManager mManager;
    private Channel mChannel;
    private boolean mBound = false;
    private SimWifiP2pBroadcastReceiver mReceiver;
    private BroadcastReceiver bReceiverTravel;

    public static final String TAG = "msgsender";
    private Messenger mService = null;

    private SimWifiP2pSocketServer mSrvSocket = null;
    private TextView mTextOutput;
    private String string;

    private ServiceConnection getmConnection;

    private String serverAnswer;
    //private SimWifiP2pSocket mCliSocket = null;
    private SimWifiP2pSocket sock = null;
    private String pointsToSend;
    private Timer sendToServerTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SimWifiP2pSocketManager.Init(getApplicationContext());

        FetchBikeStations fetchBikeStations = new FetchBikeStations();
        fetchBikeStations.execute("Get Stations_");

        GetPointsTask getPointsTask = new GetPointsTask();
        getPointsTask.execute("Get Points_" + USER_NAME);

        FetchRoutes fetchRoutes = new FetchRoutes();
        fetchRoutes.execute("Get Routes_");

        pathPoints.width(10).color(Color.parseColor("#3498db")).geodesic(true);

        //Building up the Google Maps Client

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(HomeActivity.this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(HomeActivity.this).addApi(AppIndex.API).build();

        //Setting up the Wi-fi direct feature and turning it always on
        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);

        registerReceiver(mReceiver, filter);

        IntentFilter travelFilter = new IntentFilter();
        travelFilter.addAction("TRAVEL");
        bReceiverTravel = bReceiverTravel_function;
        registerReceiver(bReceiverTravel, travelFilter);

        // Setup Location manager and receiver
        lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        else
            lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
    }


    @Override
    protected void onStart(){
        super.onStart();

        Intent intent = new Intent(this, SimWifiP2pService.class);
        startService(intent);
        getmConnection = mConnection;
        bindService(intent, getmConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        if(DataHolder.getInstance().getmSrvSocket()==null)
            new IncommingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        timerToContactServer();
        //confirmTransactionsWithServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if (bReceiverTravel == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("TRAVEL");
            bReceiverTravel = bReceiverTravel_function;
            registerReceiver(bReceiverTravel,filter);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bReceiverTravel != null) {
            try {
                unregisterReceiver(bReceiverTravel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bReceiverTravel = null;
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        TextView myPoints = (TextView) findViewById(R.id.pointsValue);
        myPoints.setText(DataHolder.getInstance().getAppUser().getUserPoints() + "");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(HomeActivity.this, Login.class));
        Intent intent = new Intent(this, SimWifiP2pService.class);
        unbindService(getmConnection);
        stopService(intent);
        unregisterReceiver(mReceiver);
        unregisterReceiver(bReceiverTravel);
        DataHolder.getInstance().setmSrvSocket(null);
        finish();
    }

    public String getData() {return string;}
    public void setData(String string) {this.string = string;}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, HomeActivity.this);
                    }catch(SecurityException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fillMap();
    }

    private void fillMap() {

        // Add a marker in Tagus and move the camera
        mMap.clear();

        for(BikeStation bikeStation : INITIAL_SERVICE_STATE.values()) {
            LatLng bikeStationPosition = new LatLng(bikeStation.getLatitude(), bikeStation.getLongitude());

            //Move camera to a position where it's possible to see all stations
            if(bikeStation.getStationName().equals("Station-Tagus3"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bikeStationPosition, 15));

            bicycleStationMarkers.put(bikeStation.getStationName(), mMap.addMarker(new MarkerOptions().position(bikeStationPosition).title(bikeStation.getStationName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_bike))));
        }
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                String bikeStationName = INITIAL_SERVICE_STATE.get(marker.getTitle()).getStationName();
                String bikesAvailable = INITIAL_SERVICE_STATE.get(marker.getTitle()).getBikesAvailable()+"";

                // Getting view from the layout file
                View v = getLayoutInflater().inflate(R.layout.info_window, null);

                TextView title = (TextView) v.findViewById(R.id.windowTitle);
                title.setText(bikeStationName);// + " Bicycle Station");

                TextView text = (TextView) v.findViewById(R.id.windowText);
                text.setText("Available Bikes: " + bikesAvailable);
                return v;
            }
        });

        //Initial Station in focus
        for(Marker marker : bicycleStationMarkers.values()){
            String bikeStationName = INITIAL_SERVICE_STATE.get(marker.getTitle()).getStationName();
            if(bikeStationName.equals("Station-Tagus3"))
                marker.showInfoWindow();
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final String bikeStationName = INITIAL_SERVICE_STATE.get(marker.getTitle()).getStationName();
                String bikesAvailable = INITIAL_SERVICE_STATE.get(marker.getTitle()).getBikesAvailable() + "";

                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle(bikeStationName) //" Bicycle Station")
                        .setMessage("Number of Bikes Available: " + bikesAvailable)
                        .setPositiveButton(R.string.book_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with booking to the server
                                sendBookToServer(bikeStationName);
                            }
                        })
                        .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    public void onPointsClick(View view){
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, getmConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        Intent intentPoints = new Intent(this, PointsActivity.class);
        startActivity(intentPoints);
    }

    public void onMessagesClick(View view){
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, getmConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        Intent intentMsgs = new Intent(this, MessagesActivity.class);
        startActivity(intentMsgs);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mManager = new SimWifiP2pManager(new Messenger(service));
            DataHolder.getInstance().setmManager(mManager);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            DataHolder.getInstance().setmChannel(mChannel);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };

    /*
	 * Termite listeners
	 */

    private final BroadcastReceiver bReceiverTravel_function = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBound) {
                mManager.requestPeers(mChannel, HomeActivity.this);
            } else {
                Toast.makeText(HomeActivity.this, "Service not bound", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        String newStationName = null;
        String lastStationName = DataHolder.getInstance().getCloseStation();
        String newBikeName = null;
        String lastBikeName = DataHolder.getInstance().getCloseBike();

        Boolean isNearBike = false;
        Boolean wasNearBike = DataHolder.getInstance().getIsNearToBike();
        Boolean isNearStation = false;
        Boolean wasNearStation = DataHolder.getInstance().getIsNearToStation();


        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            if (!DataHolder.getInstance().getAppUser().existsPeer(device.deviceName)){
                DataHolder.getInstance().getAppUser().getPeers().put(device.deviceName, new User(device.deviceName, device.getVirtIp()));
                DataHolder.getInstance().getNewPeersAvailable().add(device);
                //DataHolder.getInstance().getOldPeers().add(device);
            }
            if (isBike(device.deviceName)){
                newBikeName = device.deviceName;
                isNearBike = true;
                DataHolder.getInstance().setIsNearToBike(true);
                DataHolder.getInstance().setCloseBike(device.deviceName);
            } else if (isStation(device.deviceName)){

                newStationName = device.deviceName;
                isNearStation = true;
                DataHolder.getInstance().setIsNearToStation(true);
                DataHolder.getInstance().setCloseStation(device.deviceName);
            }
        }

        if (isNearStation){
            Log.d("Estou", "perto de uma station xD");
            if(!wasNearStation){
                Log.d("Station", "Cheguei");
                Toast.makeText(HomeActivity.this, "Arrived to station " + newStationName, Toast.LENGTH_SHORT).show();
            }
            if (!isNearBike) {
                Log.d("Nao estou", "perto da minha bike :P");
                if (wasNearBike) {
                    Toast.makeText(HomeActivity.this, "Delivered " + lastBikeName + " to station " + newStationName, Toast.LENGTH_SHORT).show();
                    informPickOrDeliverToServer(newStationName,"DeliverBike");


                    if(!userTrajectory.getTrajectoryLong().isEmpty() && !userTrajectory.getTrajectoryLat().isEmpty()) {
                        DataHolder.getInstance().addTrajectory(userTrajectory);
                        DataHolder.getInstance().addTrajectoryToUpdate(userTrajectory);
                    }
                    userTrajectory = null;
                    for(Polyline line : actualPath)
                        line.remove();
                }
                DataHolder.getInstance().setIsNearToBike(false);
            }
        }

        if (!isNearStation) {
            if(wasNearStation) {
                Toast.makeText(HomeActivity.this, "Left station " + lastStationName, Toast.LENGTH_SHORT).show();
                if (isNearBike) {
                    //left the station with the bike
                    Toast.makeText(HomeActivity.this, "Have a good ride! We hope you enjoy " + newBikeName + " !", Toast.LENGTH_SHORT).show();
                    informPickOrDeliverToServer(lastStationName, "PickBike");


                    this.userTrajectory =  new Trajectory();
                }else
                    DataHolder.getInstance().setIsNearToBike(false);
            }
            if (!isNearBike && userTrajectory != null) {
                for (String lat : userTrajectory.getTrajectoryLat())
                    Log.d("LatList", lat);
                for (String lon : userTrajectory.getTrajectoryLong())
                    Log.d("LongList", lon);

                if(!userTrajectory.getTrajectoryLong().isEmpty() && !userTrajectory.getTrajectoryLat().isEmpty()){
                    DataHolder.getInstance().addTrajectory(userTrajectory);
                    DataHolder.getInstance().addTrajectoryToUpdate(userTrajectory);
                }
                this.userTrajectory = null;
                for(Polyline line : actualPath)
                    line.remove();
            }

            DataHolder.getInstance().setIsNearToStation(false);
            DataHolder.getInstance().setCloseStation("None");
        }
    }


    public boolean isStation(String deviceName){

        Boolean result = false;
        String pair[] = deviceName.split("-");
        if (pair.length>1) {
            if(pair[0].equals("Station"))
                result = true;
        }
        return result;
    }

    public boolean isBike(String deviceName){

        Boolean result = false;
        String pair[] = deviceName.split("-");
        if (pair.length>1) {
            if(pair[0].equals("Bike"))
                result = true;
        }
        return result;
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("GPS", "Location Changed " + location.toString());
        Log.d("Loc Long", location.getLatitude()+"");
        Log.d("Loc Lat", location.getLongitude()+"");

        final Location myLocation = location;

        pathPoints.add(new LatLng(myLocation.getLongitude(), myLocation.getLatitude()));

        final MarkerOptions currentPositionMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bicycle))
                .position(new LatLng(myLocation.getLongitude(), myLocation.getLatitude()));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (previousLocationMarker != null)
                    previousLocationMarker.remove();
                if(userTrajectory != null) {
                    Log.d("Trajetoria", "nao e nula");
                    userTrajectory.addGPSPoint(myLocation.getLongitude(), myLocation.getLatitude());
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(myLocation.getLongitude(), myLocation.getLatitude()), 15));
                actualPath.add(mMap.addPolyline(pathPoints));
                Marker currentPosition = mMap.addMarker(currentPositionMarker);


                previousLocationMarker = currentPosition;

            }
        });

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (previousLocation != null && DataHolder.getInstance().getIsNearToBike()) {
                    Log.d("Pontos", "vou actualizar");
                    float distance = previousLocation.distanceTo(myLocation);
                    User appUser = DataHolder.getInstance().getAppUser();
                    int pointsToAdd = (int) distance/100;
                    appUser.setUserPoints(appUser.getUserPoints() + pointsToAdd);
                    TextView pointsView = (TextView) findViewById(R.id.pointsValue);
                    pointsView.setText(appUser.getUserPoints() + "");

                    //verifica se andou
                    if(pointsToAdd!=0) {
                        appUser.addIdTransaction();
                        Transaction transaction = new Transaction("Riding", appUser.getUserName(), String.valueOf(pointsToAdd), appUser.getIdTransaction());
                        DataHolder.getInstance().addPointsToCommit(transaction);
                    }
                }
                previousLocation = myLocation;
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void sendBookToServer(String stationName){
        ARGUMENTS = "Book_"+stationName;

        if (!DataHolder.getInstance().getAppUser().hasBikeBooked()) {
            BookBikeTask bookBikeTask = new BookBikeTask();
            bookBikeTask.execute();
        } else {
            Toast.makeText(HomeActivity.this, "You already have a bike booked!", Toast.LENGTH_LONG).show();
        }

    }

    private void informPickOrDeliverToServer(String stationName, String typeOfInform){

        if (stationName.equals(DataHolder.getInstance().getAppUser().getBookedStationName()) && DataHolder.getInstance().getAppUser().hasBikeBooked() && typeOfInform.equals("PickBike")) {
            DataHolder.getInstance().getAppUser().setHasBikeBooked(false);
            DataHolder.getInstance().getAppUser().setBookedStationName("none");
            ARGUMENTS_TO_INFORM = "Inform_" + typeOfInform + "," + stationName + ",true";
            //} else if(stationName.equals(DataHolder.getInstance().getAppUser().getBookedStationName()) && DataHolder.getInstance().getAppUser().hasBikeBooked()) {
            //    ARGUMENTS_TO_INFORM = "Inform_" + typeOfInform + "," + stationName + ",true";
        } else {
            ARGUMENTS_TO_INFORM = "Inform_" + typeOfInform +"," + stationName + ",false";
        }

        InformPickOrDeliverTask informPickOrDeliverTask = new InformPickOrDeliverTask();
        informPickOrDeliverTask.execute();
    }

    private class FetchBikeStations extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("Server unreachable")){
                Toast.makeText(HomeActivity.this, s, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            RequestToServer requestToServer = new RequestToServer();
            try {
                serverAnswer = requestToServer.tradeInformation(params[0] + "\n");
                StationsParser stationsParser = new StationsParser(serverAnswer);
                INITIAL_SERVICE_STATE = stationsParser.getAllStations();
            }
            catch (Exception e){
                return "Server unreachable";
            }
            return serverAnswer;
        }
    }

    private class InformPickOrDeliverTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String answer) {
            super.onPostExecute(answer);
            /*String[] s = answer.split("_", -1);
            String stationName = s[0];
            String numBikesAvailable = s[1];
            String nameOfBikeAtributed = s[2];*/

            if (answer.equals("Server unreachable")){
                Toast.makeText(HomeActivity.this, answer, Toast.LENGTH_LONG).show();
            }else {
                if (answer.equals("bikeDeliverInformDone")) {
                    Toast.makeText(HomeActivity.this, "Delivering Info Successful", Toast.LENGTH_LONG).show();

                    //Saves that the current user has booked a bike
                } else if (answer.equals("bikePickInformDone")) {

                    Toast.makeText(HomeActivity.this, "Picking Bike Info Successful", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(HomeActivity.this, "An error occurred informing the server", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            RequestToServer requestToServer = new RequestToServer();
            try {
                serverAnswer = requestToServer.tradeInformation(ARGUMENTS_TO_INFORM + "\n");
                return serverAnswer;
            }catch (Exception e){
                e.printStackTrace();
                return "Server unreachable";
            }
        }
    }


    private class BookBikeTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String answer) {
            super.onPostExecute(answer);
            String[] s = answer.split("_", -1);
            String stationName = s[0];
            String numBikesAvailable = s[1];
            //String nameOfBikeAtributed = s[2];

            if (answer.equals("Server unreachable")){
                Toast.makeText(HomeActivity.this, stationName, Toast.LENGTH_LONG).show();
            }else {
                if (!stationName.equals("-1")) {
                    numberOfBikesAvailable = Integer.valueOf(numBikesAvailable);
                    INITIAL_SERVICE_STATE.get(stationName).setBikesAvailable(numberOfBikesAvailable);
                    bicycleStationMarkers.get(stationName).showInfoWindow();
                    Toast.makeText(HomeActivity.this, "Booked Successfully!", Toast.LENGTH_LONG).show();

                    //Saves that the current user has booked a bike
                    DataHolder.getInstance().getAppUser().setHasBikeBooked(true);
                    DataHolder.getInstance().getAppUser().setBookedStationName(stationName);
                } else
                    Toast.makeText(HomeActivity.this, "No more bikes available!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            RequestToServer requestToServer = new RequestToServer();
            try {
                serverAnswer = requestToServer.tradeInformation(ARGUMENTS + "\n");
                //Toast.makeText(HomeActivity.this, serverAnswer, Toast.LENGTH_LONG).show();
                Log.d(TAG, "\t\t\t=================================== (" + serverAnswer + ").");
                String[] s = serverAnswer.split("_", -1);
                Log.d(TAG, "\t\t\t=================================== (" + s[0] + ").");
                return serverAnswer;
            }catch (Exception e){
                e.printStackTrace();
                return "Server unreachable";
            }
        }
    }

    private class GetPointsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!s.equals("Server unreachable")) {
                TextView pointsTextView = (TextView) findViewById(R.id.pointsValue);
                pointsTextView.setText(serverAnswer);
                DataHolder.getInstance().createUser(USER_NAME, Integer.parseInt(serverAnswer));
                SendPublicKeyTask sendPublicKeyTask = new SendPublicKeyTask();
                sendPublicKeyTask.execute();
            }
            else
                Toast.makeText(HomeActivity.this, s, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            RequestToServer requestToServer = new RequestToServer();
            try {
                serverAnswer = requestToServer.tradeInformation(params[0] + "\n");
                return serverAnswer;
            }catch (Exception e){
                return "Server unreachable";
            }
        }
    }

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(Integer.parseInt("10001"));
                DataHolder.getInstance().setmSrvSocket(mSrvSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    sock = mSrvSocket.accept();
                    try {
                        BufferedReader sockIn = new BufferedReader(
                                new InputStreamReader(sock.getInputStream()));

                        String st = sockIn.readLine();
                        if (st.contains("SendPoints")) {
                            while (sockIn.ready()) {
                                st+=sockIn.readLine();
                            }
                        }

                        publishProgress(st);
                    } catch (IOException e) {
                        Log.d("Error reading socket:", e.getMessage());
                        break;
                    }
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            User appUser = DataHolder.getInstance().getAppUser();

            String fullMsgText = values[0];
            Log.d(TAG, "\t\t\t\t\t msg recebida ==> " + fullMsgText);

            String[] parts = fullMsgText.split(EofIndicatorClass.class.toString());
            String nameOfSender= parts[0]; // 004
            String msgText = parts[1]; // 034556
            String request = parts[2];
            Log.d("request = ", request);

            String signedMsg="";
            int idTransaction = 0;
            if (request.equals("SendPoints")){
                idTransaction = Integer.parseInt(parts[3]);
                signedMsg = parts[4];
                Log.d("Signed msg = " , signedMsg);
                Log.d("idTransaction = ", idTransaction+"");
            }

            switch (request){

                case "SendMessage":
                    Message msg = new Message(nameOfSender, msgText, false);
                    if (msg == null)
                        msg = new Message("NomeMaluco", "deu buraco no onprogressupdate", false);
                    Log.d(TAG, "\t\t\t\t\t msg estava a null");


                    // Appending the message to chat list
                    //appendMessage(m);
                    Log.d(TAG, "\t\t\t\t\t msg.getFromName() ==> " + msg.getFromName());
                    Log.d(TAG, "\t\t\t\t\t msg.getMessage() ==> " + msg.getMessage());
                    DataHolder.getInstance().addMessageToHolder(msg.getFromName(), msg);
                    Intent in = new Intent("MSG_RECEIVED");
                    sendBroadcast(in);
                    break;

                case "RequestPoints":
                    pointsToSend = appUser.getUserPoints()+"";
                    try {
                        sock.getOutputStream().write((pointsToSend + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "SendPoints":
                    for (int i=0; i<parts.length; i=i+10) {
                        String sender = parts[i];
                        String points = parts[i+1];
                        String requestPoints = parts[i+2];
                        String id = parts[i+3];
                        String reconstructedRequest = sender + EofIndicatorClass.class.toString() //i = nameofsender
                                + points + EofIndicatorClass.class.toString() //i+1 = points
                                + requestPoints + EofIndicatorClass.class.toString() //i+2 = sendpoints
                                + id; // i+3 = transactionID

                        String receiver = parts[i+4];
                        String msgSigned = parts[i+9];
                        if (verifyThatMsgCameFromPeer(reconstructedRequest, msgSigned, "")) {
                            Log.d("Verifiquei", "e esta tudo OK!!");
                            int receivedPoints = Integer.parseInt(msgText);
                            int actualPoints = appUser.getUserPoints();
                            //appUser.setUserPoints(actualPoints + receivedPoints);


                        /*Toast.makeText(getApplicationContext(),
                                nameOfSender + " sent you " + receivedPoints + " points!",
                                Toast.LENGTH_LONG).show();*/

                            //appUser.getPeerByName(nameOfSender).subtractPoints(receivedPoints);
                            Transaction transaction = new Transaction(sender, receiver, points, Integer.valueOf(id));

                            appUser.addTemporaryTransactions(transaction);
                            DataHolder.getInstance().addPointsToCommit(transaction);
                            DataHolder.getInstance().addSignatureToCommit(reconstructedRequest + EofIndicatorClass.class.toString() + msgSigned);
                            Intent inPoints = new Intent("REFRESH POINTS");
                            sendBroadcast(inPoints);
                            String finalMsg="";
                            int index = -1;
                            for (Transaction t : DataHolder.getInstance().getPointsToCommit()){
                                index++;
                                finalMsg += t.getSenderUsername() + EofIndicatorClass.class.toString()
                                        + t.getPoints() + EofIndicatorClass.class.toString()
                                        + "SendPoints" + EofIndicatorClass.class.toString()
                                        + t.getId() + EofIndicatorClass.class.toString()
                                        + t.getReceiverUsername();
                                finalMsg += EofIndicatorClass.class.toString()
                                        + DataHolder.getInstance().getSignaturesToCommit().get(index)
                                        + EofIndicatorClass.class.toString();
                                Log.d("MSG SIGNED!!!", DataHolder.getInstance().getSignaturesToCommit().get(index));
                            }
                            Log.d("FINALE HOME", finalMsg.replaceAll(EofIndicatorClass.class.toString(), " ; "));

                            pointsToSend = appUser.getUserPoints() + "";
                            try {
                                sock.getOutputStream().write((pointsToSend + "\n").getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else
                            Log.d("UPS", "signature deu mal!!!");
                        //break;
                    }
                    break;
            }
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void timerToContactServer (){
        final android.os.Handler handler = new android.os.Handler();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask(){
            @Override
            public void run(){
                handler.post(new Runnable(){
                    public void run(){
                        try{
                            TryToSendPointsTask tryToSendPointsTask = new TryToSendPointsTask();
                            tryToSendPointsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            TryToSendTrajectoriesTask tryToSendTrajectoriesTask = new TryToSendTrajectoriesTask();
                            tryToSendTrajectoriesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 90000);
    }

    /*public void confirmTransactionsWithServer(){
        final android.os.Handler handlerTransactions = new android.os.Handler();
        Timer timerConfirm = new Timer();
        TimerTask timerTransactionsTask = new TimerTask(){
            @Override
            public void run(){
                handlerTransactions.post(new Runnable(){
                    public void run(){
                        try{
                            ConfirmTransactionsTask confirmTransactionsTask = new ConfirmTransactionsTask();
                            confirmTransactionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timerConfirm.schedule(timerTransactionsTask, 0, 120000);
    }*/

    /*private class ConfirmTransactionsTask extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            if (!s.equals("Server unreachable") && (!s.equals("EMPTY"))){
                List<Transaction> transactionsToConfirm = DataHolder.getInstance().getAppUser().getTransactionsToConfirm();
                User appUser = DataHolder.getInstance().getAppUser();
                String[] answerFromServer = s.split(EofIndicatorClass.class.toString(), -1);
                int id;
                String message;
                List<Integer> idToDeleteList = new ArrayList<Integer>();
                for (int i = 0; i<answerFromServer.length; i++){
                    id = Integer.valueOf(answerFromServer[i]);
                    i++;
                    message = answerFromServer[i];
                    if (message.equals("Aborted")){
                        for (Transaction t : transactionsToConfirm){
                            if (t.getId() == id) {
                                appUser.setUserPoints(appUser.getUserPoints() + Integer.valueOf(t.getPoints()));
                                idToDeleteList.add(id);
                                break;
                            }
                        }
                    }
                    if (message.equals("Commited"))
                        idToDeleteList.add(id);
                }

                Iterator<Transaction> iterator = transactionsToConfirm.iterator();
                while (iterator.hasNext()) {
                    Transaction transaction = iterator.next();
                    if (idToDeleteList.contains(transaction.getId())) {
                        iterator.remove();
                    }
                }

            }
        }

        @Override
        protected String doInBackground(Void... params){
            String answer = "";
            if (DataHolder.getInstance().getAppUser().getTransactionsToConfirm().isEmpty())
                return "EMPTY";
            else{
                RequestToServer requestToServer = new RequestToServer();
                String request = DataHolder.getInstance().getAppUser().getUserName() + EofIndicatorClass.class.toString() ;
                for (Transaction t : DataHolder.getInstance().getAppUser().getTransactionsToConfirm()){
                    request += t.getId() + EofIndicatorClass.class.toString();
                }
                request = request.substring(0, request.length() - EofIndicatorClass.class.toString().length());
                try {
                    answer = requestToServer.tradeInformation("Confirm Transactions_" + request + "\n");
                }catch (Exception e){
                    return "Server unreachable";
                }
            }
            return answer;
        }
    }*/

    private class TryToSendPointsTask extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (!s.equals("Server unreachable") && (!s.equals("EMPTY")) && (!s.equals(""))){
                DataHolder.getInstance().getPointsToCommit().clear();
                DataHolder.getInstance().getSignaturesToCommit().clear();
                String[] answer = s.split(EofIndicatorClass.class.toString(),-1);
                User appUser = DataHolder.getInstance().getAppUser();
                String sender, receiver, pointsSender, pointsReceiver;

                for (int i=0; i<answer.length; i++){
                    sender = answer[i];
                    i++;
                    pointsSender = answer[i];
                    i++;
                    receiver = answer[i];
                    i++;
                    pointsReceiver = answer[i];
                    if (sender.equals(appUser.getUserName())){ // ou seja quem enviou os pontos
                        appUser.setUserPoints(Integer.valueOf(pointsSender));
                    }
                    if (receiver.equals(appUser.getUserName())){ //ou seja quem recebeu os pontos
                        appUser.setUserPoints(Integer.valueOf(pointsReceiver));
                        Toast.makeText(getApplicationContext(), sender + " sent you " + " points!", Toast.LENGTH_SHORT).show();
                    }
                    TextView myPoints = (TextView) findViewById(R.id.pointsValue);
                    myPoints.setText(DataHolder.getInstance().getAppUser().getUserPoints() + "");
                }
            }

            /*if (!s.equals("Server unreachable") && (!s.equals("EMPTY")) && (!s.equals("All wrong"))) {
                DataHolder.getInstance().getPointsToCommit().clear();
                DataHolder.getInstance().getSignaturesToCommit().clear();
                User appUser = DataHolder.getInstance().getAppUser();
                String[] transactionsCommited = s.split(EofIndicatorClass.class.toString(),-1);
                String sender, receiver, points;
                int id;

                for (int i=0; i<transactionsCommited.length; i++){
                    sender = transactionsCommited[i];
                    Log.d("Sender", sender);
                    i++;
                    receiver = transactionsCommited[i];
                    Log.d("Receiver", receiver);
                    i++;
                    points = transactionsCommited[i];
                    Log.d("Points", points);
                    i++;
                    id = Integer.valueOf(transactionsCommited[i]);
                    Log.d("Id", id+"");
                    for (Transaction t : DataHolder.getInstance().getAppUser().getTemporaryTransactions()){
                        if (t.equals(new Transaction(sender,receiver,points,id))){
                            appUser.setUserPoints(appUser.getUserPoints() + Integer.valueOf(points));
                            Toast.makeText(getApplicationContext(),
                                    sender + " sent you " + points + " points!",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }
            if (s.equals("All wrong")){
                DataHolder.getInstance().getPointsToCommit().clear();
                DataHolder.getInstance().getSignaturesToCommit().clear();
            }*/
        }

        @Override
        protected String doInBackground(Void... params){
            String answer = "";
            if (DataHolder.getInstance().getPointsToCommit().isEmpty())
                return "EMPTY";
            else{
                RequestToServer requestToServer = new RequestToServer();
                String request = "";
                int index = -1;
                for (Transaction t : DataHolder.getInstance().getPointsToCommit()) {
                    index++;
                    request += t.getSenderUsername() + EofIndicatorClass.class.toString() + t.getReceiverUsername() + EofIndicatorClass.class.toString() + t.getPoints() + EofIndicatorClass.class.toString() + t.getId() + EofIndicatorClass.class.toString();
                    if (!t.getSenderUsername().equals("Riding"))
                        request += DataHolder.getInstance().getSignaturesToCommit().get(index) + EofIndicatorClass.class.toString();
                }
                request = request.substring(0, request.length() - EofIndicatorClass.class.toString().length());
                try {
                    answer = requestToServer.tradeInformation("New Transaction_" + request + "\n");
                }catch (Exception e){
                    return "Server unreachable";
                }
            }
            return answer;
        }
    }

    private class TryToSendTrajectoriesTask extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("OK")){
                DataHolder.getInstance().getTrajectories().clear();
            }
        }

        @Override
        protected String doInBackground(Void... params){
            String answer = "";
            if (DataHolder.getInstance().getTrajectories().isEmpty())
                return "EMPTY";
            else{
                RequestToServer requestToServer = new RequestToServer();
                String request = DataHolder.getInstance().getAppUser().getUserName()+":";
                for (Trajectory trajectory : DataHolder.getInstance().getTrajectories()){
                    for(int i = 0; i < trajectory.getTrajectoryLat().size(); i++)
                        request += trajectory.getTrajectoryLat().get(i) + "," + trajectory.getTrajectoryLong().get(i) + ";";
                }
                request = request.substring(0, request.length()-1);
                try {
                    answer = requestToServer.tradeInformation("New Trajectory_" + request + "\n");
                }catch (Exception e){
                    return "Server unreachable";
                }
            }
            return answer;
        }
    }

    private class SendPublicKeyTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            RequestToServer requestToServer = new RequestToServer();
            byte[] myPublicKeyBytes = DataHolder.getInstance().getAppUser().getPublicKey().getEncoded();
            String myPublicKey = Base64.encodeToString(myPublicKeyBytes, Base64.DEFAULT);
            String request = DataHolder.getInstance().getAppUser().getUserName() + ";;;" + myPublicKey;
            Log.d("PUBLIC KEY A ENVIAR", myPublicKey);
            String answer = "";
            try{
                answer = requestToServer.tradeInformation("Send PublicKey_" + request + "\n");
            }
            catch (Exception e){
                return "Server unreachable";
            }
            return answer;
        }
    }

    public boolean verifyThatMsgCameFromPeer(String msg, String signedMsg, String peerPublicKey){
        return true;
    }

    private class FetchRoutes extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("Server unreachable"))
                Toast.makeText(HomeActivity.this, s, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String answer;
            RequestToServer requestToServer = new RequestToServer();
            try{
                answer = requestToServer.tradeInformation(params[0] + USER_NAME + "\n");

                if(!answer.equals("EMPTY")) {
                    TrajectoriesParser trajectoriesParser = new TrajectoriesParser(answer);
                    List<Trajectory> trajectoriesFromServer = trajectoriesParser.getAllTrajectories();
                    DataHolder.getInstance().setTrajectoriesUpdated(trajectoriesFromServer);
                }
            }catch (Exception e){
                return "Server unreachable";
            }
            return answer;
        }
    }
}