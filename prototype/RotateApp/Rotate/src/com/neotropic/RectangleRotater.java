/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic;

import java.awt.Rectangle;

/**
 *
 * @author johnyortega
 */
public class RectangleRotater extends Rectangle {
    private double angle;
    private String text;
    private static long idCounter = 0;
    private long id;
    
    public RectangleRotater(int x, int y, int width, int height, double angle, String text) {
        super(x, y, width, height);
        id = idCounter;
        idCounter += 1;
        this.angle = angle;
        this.text = text;
    }
    
    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (this == obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
