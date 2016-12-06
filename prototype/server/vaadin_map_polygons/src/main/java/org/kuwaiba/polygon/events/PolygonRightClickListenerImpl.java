/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.polygon.events;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.events.PolygonRightClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.polygon.PolygonExt;
import org.kuwaiba.polygon.buttons.ButtonDeletePolygon;
import org.kuwaiba.polygon.buttons.ButtonEditPolygon;

/**
 *
 * @author johnyortega
 */
public class PolygonRightClickListenerImpl implements PolygonRightClickListener {
    private final GoogleMap googleMap;
    
    public PolygonRightClickListenerImpl(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public void polygonRightClicked(GoogleMapPolygon clickedPolygon) {
        if (clickedPolygon instanceof PolygonExt) {
            GoogleMapInfoWindow infoWindow = new GoogleMapInfoWindow();
            infoWindow.setPosition(clickedPolygon.getCoordinates().get(0));
            //TODO: set height and width don't work review the add-on
            //infoWindow.setHeight("2000 px");
            //infoWindow.setWidth("2000 px");
            PolygonExt polygonExt = (PolygonExt) clickedPolygon;
            polygonExt.setInfoWindow(infoWindow);
            
            VerticalLayout layout = new VerticalLayout();
            
            Button btnEdit = new ButtonEditPolygon((PolygonExt) polygonExt);
            Button btnDelete = new ButtonDeletePolygon((PolygonExt) polygonExt);
            
            /*
            Button btnDelete = new Button("Delete");
            btnDelete.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
                    */
            layout.addComponent(btnEdit);
            layout.addComponent(btnDelete);

            googleMap.setInfoWindowContents(infoWindow, layout);

            googleMap.openInfoWindow(infoWindow);
        }
    }
    
}
