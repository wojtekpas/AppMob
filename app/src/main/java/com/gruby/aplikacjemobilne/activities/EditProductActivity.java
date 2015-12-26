package com.gruby.aplikacjemobilne.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gruby.aplikacjemobilne.communication.Client;
import com.gruby.aplikacjemobilne.entities.Product;
import com.gruby.aplikacjemobilne.R;
import com.gruby.aplikacjemobilne.communication.ResponseListener;
import com.gruby.aplikacjemobilne.entities.User;

/**
 * Created by Gruby on 2015-11-09.
 */
public class EditProductActivity extends Activity {

    EditText chosenProductET;
    EditText curCountET;
    EditText diffCountET;
    Button updateBt;
    Button cancelBt;
    Button removeBt;
    Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editproduct_layout);

        chosenProductET = (EditText) findViewById(R.id.chosenProductET);
        curCountET = (EditText) findViewById(R.id.curCountET);
        diffCountET  = (EditText) findViewById(R.id.diffCountET);

        updateBt = (Button) findViewById(R.id.updateBt);
        cancelBt = (Button) findViewById(R.id.cancelBt);
        removeBt = (Button) findViewById(R.id.removeBt);

        if (savedInstanceState == null) {
            Product tmp = Product.CreateProductFromBundle(getIntent());
            product = User.db.getProduct(tmp.getName());
        }

        chosenProductET.setText(product.getName());
        curCountET.setText("" + product.getCurrentCount());
        chosenProductET.setEnabled(false);
        curCountET.setEnabled(false);

        updateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Update();
            }
        });
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        removeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Remove();
            }
        });
    }

    public void Update()
    {
        int diff = 0;

        if(diffCountET.getText() != null && diffCountET.getText().equals("") == false){
            diff = Integer.parseInt(diffCountET.getText().toString());
        }

        product.uploadDiff(diff);

        User.db.updateProduct(product);
        finish();
    }

    public void Remove()
    {
        product.wasRemoved = true;
        User.db.updateProduct(product);
        finish();
    }
}