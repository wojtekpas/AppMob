package com.gruby.aplikacjemobilne.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Gruby on 2015-12-27.
 */
public class Share {
    public int id;
    public Product product;
    public User user;
    public boolean isNew;

    public Share(int id, int product_id, int user_id, boolean isNew) {
        this.id = id;
        product = User.db.getProduct(product_id);
        user = User.db.getUser(user_id);
        this.isNew = isNew;
    }

    public static ArrayList<String> Deserialize(String json) throws JSONException {
        JSONObject jObject = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
        Iterator<String> iterator = jObject.keys();
        ArrayList<String> jShares = new ArrayList<>();

        while (iterator.hasNext())
            jShares.add(iterator.next());

        return jShares;
    }
}
