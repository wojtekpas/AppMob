package com.gruby.aplikacjemobilne.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gruby.aplikacjemobilne.R;
import com.gruby.aplikacjemobilne.communication.DatabaseConnection;
import com.gruby.aplikacjemobilne.communication.ResponseListener;
import com.gruby.aplikacjemobilne.entities.Product;
import com.gruby.aplikacjemobilne.entities.User;

import org.json.JSONObject;

public class LoginActivity extends Activity implements ResponseListener {

    EditText loginET;
    EditText passwordET;
    Button loginBt;
    TextView infoTV;
    private String login = "";
    private String password = "";
    private String encodedPass = "";
    private String device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        loginET = (EditText) findViewById(R.id.loginLoginET);
        passwordET = (EditText) findViewById(R.id.loginPasswordET);
        loginBt = (Button) findViewById(R.id.loginLoginBt);
        infoTV = (TextView) findViewById(R.id.loginInfoTV);

        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void login() {
        infoTV.setText("");
        User.Reset(this);

        login = loginET.getText().toString();
        password = passwordET.getText().toString();

        if(login.isEmpty() || password.isEmpty() || login.equals(Product.withoutSpace(login)) == false)
        {
            infoTV.setText("wpisz login i hasło");
            return;
        }

        encodedPass = Base64.encodeToString(password.getBytes(), 0);
        encodedPass = encodedPass.substring(0, encodedPass.length() - 1);

        User userDB = User.db.getUser(login);
        if(userDB != null) {
            device_id = userDB.device_id;
            User.loggedUser = User.db.getUser(login);

            if(userDB.equals(password))
                startProductActivity();
            else {
                infoTV.setText("nieudane logowanie");
            }
        }
        else
            device_id = DatabaseConnection.GenerateRandomString(DatabaseConnection.LENGTH_DEVICE_ID);

        User.client.TokenGet(login, encodedPass, device_id);
        User.client.execute();
    }

    @Override
    public void onResponse(String data) {
        if(User.client.lastResponseOk()) {
            User.token = DeserializeToken(data);

            User userDB = User.db.getUser(login);

            if(userDB == null) {
                User.db.insertUser(0, login, encodedPass, device_id);
            }

            User.loggedUser = User.db.getUser(login);
            infoTV.setText("");
            startProductActivity();
        }else{
            infoTV.setText("nieudane logowanie");
        }
    }

    public String DeserializeToken(String json) {

        try {
            JSONObject jObject = new JSONObject(json);
            String token = (String)jObject.get("token");
            return token;
        }catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void startProductActivity() {
        Intent intent = new Intent(this, ProductActivity.class);
        startActivity(intent);
    }
}
