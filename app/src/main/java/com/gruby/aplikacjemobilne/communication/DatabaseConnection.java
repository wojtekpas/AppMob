package com.gruby.aplikacjemobilne.communication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gruby.aplikacjemobilne.entities.Product;
import com.gruby.aplikacjemobilne.entities.Share;
import com.gruby.aplikacjemobilne.entities.User;

import java.util.ArrayList;
import java.util.Random;

public class DatabaseConnection extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDbver255.db";

    public static final String USERS_TABLE_NAME = "users";
    public static final String USERS_COLUMN_ID = "id";
    public static final String USERS_COLUMN_LOGIN = "login";
    public static final String USERS_COLUMN_PASSWORD = "password";
    public static final String USERS_COLUMN_DEVICE_ID = "device_id";
    public static final int LENGTH_DEVICE_ID = 32;

    public static final String PRODUCTS_TABLE_NAME = "products";
    public static final String PRODUCTS_COLUMN_ID = "id";
    public static final String PRODUCTS_COLUMN_NAME = "name";
    public static final String PRODUCTS_COLUMN_COUNT = "count";
    public static final String PRODUCTS_COLUMN_DIFF = "diff";
    public static final String PRODUCTS_COLUMN_VERSION = "version";
    public static final String PRODUCTS_COLUMN_WAS_CREATED = "was_created";
    public static final String PRODUCTS_COLUMN_WAS_REMOVED = "was_removed";
    public static final String PRODUCTS_COLUMN_USER_ID = "user_id";

    public static final String SHARES_TABLE_NAME = "shares";
    public static final String SHARES_COLUMN_ID = "id";
    public static final String SHARES_COLUMN_PRODUCT_ID = "product_id";
    public static final String SHARES_COLUMN_USER_ID = "user_id";
    public static final String SHARES_COLUMN_IS_NEW = "is_new";

    public DatabaseConnection(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + USERS_TABLE_NAME +
                        "( " + USERS_COLUMN_ID + " integer primary key," +
                        USERS_COLUMN_LOGIN + " text," +
                        USERS_COLUMN_PASSWORD + " text," +
                        USERS_COLUMN_DEVICE_ID + " text)"
        );

        db.execSQL(
                "create table " + PRODUCTS_TABLE_NAME +
                        "( " + PRODUCTS_COLUMN_ID + " integer primary key," +
                        PRODUCTS_COLUMN_NAME + " text," +
                        PRODUCTS_COLUMN_COUNT + " integer," +
                        PRODUCTS_COLUMN_DIFF + " integer," +
                        PRODUCTS_COLUMN_VERSION + " integer," +
                        PRODUCTS_COLUMN_USER_ID + " integer," +
                        PRODUCTS_COLUMN_WAS_CREATED + " text," +
                        PRODUCTS_COLUMN_WAS_REMOVED + " text)"
        );

        db.execSQL(
                "create table " + SHARES_TABLE_NAME +
                        "( " + SHARES_COLUMN_ID + " integer primary key," +
                        SHARES_COLUMN_PRODUCT_ID + " integer," +
                        SHARES_COLUMN_USER_ID + " integer," +
                        SHARES_COLUMN_IS_NEW + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SHARES_TABLE_NAME);
        onCreate(db);
    }

    public void insertProduct (Product p)
    {
        insertProduct(p.id, p.getName(), p.getCount(), p.getDiff(), p.version, p.wasCreated, p.wasRemoved);
    }

    public void updateProduct (Product p)
    {
        updateProduct(p.id, p.getName(), p.getCount(), p.getDiff(), p.version, p.wasCreated, p.wasRemoved);
    }

    public void deleteProduct (Product p)
    {
        deleteProduct(p.id);
    }

    public void insertUser (int id, String login, String password, String device_id)
    {
        if(id == 0)
            id = getMaxUserId() + 1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERS_COLUMN_ID, id);
        contentValues.put(USERS_COLUMN_LOGIN, login);
        contentValues.put(USERS_COLUMN_PASSWORD, password);
        contentValues.put(USERS_COLUMN_DEVICE_ID, device_id);
        db.insert(USERS_TABLE_NAME, null, contentValues);
    }

    public void insertProduct (int id, String name, int count, int diff, int version, boolean wasCreated, boolean wasRemoved)
    {
        if(isOnProductsList(name))
            return;

        if(id == 0)
            id = getMaxProductId() + 1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PRODUCTS_COLUMN_ID, id);
        contentValues.put(PRODUCTS_COLUMN_NAME, name);
        contentValues.put(PRODUCTS_COLUMN_COUNT, count);
        contentValues.put(PRODUCTS_COLUMN_DIFF, diff);
        contentValues.put(PRODUCTS_COLUMN_VERSION, version);
        contentValues.put(PRODUCTS_COLUMN_WAS_CREATED, convertBToS(wasCreated));
        contentValues.put(PRODUCTS_COLUMN_WAS_REMOVED, convertBToS(wasRemoved));
        contentValues.put(PRODUCTS_COLUMN_USER_ID, User.loggedUser.id);
        db.insert(PRODUCTS_TABLE_NAME, null, contentValues);
    }

    public void insertShares (Product product, User user, boolean isNew) {
        if(product == null || user == null)
            return;

        insertShares(0, product.id, user.id, isNew);
    }

    public void insertShares (int id, int product_id, int user_id, boolean isNew) {
        if(id == 0)
            id = getMaxSharesId() + 1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SHARES_COLUMN_ID, id);
        contentValues.put(SHARES_COLUMN_PRODUCT_ID, product_id);
        contentValues.put(SHARES_COLUMN_USER_ID, user_id);
        contentValues.put(SHARES_COLUMN_IS_NEW, convertBToS(isNew));
        db.insert(SHARES_TABLE_NAME, null, contentValues);
    }

    public void updateProduct (int id, String name, int count, int diff, int version, boolean wasCreated, boolean wasRemoved)
    {
        if(wasRemoved)
            deleteShares(id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PRODUCTS_COLUMN_ID, id);
        contentValues.put(PRODUCTS_COLUMN_NAME, name);
        contentValues.put(PRODUCTS_COLUMN_COUNT, count);
        contentValues.put(PRODUCTS_COLUMN_DIFF, diff);
        contentValues.put(PRODUCTS_COLUMN_VERSION, version);
        contentValues.put(PRODUCTS_COLUMN_WAS_CREATED, convertBToS(wasCreated));
        contentValues.put(PRODUCTS_COLUMN_WAS_REMOVED, convertBToS(wasRemoved));
        contentValues.put(PRODUCTS_COLUMN_USER_ID, User.loggedUser.id);
        db.update(PRODUCTS_TABLE_NAME, contentValues, PRODUCTS_COLUMN_ID + " = ? ", new String[]{Integer.toString(id)});
    }

    public void deleteProduct (int id) {
        deleteShares(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PRODUCTS_TABLE_NAME,
                PRODUCTS_COLUMN_ID + "= ? ",
                new String[]{Integer.toString(id)});
    }

    public void deleteShares(int pid) {
        int c1 = getSharesList().size();
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SHARES_TABLE_NAME,
                SHARES_COLUMN_PRODUCT_ID + "= ? ",
                new String[]{Integer.toString(pid)});
        int c2 = getSharesList().size();
        int diff = c2 - c1;
    }

    public ArrayList<User> getUsers()
    {
        ArrayList<User> allUsers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + USERS_TABLE_NAME, null);
        res.moveToFirst();

        User u;

        while(res.isAfterLast() == false) {
            u = new User();
            u.id = Integer.parseInt(res.getString(res.getColumnIndex(USERS_COLUMN_ID)));
            u.login = res.getString(res.getColumnIndex(USERS_COLUMN_LOGIN));
            u.password = res.getString(res.getColumnIndex(USERS_COLUMN_PASSWORD));
            u.device_id = res.getString(res.getColumnIndex(USERS_COLUMN_DEVICE_ID));

            allUsers.add(u);
            res.moveToNext();
        }

        return allUsers;
    }

    public User getUser(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + USERS_TABLE_NAME +
                " where " + USERS_COLUMN_ID + " = " + id, null);
        res.moveToFirst();

        if(res.isAfterLast())
            return null;

        User u = new User();
        u.id = Integer.parseInt(res.getString(res.getColumnIndex(USERS_COLUMN_ID)));
        u.login = res.getString(res.getColumnIndex(USERS_COLUMN_LOGIN));
        u.password = res.getString(res.getColumnIndex(USERS_COLUMN_PASSWORD));
        u.device_id = res.getString(res.getColumnIndex(USERS_COLUMN_DEVICE_ID));

        return u;
    }

    public User getUser(String login)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + USERS_TABLE_NAME +
                " where " + USERS_COLUMN_LOGIN + " = " + getQueryArg(login), null);
        res.moveToFirst();

        if(res.isAfterLast())
            return null;

        User u = new User();
        u.id = Integer.parseInt(res.getString(res.getColumnIndex(USERS_COLUMN_ID)));
        u.login = res.getString(res.getColumnIndex(USERS_COLUMN_LOGIN));
        u.password = res.getString(res.getColumnIndex(USERS_COLUMN_PASSWORD));
        u.device_id = res.getString(res.getColumnIndex(USERS_COLUMN_DEVICE_ID));

        return u;
    }

    public int getMaxUserId(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + USERS_TABLE_NAME + " order by " + USERS_COLUMN_ID + " DESC", null );
        res.moveToFirst();

        if(res.isAfterLast())
            return 0;

        return Integer.parseInt(res.getString(res.getColumnIndex(USERS_COLUMN_ID)));
    }

    public int getMaxProductId(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + PRODUCTS_TABLE_NAME + " order by " + PRODUCTS_COLUMN_ID + " DESC", null);
        res.moveToFirst();

        if(res.isAfterLast())
            return 0;

        return Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_ID)));
    }

    public int getMaxSharesId(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + SHARES_TABLE_NAME + " order by " + SHARES_COLUMN_ID + " DESC", null);
        res.moveToFirst();

        if(res.isAfterLast())
            return 0;

        return Integer.parseInt(res.getString(res.getColumnIndex(SHARES_COLUMN_ID)));
    }

    public ArrayList<Share> getSharesList()
    {
        ArrayList<Share> sharesList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + SHARES_TABLE_NAME, null);
        res.moveToFirst();

        Share s;

        while(res.isAfterLast() == false) {

            int id = Integer.parseInt(res.getString(res.getColumnIndex(SHARES_COLUMN_ID)));
            int pid = Integer.parseInt(res.getString(res.getColumnIndex(SHARES_COLUMN_PRODUCT_ID)));
            int uid = Integer.parseInt(res.getString(res.getColumnIndex(SHARES_COLUMN_USER_ID)));
            boolean isNew = convertSToB(res.getString(res.getColumnIndex(SHARES_COLUMN_IS_NEW)));

            s = new Share(id, pid, uid, isNew);
            sharesList.add(s);
            res.moveToNext();
        }

        return sharesList;
    }

    public ArrayList<Share> getSharesList(Product p) {

        ArrayList<Share> shares = new ArrayList<>();
        ArrayList<Share> sharesList = getSharesList();

        for(Share s: sharesList) {
            if(s.product.id == p.id && shares.contains(s) == false)
                shares.add(s);
        }

        return shares;
    }

    public ArrayList<User> getPossibleUsers(Product p){
        ArrayList<User> users = getUsers();
        ArrayList<Share> shares = getSharesList(p);
        ArrayList<User> usersToRemove = new ArrayList<>();

        users.remove(User.loggedUser);

        for(User u: users) {
            for(Share s: shares){
                if (s.user.id == u.id){
                    if(usersToRemove.contains(u) == false){
                        usersToRemove.add(u);
                    }
                }
            }

            if (User.loggedUser.id == u.id){
                if(usersToRemove.contains(u) == false){
                    usersToRemove.add(u);
                }
            }
        }

        for(User u: usersToRemove) {
            users.remove(u);
        }

        return users;
    }

    public ArrayList<Product> getAllProductsList()
    {
        ArrayList<Product> productsList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + PRODUCTS_TABLE_NAME, null);
        res.moveToFirst();

        Product p;

        while(res.isAfterLast() == false) {
            p = new Product();
            p.id = Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_ID)));
            p.setName(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_NAME)));
            p.setCount(Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_COUNT))));
            p.uploadDiff(Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_DIFF))));
            p.version = Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_VERSION)));
            p.wasCreated = convertSToB(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_WAS_CREATED)));
            p.wasRemoved = convertSToB(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_WAS_REMOVED)));
            p.user = User.loggedUser;
            productsList.add(p);
            res.moveToNext();
        }
        return productsList;
    }

    public ArrayList<Product> getProductsListForLoggedUser()
    {
        ArrayList<Product> productsList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + PRODUCTS_TABLE_NAME +
                " where " + PRODUCTS_COLUMN_USER_ID + " = " + getQueryArg(User.loggedUser.id), null);
        res.moveToFirst();

        Product p;

        while(res.isAfterLast() == false) {
            p = new Product();
            p.id = Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_ID)));
            p.setName(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_NAME)));
            p.setCount(Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_COUNT))));
            p.uploadDiff(Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_DIFF))));
            p.version = Integer.parseInt(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_VERSION)));
            p.wasCreated = convertSToB(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_WAS_CREATED)));
            p.wasRemoved = convertSToB(res.getString(res.getColumnIndex(PRODUCTS_COLUMN_WAS_REMOVED)));
            p.user = User.loggedUser;
            productsList.add(p);
            res.moveToNext();
        }
        return productsList;
    }

    public ArrayList<Share> getNewSharesListForLoggedUser()
    {;
        ArrayList<Share> newSharesList = new ArrayList<>();

        for(Product p: getProductsListForLoggedUser()){
            for(Share s: getSharesList(p)){
                if(s.isNew)
                    newSharesList.add(s);
            }
        }
        return newSharesList;
    }

    public Product getProduct(int id){
        for (Product p : getAllProductsList()){
            if(id == p.id)
                return p;
        }
        System.out.println("WARNING: didn't find product with id = " + id);
        return null;
    }

    public Product getProduct(String name){
        for (Product p : getProductsListForLoggedUser()){
            if(name.equals(p.getName()))
                return p;
        }
        return null;
    }

    public boolean isOnProductsList(String name){
        for (Product p : getProductsListForLoggedUser()){
            if(name.equals(p.getName()))
                return true;
        }
        return false;
    }

    public String getQueryArg(int arg)
    {
        return getQueryArg(Integer.toString(arg));
    }

    public String getQueryArg(String arg)
    {
        return "'" + arg + "'";
    }

    public static String convertBToS(Boolean b)
    {
        if(b)
            return "true";
        return "false";
    }

    public static Boolean convertSToB(String s) {
        return s.equals("true");
    }

    public static String GenerateRandomString(int length)
    {
        Random r = new Random();
        int bytes[] = new int [length];
        StringBuilder s = new StringBuilder();

        for(int i = 0; i < length; i++) {
            bytes[i] = r.nextInt(10);
            s.append(bytes[i]);
        }

        return s.toString();
    }

    public void printTables()
    {
        printUsers();
        printProducts();
    }

    public void printUsers(){
        System.out.println("---users---");
        for(User u: getUsers()){
            System.out.println("id: " + u.id + " - login : " + u.login);
        }
    }

    public void printProducts(){
        System.out.println("---products---");
        for(Product p: getAllProductsList()){
            System.out.println("id: " + p.id + " - name : " + p.getName());
        }
    }
}
