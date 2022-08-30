package com.prokopchuk.orm.demo;

import com.prokopchuk.orm.Session;
import com.prokopchuk.orm.SessionFactory;
import com.prokopchuk.orm.demo.entity.Product;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;

public class Demo {
    public static void main(String[] args) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres?currentSchema=test");
        dataSource.setUser("postgres");
        dataSource.setPassword("password");

        SessionFactory sessionFactory = new SessionFactory(dataSource);

        Session session = sessionFactory.creatSession();

        Product product1 = session.find(Product.class, 1);
        System.out.println(product1);

        final Product product2 = session.find(Product.class, 1);

        System.out.println(product2);

    }
}
