package com.gruby.aplikacjemobilne.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gruby.aplikacjemobilne.R;
import com.gruby.aplikacjemobilne.entities.Product;
import com.gruby.aplikacjemobilne.entities.User;

/**
 * Created by Gruby on 2015-11-09.
 */
public class AddProductActivity extends Activity {

    EditText newProductET;
    Button addBt;
    Button cancelBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addproduct_layout);

        newProductET = (EditText) findViewById(R.id.newProductET);
        addBt = (Button) findViewById(R.id.addBt);
        cancelBt = (Button) findViewById(R.id.cancelBt);

        addBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Add();
            }
        });
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void Add() {
        String name = newProductET.getText().toString();
        if (name.isEmpty() == false) {
            name = Product.withoutSpace(name);
            if(User.db.isOnProductsList(name)){
                Product product = User.db.getProduct(name);
                if(product.wasRemoved){
                    User.db.updateProduct(product.id, product.getName(),
                            product.getCount(), -product.getCount(), product.version, product.wasCreated, false);
                }
                System.out.println("PRODUCTif = " + product.id);
            }else{
                Product product = new Product(name);
                User.db.insertProduct(0, product.getName(), 0, 0, 0, true, false);
                System.out.println("PRODUCTelse = " + product.id);
            }
            finish();
        }
    }
}