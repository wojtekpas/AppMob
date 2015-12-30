package com.gruby.aplikacjemobilne.entities;

import com.gruby.aplikacjemobilne.activities.LoginActivity;
import com.gruby.aplikacjemobilne.communication.Client;
import com.gruby.aplikacjemobilne.communication.DatabaseConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Gruby on 2015-11-21.
 */
public class User {

    public static User loggedUser = null;
    public static String token = "";
    public static Client client = null;
    public static DatabaseConnection db;

    public int id = 0;
    public String login;
    public String password;
    public String device_id;

    public User() {}

    public User(String login)
    {
        this.login = login;
    }

    public User(int id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public static void Reset(LoginActivity a) {
        loggedUser = null;
        token = "";
        client = new Client(a);
        db = new DatabaseConnection(a);
    }

    public static void Insert(String json) {
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
            System.out.println(jObject.toString());
            Iterator<String> iterator = jObject.keys();
            ArrayList<String> jShares = new ArrayList<>();

            while (iterator.hasNext())
                jShares.add(iterator.next());

            for(String login: jShares){
                String pass = jObject.get(login).toString();
                String device_id = DatabaseConnection.GenerateRandomString(DatabaseConnection.LENGTH_DEVICE_ID);
                if(User.db.getUser(login) == null)
                    User.db.insertUser(0, login, pass, device_id);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
