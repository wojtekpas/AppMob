package com.gruby.aplikacjemobilne.communication;

import android.os.AsyncTask;

import com.gruby.aplikacjemobilne.entities.Product;
import com.gruby.aplikacjemobilne.entities.User;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class Client extends AsyncTask<String, Void, String> {

    public int status = 0;
    private Product product = null;
    private HttpURLConnection connection = null;
    private ResponseListener responseListener = null;

    public Client(ResponseListener rl)
    {
        responseListener = rl;
    }

    public Client(ResponseListener rl, Product product)
    {
        responseListener = rl;
        this.product = product;
    }

    public boolean lastResponseOk()
    {
        return status >= 200 && status < 300;
    }

    public void TokenGet(String login, String password, String device_id)
    {
        connection = MyConnection.TokenRequestGet(login, password, device_id);
    }

    public void ProductsListRequestGet()
    {
        connection = MyConnection.ProductsListRequestGet(User.token);
    }

    public void ProductsListRequestShare(String product, int user_id, int version)
    {
        connection = MyConnection.ProductRequestShare(User.token, product, user_id, version);
    }

    public void ProductRequestPut(String product, int diff, int version)
    {
        connection = MyConnection.ProductRequestPut(User.token, product, diff, version);
    }

    public void ProductRequestDelete(String product)
    {
        connection = MyConnection.ProductRequestDelete(User.token, product);
    }

    private static String readStream(InputStream in) throws IOException {
        byte[] bytes = new byte[1000];

        StringBuilder x = new StringBuilder();

        int numRead;
        while ((numRead = in.read(bytes)) >= 0) {
            x.append(new String(bytes, 0, numRead));
        }

        return x.toString();
    }

    public String sendRequest() {
        String out = "";

        try {
            String method = connection.getRequestMethod();

            if(method.equals("POST") || method.equals("PUT")) {
                String json = Product.Serialize(product);

                //byte[] byteArray = Encoding.getBytes(json);

                connection.setDoOutput(true);

                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", Integer.toString(json.getBytes().length));

                OutputStream output = connection.getOutputStream();
                output.write(json.getBytes());
                output.close();
            }

            status = connection.getResponseCode();
            System.out.println(status);

            if(lastResponseOk() == false) {
                if(connection != null)
                    connection.disconnect();
                return "status = " + status;
            }
            InputStream in = new BufferedInputStream(connection.getInputStream());
            out = readStream(in);
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }

        return out;
    }

    @Override
    protected String doInBackground(String... params) {
        return sendRequest();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (responseListener != null)
            responseListener.onResponse(s);
    }
}