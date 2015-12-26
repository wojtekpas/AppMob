package com.gruby.aplikacjemobilne.entities;

import android.content.Intent;
import android.provider.ContactsContract;

import com.gruby.aplikacjemobilne.communication.DatabaseConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Product {

    public int id = 0;
    private String name;
    private int count = 0;
    private int diff = 0;
    public int version = 0;
    public User user;
    public boolean wasCreated = false;
    public boolean wasRemoved = false;

    public Product() {
    }

    public Product(String name) {
        this.name = withoutSpace(name);
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public int getCurrentCount() {
        if(count + diff > 0)
            return (count + diff);
        else
            return 0;
    }

    public void setName(String name) {
        this.name = withoutSpace(name);
    }

    public void setCount(int count) {
        if(count > 0)
            this.count = count;
        else
            this.count = 0;
    }

    public int getDiff(){
        return diff;
    }

    public void uploadDiff(int diff) {
        this.diff += diff;
    }

    public static String withoutSpace(String s)
    {
        return s.replace(' ','_');
    }

    public void putToIntent(Intent intent)
    {
        intent.putExtra("name", name);
        intent.putExtra("count", count);
    }

    public static Product CreateProductFromBundle(Intent intent)
    {
        Product p = new Product();

        p.name = intent.getStringExtra("name");
        p.count = intent.getIntExtra("count", 0);

        return p;
    }

    public static String Serialize(Product p)
    {
        JSONObject jObject;
        JSONObject jProduct;
        try {
            jProduct = new JSONObject();
            jObject = new JSONObject();
            jProduct.put("name", p.name);
            jProduct.put("count", p.count);
            jObject.put(p.name, jProduct.toString());

            return jObject.toString();
        }catch(Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static ArrayList<Product> Deserialize(String json)
    {
        JSONObject jObject;
        Iterator<String> iterator;
        List<String> jProducts;
        JSONObject jProduct;
        ArrayList<Product> productsList = new ArrayList<>();
        try {
            jObject = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
            iterator = jObject.keys();
            jProducts = new ArrayList<>();

            while (iterator.hasNext())
                jProducts.add(iterator.next());

            for (String p_key: jProducts){
                Object o = jObject.get(p_key);
                String pjson = o.toString();

                jProduct = new JSONObject(pjson);
                Product p = new Product();

                p.name = (String)jProduct.get("name");

                Object count_string = jProduct.get("count");
                String s = count_string.toString();
                int i = Integer.parseInt(s);
                p.count = Integer.valueOf(i);

                Object version_string = jProduct.get("version");
                s = version_string.toString();
                i = Integer.parseInt(s);
                p.version = Integer.valueOf(i);

                s = (String) jProduct.get("wasRemoved");

                p.wasRemoved = DatabaseConnection.convertSToB(s);

                productsList.add(p);
            }

            return productsList;
        }catch(Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}