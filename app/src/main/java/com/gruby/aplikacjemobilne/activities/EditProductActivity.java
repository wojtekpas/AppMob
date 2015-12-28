package com.gruby.aplikacjemobilne.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.gruby.aplikacjemobilne.R;
import com.gruby.aplikacjemobilne.entities.Product;
import com.gruby.aplikacjemobilne.entities.User;

import java.util.ArrayList;

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

    ListView usersLV;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    ArrayList<User> users;
    ArrayList<User> selectedUsers;

    public static final String token = "---";

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

        usersLV = (ListView) findViewById(R.id.usersLV);

        listItems = new ArrayList<>();
        users = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);

        usersLV.setAdapter(adapter);

        selectedUsers = new ArrayList<>();
        System.out.println("product: " + product + " id: " + product.id + " name: " + product.getName());
        users = User.db.getPossibleUsers(product);

        for(User u: users){
            listItems.add(u.login);
        }

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

        usersLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelectUser(position);
            }
        });
    }

    public void Update() {
        int diff = 0;

        if(diffCountET.getText() != null && diffCountET.getText().equals("") == false){
            diff = Integer.parseInt(diffCountET.getText().toString());
        }

        product.uploadDiff(diff);

        User.db.updateProduct(product);

        for(User u: selectedUsers){
            User.db.insertShares(product, u);
        }

        finish();
    }

    public void Remove() {
        product.wasRemoved = true;
        User.db.updateProduct(product);
        finish();
    }

    public void SelectUser(int position){
        User u = users.get(position);

        if(selectedUsers.contains(u)){
            listItems.set(position, u.login);
            selectedUsers.remove(u);
        } else {
            listItems.set(position, token + u.login + token);
            selectedUsers.add(u);
        }

        adapter.notifyDataSetChanged();
    }
}