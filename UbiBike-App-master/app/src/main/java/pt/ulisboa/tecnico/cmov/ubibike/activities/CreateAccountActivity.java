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

public class CreateAccountActivity extends AppCompatActivity {

    private static String ARGUMENTS;
    private String serverAnswer;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
    }

    public void createAccount(View view){
        EditText userNameText = (EditText) findViewById(R.id.userName);
        this.userName = userNameText.getText().toString();
        EditText emailText = (EditText) findViewById(R.id.email);
        String email = emailText.getText().toString();
        EditText passText = (EditText) findViewById(R.id.password);
        String pass = passText.getText().toString();

        ARGUMENTS = "Create Account_" + userName + "," + email + "," + pass;

        CreateAccountTask createAccountTask = new CreateAccountTask();
        createAccountTask.execute();

       /* Intent createAccountIntent = new Intent(this, HomeActivity.class);
        startActivity(createAccountIntent); */
    }

    private class CreateAccountTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("Server unreachable")){
                Toast.makeText(CreateAccountActivity.this, s, Toast.LENGTH_LONG).show();
            }else {
                if (serverAnswer.equals("OK")) {
                    DataHolder.getInstance().setUsername(userName);
                    Intent createAccountIntent = new Intent(CreateAccountActivity.this, HomeActivity.class);
                    startActivity(createAccountIntent);
                } else
                    Toast.makeText(CreateAccountActivity.this, serverAnswer, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            RequestToServer requestToServer = new RequestToServer();
            try {
                serverAnswer = requestToServer.tradeInformation(ARGUMENTS + "\n");
            }
            catch (Exception e){
                return "Server unreachable";
            }
            return serverAnswer;
        }
    }
}
