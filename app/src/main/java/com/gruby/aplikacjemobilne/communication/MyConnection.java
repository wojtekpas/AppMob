package com.gruby.aplikacjemobilne.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Gruby
 */
public class MyConnection {

    public static String ip = "http://10.0.2.2:5000/";
    public static String baseUrl = "http://10.0.2.2:5000/token/";

    public static HttpURLConnection TokenRequestGet(String user_id, String password, String device_id)
    {
        try{
            URL url = new URL(ip + "users/" + user_id + "/password/" + password + "/devices/" + device_id);
            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            return connection;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static HttpURLConnection ProductsListRequestGet(String token)
    {
        try{
            URL url = new URL(baseUrl+token+"/products");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            return connection;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static HttpURLConnection ProductRequestPut(String token, String id, int count, int version)
    {
        try{
            URL url = new URL(baseUrl+token+"/products/"+id+"/diff/"+count+"/version/"+version);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("PUT");
            return connection;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static HttpURLConnection ProductRequestDelete(String token, String id)
    {
        try{

            URL url = new URL(baseUrl+token+"/products/"+id);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod("DELETE");

            return connection;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

