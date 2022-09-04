package com.prokopchuk.orm.demo;

import com.prokopchuk.orm.SessionFactory;
import com.prokopchuk.orm.demo.entity.Product;
import org.postgresql.ds.PGSimpleDataSource;

import java.math.BigDecimal;

public class Demo {
    public static void main(String[] args) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres?currentSchema=test");
        dataSource.setUser("postgres");
        dataSource.setPassword("password");

        SessionFactory sessionFactory = new SessionFactory(dataSource);

        try (var session = sessionFactory.creatSession()) {
            Product product1 = session.find(Product.class, 1);
            System.out.println(product1);

            product1.setName("ps3v");
            product1.setPrice(BigDecimal.valueOf(600));
        }

        try (var session = sessionFactory.creatSession();) {
            Product updatedProduct = session.find(Product.class, 1);
            System.out.println(updatedProduct);
        }
    }
}
