package com.gruby.aplikacjemobilne.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.gruby.aplikacjemobilne.communication.Client;
import com.gruby.aplikacjemobilne.entities.Product;
import com.gruby.aplikacjemobilne.R;
import com.gruby.aplikacjemobilne.communication.ResponseListener;
import com.gruby.aplikacjemobilne.entities.Share;
import com.gruby.aplikacjemobilne.entities.User;

import java.util.ArrayList;

/**
 * Created by Gruby on 2015-11-09.
 */
public class ProductActivity extends Activity implements ResponseListener {

    Button addProductBt;
    Button syncBt;
    Button logoutBt;
    ListView productsLV;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    private ArrayList<Product> products;
    int numberOfProducts = 0;
    int numberOfSyncProducts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.productslist_layout);

        addProductBt = (Button) findViewById(R.id.addProductBt);
        syncBt = (Button) findViewById(R.id.syncBt);
        logoutBt = (Button) findViewById(R.id.logoutBt);
        productsLV = (ListView) findViewById(R.id.productsLV);

        listItems = new ArrayList<>();
        products = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        productsLV.setAdapter(adapter);

        addProductBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddProduct();
            }
        });

        syncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sync();
            }
        });

        logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout();
            }
        });

        productsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product p = products.get(position);
                EditProduct(p);
            }
        });

        RefreshProductsList();
    }

    public void RefreshProductsList()
    {
        ArrayList<Product> productsInDb = User.db.getProductsListForLoggedUser();
        products = new ArrayList<>();
        for(Product p: productsInDb) {
            if(p.wasRemoved == false)
                products.add(p);
        }
        SetProductsList();
    }

    public void SetProductsList()
    {
        adapter.clear();

        for(Product p: products) {
            adapter.add(p.getName() + " - " + p.getCurrentCount());
        }

        adapter.notifyDataSetChanged();
    }

    public void AddProduct()
    {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivityForResult(intent, 0);
    }

    private void EditProduct(Product p)
    {
        Intent intent = new Intent(this, EditProductActivity.class);
        p.putToIntent(intent);
        startActivityForResult(intent, 0);
    }

    private void Sync()
    {
        UpdateDuringSync();
    }

    private void UpdateDuringSync(){
        numberOfProducts = User.db.getProductsListForLoggedUser().size();
        numberOfSyncProducts = 0;

        ArrayList<Share> newSharesListForLoggedUser = User.db.getNewSharesListForLoggedUser();

        numberOfProducts += newSharesListForLoggedUser.size();

        if(numberOfProducts == 0) {
            UsersDownload();
            return;
        }

        for(Product p: User.db.getProductsListForLoggedUser()) {
            if(p.wasRemoved == false && p.wasCreated == false && p.getDiff() == 0) {
                numberOfSyncProducts++;
            }else if(p.wasRemoved) {
                User.client = new Client(this, p);
                User.client.ProductRequestDelete(p.getName());
                User.client.execute();
            }
            else {
                User.client = new Client(this, p);
                User.client.ProductRequestPut(p.getName(), p.getDiff(), p.version);
                User.client.execute();
            }
        }

        for(Share s: newSharesListForLoggedUser){
            User.client = new Client(this, s.product);
            User.client.ProductRequestShare(s.product.getName(),s.user.login, s.product.version);
            User.client.execute();
        }

        if(numberOfProducts == numberOfSyncProducts) {
            UsersDownload();
            return;
        }
    }

    private void DownloadDuringSync(){
        if(User.db.getProductsListForLoggedUser().size() > 0) {
            for (Product p : User.db.getProductsListForLoggedUser()) {
                User.db.deleteProduct(p);
            }
        }
        User.client = new Client(this);
        User.client.ProductsListRequestGet();
        User.client.execute();
    }

    private void Logout()
    {
        User.Reset(null);
        finish();
    }

    private void UsersDownload()
    {
        User.client = new Client(this);
        User.client.UsersListRequestGet();
        User.client.execute();
    }

    @Override
    public void onResponse(String data) {
        numberOfSyncProducts++;
        if(numberOfSyncProducts < numberOfProducts){
            return;
        }
        if(numberOfSyncProducts == numberOfProducts){
            UsersDownload();
            return;
        }
        if(numberOfSyncProducts == numberOfProducts + 1){
            User.Insert(data);
            DownloadDuringSync();
            return;
        }

        ArrayList<Product> productsOnServer = Product.Deserialize(data);
        products = new ArrayList<>();
        if(productsOnServer.size() > 0) {
            for (Product p : productsOnServer) {
                if(p.wasRemoved == false) {
                    User.db.insertProduct(p);
                    Product tmpProduct = User.db.getProduct(p.getName());
                    p.id = tmpProduct.id;
                    p.insertShares();
                }
            }
        }

        RefreshProductsList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RefreshProductsList();
    }
}
