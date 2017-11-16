package pt.ulisboa.tecnico.cmov.ubibike.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.activities.PointsActivity;
import pt.ulisboa.tecnico.cmov.ubibike.activities.SendPointsActivity;
import pt.ulisboa.tecnico.cmov.ubibike.domain.User;
import pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;

public class GeneralListAdapter extends ArrayAdapter<User> {

    Context context;
    List<User> data = null;
    String identifier;
    User user = null;
    private SimWifiP2pSocket mCliSocket = null;
    private TextView userPoints;
    private User appUser;

    public GeneralListAdapter(Context context, List data, String id) {
        super(context, 0, data);
        this.context = context;
        this.data = data;
        this.identifier = id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //User user = getItem(position);
        user = this.data.get(position);
        // Identifying the Activity

        if(this.identifier.equals("Routes")){
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.routes_rows, parent, false);
            }

            TextView routeDistance = (TextView) convertView.findViewById(R.id.distance);

        }else if (this.identifier.equals("Points")) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.points_rows, parent, false);
            }

            final TextView userId = (TextView) convertView.findViewById(R.id.userID);
            userId.setText(user.getUserName());

            if (!user.getIpAddress().equals("0.0.0.0")) {

                userPoints = (TextView) convertView.findViewById(R.id.userPoints);

                getPeerPoints();

                ImageButton button = (ImageButton) convertView.findViewById(R.id.sendPointsImg);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent sendPointsIntent = new Intent(v.getContext(), SendPointsActivity.class);
                        sendPointsIntent.putExtra(PointsActivity.USER_ID, user.getUserName());
                        sendPointsIntent.putExtra(PointsActivity.USER_IP, user.getIpAddress());
                        v.getContext().startActivity(sendPointsIntent);
                    }
                });
            }
        } else {
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.messages_rows, parent, false);
            }

            // Lookup view for data population
            TextView userID = (TextView) convertView.findViewById(R.id.userID);
            TextView lastMessage = (TextView) convertView.findViewById(R.id.lastMessage);


            // Populate the data into the template view using the data object
            userID.setText(user.getUserName());
            lastMessage.setText(user.getIpAddress());
            if (user.getIpAddress().equals("0.0.0.0"))
                lastMessage.setHint("THIS IS A BEACON");


            // Return the completed view to render on screen
        }
        return convertView;
    }

    public void getPeerPoints(){

        appUser = DataHolder.getInstance().getAppUser();

        if (DataHolder.getInstance().existsUserInNewPeers(user.getUserName())) {
            GetOtherUserPointsTask getPointsTask = new GetOtherUserPointsTask();
            Log.d("EU SOU O ---", appUser.getUserName() + " vou abrir um socket");
            String msgToSend = appUser.getUserName() + EofIndicatorClass.class.toString() + "***" +
                    EofIndicatorClass.class.toString() + "RequestPoints";
            getPointsTask.execute(msgToSend);
        }
        else {
            Log.d("EU SOU O ---", appUser.getUserName() + " tenho a lista atualizada localmente");
            userPoints.setText(appUser.getPeerByName(user.getUserName()).getUserPoints() + "");
        }
    }

    private class GetOtherUserPointsTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            Log.d("ON PRE = ", "\t\t\t\t\t OnPreExecute Points");
        }

        @Override
        protected String doInBackground(String... msg) {
            String points="";
            try {
                if (mCliSocket == null) {
                    mCliSocket = new SimWifiP2pSocket(user.getIpAddress(), Integer.parseInt("10001"));
                }
                Log.d("pedido do cliente", msg[0]);
                mCliSocket.getOutputStream().write((msg[0] + "\n").getBytes());
                BufferedReader sockIn = new BufferedReader(new InputStreamReader(mCliSocket.getInputStream()));
                points = sockIn.readLine();
                Log.d("recebi isto ", points);
                mCliSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            mCliSocket = null;
            return points;
        }

        @Override
        protected void onPostExecute(String s) {
            user.setUserPoints(Integer.parseInt(s));

            userPoints.setText(user.getUserPoints() + "");
            appUser.addPeer(user.getUserName(), user);
            Log.d("ON POST: ", "\t\t\t\t acabou a execução  \t");
        }

        /*@Override
        protected String doInBackground(String... params) {
            RequestToServer requestToServer = new RequestToServer();
            String serverAnswer = requestToServer.tradeInformation(params[0] + "\n");
            return serverAnswer;
        }*/
    }
}
