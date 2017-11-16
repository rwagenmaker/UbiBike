package pt.ulisboa.tecnico.cmov.ubibike.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.ubibike.R;
import pt.ulisboa.tecnico.cmov.ubibike.network.RequestToServer;
import pt.ulisboa.tecnico.cmov.ubibike.storage.DataHolder;

public class Login extends AppCompatActivity {

    private static String ARGUMENTS;
    private static boolean SUCCESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view){
        doLogin();
    }

    public void createAccount(View view){
        Intent goToCreateActivitiy = new Intent(this, CreateAccountActivity.class);
        startActivity(goToCreateActivitiy);
    }

    private void doLogin(){
        EditText userNameText = (EditText) findViewById(R.id.login);
        String userName = userNameText.getText().toString();
        EditText passText = (EditText) findViewById(R.id.password);
        String pass = passText.getText().toString();

        ARGUMENTS="Login_"+userName+","+pass;

        TryLogin tryLogin = new TryLogin();
        tryLogin.execute();

        //Sets the name of the session with the userName
        DataHolder.getInstance().setUsername(userName);

    }

    private void checkLogin(String message){
        if(message.equals("Login Success"))
            SUCCESS = true;
        else SUCCESS=false;
    }

    private class TryLogin extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("Server unreachable"))
                Toast.makeText(Login.this, s, Toast.LENGTH_LONG).show();
            else {
                if (s.equals("Login Success")) {
                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Login.this, HomeActivity.class);
                    startActivity(intent);
                } else
                    Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverAnswer;
            RequestToServer requestToServer = new RequestToServer();
            try {
                serverAnswer = requestToServer.tradeInformation(ARGUMENTS + "\n");
                //checkLogin(serverAnswer);
                return serverAnswer;
            }
            catch (Exception e){
                e.printStackTrace();
                return "Server unreachable";
            }
        }
    }
}
