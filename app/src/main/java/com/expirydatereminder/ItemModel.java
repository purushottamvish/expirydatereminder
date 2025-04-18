package com.expirydatereminder;

public class ItemModel {
    private String item, dateOfYear ,category;
    private int month, year, id, date;

    public String getDateOfYear() {
        return dateOfYear;
    }

    public void setDateOfYear(String dateOfYear) {
        this.dateOfYear = dateOfYear;
    }

    ItemModel(String i, String m,String cat){
        this.item = i;
        this.dateOfYear = m;
        this.category = cat;
    }
    public ItemModel() {
        this.item = null;
        this.month = 0;
        this.year = 0;
        this.category = null;
        this.date = 0;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    ItemModel(String i, int m, int y, int d, String cat){
        this.item = i;
        this.month = m;
        this.year = y;
        this.date = d;
        this.category = cat;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
