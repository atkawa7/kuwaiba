/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.tools.importer;

import java.util.Comparator;

/**
 *
 * @author daniel
 */
public class Item {
    private String name;
    private String lat;
    private String lon;
    private String coordinate;

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
    
}

class LongComparator implements Comparator<Item>
{

    @Override
    public int compare(Item o1, Item o2) {
        float f1 = Float.parseFloat(o1.getLon());
        float f2 = Float.parseFloat(o2.getLon());
        if (f1 > f2){
            return 1;
        } else if (f1 < f2){
            return -1;
        } else {
            return 0;
        }
    }
    
}
