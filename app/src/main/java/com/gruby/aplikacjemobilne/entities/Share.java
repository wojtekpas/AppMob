package com.gruby.aplikacjemobilne.entities;

/**
 * Created by Gruby on 2015-12-27.
 */
public class Share {
    public int id;
    public Product product;
    public User user;

    public Share(int id, Product p, User u) {
        this.id = id;
        product = p;
        user = u;
    }

    public Share(int id, int product_id, int user_id) {
        this.id = id;
        product = User.db.getProduct(product_id);
        user = User.db.getUser(user_id);
        System.out.println("konstruktor\nid = " + id);
        System.out.println(product_id);
        System.out.println(user_id);
        System.out.println(product.id);
        System.out.println(user.id);
    }
}
