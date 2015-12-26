package com.gruby.aplikacjemobilne.entities;

import com.gruby.aplikacjemobilne.activities.LoginActivity;
import com.gruby.aplikacjemobilne.communication.Client;
import com.gruby.aplikacjemobilne.communication.DatabaseConnection;

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
}
