package com.helloworld.myapplication;

import java.io.Serializable;

public class UnoCardClass implements Serializable {

    String color;
    int number;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "UnoCardClass{" +
                "color='" + color + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
