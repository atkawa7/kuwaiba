package com.vaadin.tapio.googlemaps.client.overlays;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.tapio.googlemaps.client.LatLon;

/**
 * A class representing a polyline (a line consisting of multiple points)
 * overlay of Google Maps.
 */
public class GoogleMapPolyline implements Serializable {

    private static final long serialVersionUID = 646346543563L;

    private static long idCounter = 0;

    private long id;
    
    private String caption;

    private List<LatLon> coordinates = new ArrayList<>();

    private String strokeColor = "#000000";

    private double strokeOpacity = 1.0;

    private int strokeWeight = 1;

    private int zIndex = 0;

    private boolean geodesic = false;
    
    private boolean visible = true;

    public GoogleMapPolyline() {
        id = idCounter;
        idCounter++;
    }

    public GoogleMapPolyline(String caption, List<LatLon> coordinates) {
        this();
        this.coordinates = coordinates;
        this.caption = caption;
    }
    
    public GoogleMapPolyline(List<LatLon> coordinates, String strokeColor,
        double strokeOpacity, int strokeWeight) {
        this(null, coordinates);
        this.strokeColor = strokeColor;
        this.strokeOpacity = strokeOpacity;
        this.strokeWeight = strokeWeight;
    }

    public GoogleMapPolyline(String caption, List<LatLon> coordinates, String strokeColor,
        double strokeOpacity, int strokeWeight) {
        this(caption, coordinates);
        this.strokeColor = strokeColor;
        this.strokeOpacity = strokeOpacity;
        this.strokeWeight = strokeWeight;
    }

    /**
     * Returns the coordinates of the polyline.
     *
     * @return coordinates
     */
    public List<LatLon> getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the coordinates of the polyline.
     *
     * @param coordinates the new coordinates
     */
    public void setCoordinates(List<LatLon> coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Returns the stroke color of the polyline.
     *
     * @return the stroke color
     */
    public String getStrokeColor() {
        return strokeColor;
    }

    /**
     * Sets the stroke color of the polyline.
     *
     * @param strokeColor The new stroke color.
     */
    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    /**
     * Returns the stroke opacity of the polyline.
     *
     * @return the stroke opacity
     */
    public double getStrokeOpacity() {
        return strokeOpacity;
    }

    /**
     * Sets the stroke opacity of the polyline.
     *
     * @param strokeOpacity The new stroke opacity.
     */
    public void setStrokeOpacity(double strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
    }

    /**
     * Returns the stroke weight of the polyline.
     *
     * @return the stroke weight
     */
    public int getStrokeWeight() {
        return strokeWeight;
    }

    /**
     * Sets the stroke weight of the polyline.
     *
     * @param strokeWeight The new stroke weight.
     */
    public void setStrokeWeight(int strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    /**
     * Returns the z index compared to other polyline.
     *
     * @return the z index
     */
    public int getzIndex() {
        return zIndex;
    }

    /**
     * Returns the polyline caption
     * @return 
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the polyline caption
     * @param caption The caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * Sets the z index compared to other polyline.
     *
     * @param zIndex the new z index
     */
    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * Checks if the polyline is geodesic.
     *
     * @return true, if it is geodesic
     */
    public boolean isGeodesic() {
        return geodesic;
    }

    /**
     * Enables/disables geodesicity of the polyline.
     *
     * @param geodesic Set true to enable geodesicity.
     */
    public void setGeodesic(boolean geodesic) {
        this.geodesic = geodesic;
    }
    
    /**
     * Checks if the polyline is visible
     * 
     * @return true, if polyline is visible
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Show/hide the polyline.
     * 
     * @param visible Set true to show the polyline
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GoogleMapPolyline other = (GoogleMapPolyline) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
    
    public boolean hasSameCoordinates(GoogleMapPolyline other) {
        if (other.getCoordinates() == null || getCoordinates() == null)
            return true;
        
        if (other.getCoordinates().size() != getCoordinates().size())
            return false;
        
        for (int i = 0; i < getCoordinates().size(); i += 1)
            if (!other.getCoordinates().get(i)
                    .hasSameFieldValues(getCoordinates().get(i)))
                return false;
        
        return true;
    }
    
    public boolean hasSameFieldValues(GoogleMapPolyline other) {
        if (!other.getStrokeColor().equals(getStrokeColor()))
            return false;
        
        if (Double.doubleToLongBits(other.getStrokeOpacity()) != Double
                .doubleToLongBits(getStrokeOpacity()))
            return false;
        
        if (other.getStrokeWeight() != getStrokeWeight())
            return false;
        
        if (other.getzIndex() != getzIndex())
            return false;
        
        if (other.isGeodesic() != isGeodesic())
            return false;
        
        if (other.isVisible() != isVisible())
            return false;
        
        return true;
    }
}
