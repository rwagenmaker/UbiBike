package pt.ulisboa.tecnico.cmov.ubibike.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.domain.Transaction;
import pt.ulisboa.tecnico.cmov.ubibike.parsers.EofIndicatorClass;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;

public class SendPointsActivity extends AppCompatActivity {

    private String usernameToSend;
    private String userIPToSend;
    private String pointsToSend;
    private TextView pointsToSendText;
    private SimWifiP2pSocket mCliSocket = null;
    private String points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_points);

        Intent intent = getIntent();
        usernameToSend = intent.getStringExtra(PointsActivity.USER_ID);
        userIPToSend = intent.getStringExtra(PointsActivity.USER_IP);

        Toolbar toolbar = (Toolbar) findViewById(R.id.send_points_toolbar);
        toolbar.setTitle("Send Points to "+usernameToSend);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pointsToSendText = (TextView) findViewById(R.id.pointsToSend);
        pointsToSend = pointsToSendText.getText().toString();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        super.onKeyUp(keyCode, event);
        switch(keyCode){
            case KeyEvent.KEYCODE_ENTER:
                pointsToSend = pointsToSendText.getText().toString();
                new SendPointsTask().execute();
                onBackPressed();
                break;
        }
        return true;
    }

    private class SendPointsTask extends AsyncTask <Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String validPoints = validPointsToSend();
            if (validPoints.equals("True")) {
                try {
                    if (mCliSocket == null) {
                        mCliSocket = new SimWifiP2pSocket(userIPToSend, Integer.parseInt("10001"));
                    }
                    DataHolder.getInstance().getAppUser().addIdTransaction();
                    Transaction myTransaction = new Transaction(DataHolder.getInstance().getAppUser().getUserName(),
                            usernameToSend, pointsToSend,
                            DataHolder.getInstance().getAppUser().getIdTransaction());

                    //DataHolder.getInstance().getAppUser().addTransactionsToConfirm(myTransaction);

                    String msgToSend = DataHolder.getInstance().getAppUser().getUserName() + EofIndicatorClass.class.toString()
                            + pointsToSend + EofIndicatorClass.class.toString() + "SendPoints"
                            + EofIndicatorClass.class.toString() + DataHolder.getInstance().getAppUser().getIdTransaction();

                    String msgSigned = generateSignature(msgToSend);

                    msgToSend += EofIndicatorClass.class.toString() + msgSigned;

                    DataHolder.getInstance().addPointsToCommit(myTransaction);
                    DataHolder.getInstance().addSignatureToCommit(msgToSend);

                    int index = -1;
                    String finalMsg = "";
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
                    finalMsg = finalMsg.substring(0, finalMsg.length() - EofIndicatorClass.class.toString().length());
                    Log.d("FINALE SEND", finalMsg.replaceAll(EofIndicatorClass.class.toString(), " ; "));

                    mCliSocket.getOutputStream().write((finalMsg + "\n").getBytes());
                    BufferedReader sockIn = new BufferedReader(new InputStreamReader(mCliSocket.getInputStream()));
                    points = sockIn.readLine();
                    Log.d("PONTOS DO RECEIVER:", points);
                    mCliSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCliSocket = null;
            }
            return validPoints;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("True"))
                Toast.makeText(SendPointsActivity.this, result, Toast.LENGTH_SHORT).show();
            else {
                Log.d("Entrei", "no else");
                DataHolder.getInstance().getAppUser().subtractPoints(Integer.parseInt(pointsToSend));
                //DataHolder.getInstance().getAppUser().getPeerByName(usernameToSend).setUserPoints(Integer.parseInt(points));
                //Intent inPoints = new Intent("REFRESH");
                //sendBroadcast(inPoints);
            }
        }
    }

    public String validPointsToSend(){
        int appUserPoints = DataHolder.getInstance().getAppUser().getUserPoints();
        if (!pointsToSend.isEmpty()) {
            if (appUserPoints >= Integer.parseInt(pointsToSend)) {
                return "True";
            }
            else
                return "Not enough points";
        }
        else
            return "Field is empty";
    }

    public String generateSignature(String msgToSend){
        String msgSigned = "";
        try{
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(DataHolder.getInstance().getAppUser().getPrivateKey());
            signature.update(msgToSend.getBytes());
            byte[] msgSignedInBytes = signature.sign();
            msgSigned = Base64.encodeToString(msgSignedInBytes, Base64.DEFAULT);
            Log.d("Msg signed", msgSigned);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e){
            Log.d("Nao consegui", "assinar a mensagem");
            e.printStackTrace();
        }
        return msgSigned;
    }
}