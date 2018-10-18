package com.vaadin.tapio.googlemaps.client.overlays;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.MapCanvasProjection;
import com.google.gwt.maps.client.overlays.OverlayView;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewMethods;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnAddHandler;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnDrawHandler;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnRemoveHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GoogleGwtMapLabel {
    private String caption;
    private final OverlayView overlayLabel;
    private VerticalPanel pnlLabel;
    private HTML htmlCaption;
    
    private final MapWidget mapWidget;
    private LatLng latLng;
    
    private boolean isVisible = true;
        
    private final OverlayViewOnDrawHandler onDrawHandler = new OverlayViewOnDrawHandler() {

        @Override
        public void onDraw(OverlayViewMethods methods) {
            MapCanvasProjection projection = methods.getProjection();
            Point p = projection.fromLatLngToDivPixel(latLng);
            pnlLabel.getElement().getStyle().setPosition(Position.ABSOLUTE);
            pnlLabel.getElement().getStyle().setLeft(p.getX(), Unit.PX);
            pnlLabel.getElement().getStyle().setTop(p.getY(), Unit.PX);   
            pnlLabel.getElement().getStyle().setHeight(35, Unit.PX);
            pnlLabel.clear();
            pnlLabel.add(htmlCaption);
        }
    };
    
    private final OverlayViewOnAddHandler onAddHandler = new OverlayViewOnAddHandler() {

        @Override
        public void onAdd(OverlayViewMethods methods) {
            methods.getPanes().getFloatPane().appendChild(pnlLabel.getElement());
        }
    };
    
    private final OverlayViewOnRemoveHandler onRemoveHandler = new OverlayViewOnRemoveHandler() {

        @Override
        public void onRemove(OverlayViewMethods methods) {
            pnlLabel.getElement().removeFromParent();
        }
    };
        
    public GoogleGwtMapLabel(MapWidget mapWidget, LatLng latLng, String caption) {
        this.caption = caption;
        htmlCaption = new HTML("<span style=\"padding: 4px; font-size: small; background-color:cyan; moz-border-radius:3px 3px 3px 3px; border-radius:3px 3px 3px 3px;\">" + caption + "</span>");
                        
        this.mapWidget = mapWidget;
        this.latLng = latLng;
        
        pnlLabel = new VerticalPanel();                        
        overlayLabel = OverlayView.newInstance(mapWidget, onDrawHandler, onAddHandler, onRemoveHandler);        
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        
        if (isVisible)
            overlayLabel.setMap(mapWidget);
        else
            overlayLabel.setMap(null);
    }
        
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
        htmlCaption.setHTML("<span style=\"padding: 4px; font-size: small; background-color:cyan; moz-border-radius:3px 3px 3px 3px; border-radius:3px 3px 3px 3px;\">" + caption + "</span>");
    }
}