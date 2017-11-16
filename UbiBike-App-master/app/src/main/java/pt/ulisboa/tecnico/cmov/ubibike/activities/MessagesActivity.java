package pt.ulisboa.tecnico.cmov.ubibike.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.adapters.GeneralListAdapter;
import pt.ulisboa.tecnico.cmov.ubibike.domain.User;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;


public class MessagesActivity extends AppCompatActivity
                            implements SimWifiP2pManager.PeerListListener {

    public final static String USER_ID = "pt.ulisboa.tecnico.cmov.ubibike.USERID";

    private ArrayList<User> arrayOfUsers;
    private GeneralListAdapter adapter;
    private ListView lvItems;

    private SimWifiP2pManager mManager;
    private Channel mChannel;
    private BroadcastReceiver bReceiver = null;
    private boolean mBound = true;
    private ServiceConnection getmConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        mManager = DataHolder.getInstance().getmManager();
        mChannel = DataHolder.getInstance().getmChannel();
        getmConnection = DataHolder.getInstance().getmConnection();

        Toolbar toolbar = (Toolbar) findViewById(R.id.messages_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        lvItems = (ListView) findViewById(R.id.lvItems);
        arrayOfUsers = new ArrayList<User>();
        adapter = new GeneralListAdapter(this, arrayOfUsers, "Messages");
        lvItems.setAdapter(adapter);
        lvItems.setOnItemClickListener(new ListClickHandler());

        loadPeers();

        IntentFilter filter = new IntentFilter("REFRESH");
        bReceiver = bReceiver_function;
        registerReceiver(bReceiver, filter);

    }

    @Override
    protected void onStart() {
        super.onStart();
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
            IntentFilter filter = new IntentFilter("REFRESH");
            bReceiver = bReceiver_function;
            registerReceiver(bReceiver,filter);
        }

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    public void loadPeers(){
        if (mBound) {
            mManager.requestPeers(mChannel, MessagesActivity.this);
        } else {
            Toast.makeText(this, "Service not bound",	Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            if (!device.getVirtIp().equals("0.0.0.0"))
                adapter.add(new User(device.deviceName, device.getVirtIp()));
        }
    }

    private final BroadcastReceiver bReceiver_function = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.clear();
            loadPeers();
            adapter.notifyDataSetChanged();
        }
    };

    public class ListClickHandler implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
            // TODO Auto-generated method stub
            TextView userId = (TextView) view.findViewById(R.id.userID);
            TextView lastMessage = (TextView) view.findViewById(R.id.lastMessage);

            String userIDtext = userId.getText().toString();
            String userIP = lastMessage.getText().toString();
            Intent sendMessageIntent = new Intent(MessagesActivity.this, SendMessageActivity.class);
            sendMessageIntent.putExtra(USER_ID, userIDtext);
            sendMessageIntent.putExtra("USER_IP", userIP);

            //unbindService(getmConnection);

            startActivity(sendMessageIntent);
        }
    }
}

