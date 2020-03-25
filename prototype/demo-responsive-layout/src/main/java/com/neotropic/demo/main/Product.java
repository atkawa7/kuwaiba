/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.demo.main;

/**
 * Product Entity
 *
 * @author Orlando Paz
 */
public class Product {

    private String name;
    private String company;
    private int quantity;
    private double price;
    private String year;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Product(String name, String company, int quantity, double price, String year) {
        this.name = name;
        this.company = company;
        this.quantity = quantity;
        this.price = price;
        this.year = year;
    }

}
