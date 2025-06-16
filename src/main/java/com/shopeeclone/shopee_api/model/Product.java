package com.shopeeclone.shopee_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(name = "name")
    private String name;

    //@Column(name = "description")
    private String description;

    //@Column(name = "price")
    private double price;

    public Product(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Double getPrice ()
    {
        return  price;
    }

    public void setPrice (Double price)
    {
        this.price = price;
    }
}
