package pt.ulisboa.tecnico.cmov.ubibike.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.adapters.GeneralListAdapter;
import pt.ulisboa.tecnico.cmov.ubibike.domain.User;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;

public class PointsActivity extends AppCompatActivity implements SimWifiP2pManager.PeerListListener {

    public final static String USER_ID = "pt.ulisboa.tecnico.cmov.ubibike.USERID";
    public final static String USER_IP = "pt.ulisboa.tecnico.cmov.ubibike.USERIP";

    private List<User> pointsListByUser = new ArrayList<User>();

    private GeneralListAdapter pointsAdapter;
    private ListView listView;

    private SimWifiP2pManager mManager;
    private SimWifiP2pManager.Channel mChannel;
    private BroadcastReceiver bReceiver = null;
    //private BroadcastReceiver bReceiverPoints = null;
    private boolean mBound = true;
    private ServiceConnection getmConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        mManager = DataHolder.getInstance().getmManager();
        mChannel = DataHolder.getInstance().getmChannel();
        getmConnection = DataHolder.getInstance().getmConnection();

        Toolbar toolbar = (Toolbar) findViewById(R.id.points_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.pointsList);
        pointsAdapter = new GeneralListAdapter(this, pointsListByUser, "Points");
        listView.setAdapter(pointsAdapter);

        loadPeers();

        //IntentFilter filter = new IntentFilter("REFRESH");
        //IntentFilter filterPoints = new IntentFilter("REFRESH POINTS");
        IntentFilter filter = new IntentFilter();
        filter.addAction("REFRESH");
        //filter.addAction("REFRESH POINTS");
        bReceiver = bReceiver_function;
        //bReceiverPoints = bReceiverPoints_function;
        registerReceiver(bReceiver, filter);
        //registerReceiver(bReceiverPoints, filterPoints);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bReceiver != null) {
            try {
                unregisterReceiver(bReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bReceiver = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("REFRESH");
            //filter.addAction("REFRESH POINTS");
            bReceiver = bReceiver_function;
            registerReceiver(bReceiver,filter);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        pointsAdapter.clear();
        loadPeers();
        pointsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        DataHolder.getInstance().getOldPeers().clear();
    }

    public void loadPeers(){
        if (mBound) {
            mManager.requestPeers(mChannel, PointsActivity.this);
        } else {
            Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        DataHolder.getInstance().getNewPeersAvailable().clear();
        boolean isInList=false;
        for (SimWifiP2pDevice newPeer : peers.getDeviceList()){
            if (!newPeer.getVirtIp().equals("0.0.0.0")) {
                User u = new User(newPeer.deviceName, newPeer.getVirtIp());
                pointsAdapter.add(u);
            }
            for(SimWifiP2pDevice oldPeer : DataHolder.getInstance().getOldPeers()){
                isInList=false;
                if (newPeer.deviceName.equals(oldPeer.deviceName)){
                    isInList=true;
                    break;
                }
            }
            if(!isInList)
                DataHolder.getInstance().addToNewPeers(newPeer);
        }
        DataHolder.getInstance().setOldPeers(new ArrayList<>(peers.getDeviceList()));
    }

    private final BroadcastReceiver bReceiver_function = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pointsAdapter.clear();
            loadPeers();
            pointsAdapter.notifyDataSetChanged();
        }
    };
}
