package com.prokopchuk.orm.demo.entity;

import com.prokopchuk.orm.annotation.Column;
import com.prokopchuk.orm.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Table(name = "products")
@Data
public class Product {

    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
