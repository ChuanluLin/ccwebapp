package com.csye6225.demo.pojo;
import javax.persistence.Embeddable;

@Embeddable
public class OrderedList {
    private int position;
    private String items;
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public String getItems() {
        return items;
    }
    public void setItems(String items) {
        this.items = items;
    }
}
