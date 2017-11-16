package pt.ulisboa.tecnico.cmov.ubibike.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.adapters.MessagesListAdapter;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Message;
import pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;
import pt.ulisboa.tecnico.cmov.ubibike.storage.Utils;

public class SendMessageActivity extends AppCompatActivity{


    // LogCat tag
    private static final String TAG = SendMessageActivity.class.getSimpleName();
    public final static String USER_ID = "pt.ulisboa.tecnico.cmov.ubibike.USERID";

    private Button btnSend;
    private EditText inputMsg;

    //private WebSocketClient client;

    // Chat messages list adapter
    private MessagesListAdapter adapter;
    private List<Message> listMessages;
    private ListView listViewMessages;

    private Utils utils;

    // USERNAME
    private String username = DataHolder.getInstance().getUsername();
    // Client name
    private String name = null;
    // Client IP
    private  String userIP;

    // JSON flags to identify the kind of JSON response
    private static final String TAG_SELF = "self", TAG_NEW = "new",
            TAG_MESSAGE = "message", TAG_EXIT = "exit";

    private SimWifiP2pSocket mCliSocket = null;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pManager mManager;
    private SimWifiP2pManager.Channel mChannel;
    private boolean mBound = false;
    private ServiceConnection mConnection;


    private BroadcastReceiver bReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);


        SimWifiP2pSocketManager.Init(getApplicationContext());

        mManager = DataHolder.getInstance().getmManager();
        mChannel = DataHolder.getInstance().getmChannel();
        mConnection = DataHolder.getInstance().getmConnection();

        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        listViewMessages = (ListView) findViewById(R.id.list_view_messages);

        Toolbar toolbar = (Toolbar) findViewById(R.id.send_messages_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        utils = new Utils(getApplicationContext());

        // Getting the person name from previous screen
        Intent i = getIntent();
        name = i.getStringExtra(USER_ID);
        userIP = i.getStringExtra("USER_IP");

        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String msgToSend = username + EofIndicatorClass.class.toString() + inputMsg.getText().toString() + " "
                        + EofIndicatorClass.class.toString() + "SendMessage";
                new SendCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,msgToSend);

                Message m = new Message(username, inputMsg.getText().toString(), true);// Appending the message to chat list
                DataHolder.getInstance().addMessageToHolder(name, m);
                appendMessage(m);
                inputMsg.setText("");
            }
        });

        listMessages = new ArrayList<Message>();
        adapter = new MessagesListAdapter(this, listMessages);
        listViewMessages.setAdapter(adapter);

        IntentFilter filter = new IntentFilter("MSG_RECEIVED");
        bReceiver = bReceiver_function;
        registerReceiver(bReceiver, filter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        List<Message> messageList = DataHolder.getInstance().getMessagesListForName(name);
        for (Message m : messageList) {
            appendMessage(m);
        }

        Message m = new Message(name, userIP, false);
        // Appending the message to chat list
        appendMessage(m);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCliSocket != null) {
            try {
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mCliSocket = null;

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
    }

    private void appendMessage(final Message m) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                listMessages.add(m);

                adapter.notifyDataSetChanged();

                // Playing device's notification
                //playBeep();
            }
        });
    }

    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            try {

                if (mCliSocket == null) {
                    mCliSocket = new SimWifiP2pSocket(userIP, Integer.parseInt("10001"));
                }

                mCliSocket.getOutputStream().write((msg[0] + "\n").getBytes());
                BufferedReader sockIn = new BufferedReader(new InputStreamReader(mCliSocket.getInputStream()));
                sockIn.readLine();
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "\t\t\t\t acabou a execução  \t");
        }
    }

    /*public void loadPeers(){
        if (mBound) {
            mManager.requestPeers(mChannel, SendMessageActivity.this);
        } else {
            Toast.makeText(this, "Service not bound",	Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        StringBuilder peersStr = new StringBuilder();

        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = "" + device.deviceName + " (" + device.getVirtIp() + ")\n";
            peersStr.append(devstr);
        }

        // display list of devices in range
        new AlertDialog.Builder(this)
                .setTitle("Devices in WiFi Range")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }*/

    private final BroadcastReceiver bReceiver_function = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //adapter.clear();
            Log.d(TAG, "\t\t\t\t\t Veio até aqui !!!");
            Message msg = DataHolder.getInstance().getLastMessage(name);
            Log.d(TAG, "\t\t\t\t\t " + msg.getMessage());
            //listMessages.add(msg);

            appendMessage(msg);
            //adapter.notifyDataSetChanged();
        }
    };

}
