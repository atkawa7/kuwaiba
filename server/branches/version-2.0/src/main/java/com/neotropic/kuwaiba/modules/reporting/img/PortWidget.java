/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.reporting.img;

import java.awt.Color;
import java.awt.Dimension;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author adrian
 */
public class PortWidget extends Widget{
     /**
     * The default height of the box containing the labels
     */
    public static int DEFAULT_WIDGET_HEIGHT = 40;
    /**
     * The default height of the box containing the port names
     */
    public static int DEFAULT_WIDGET_WIDTH_1 = 20;
    /**
     * The default height of the box containing the link names
     */
    public static int DEFAULT_WIDGET_WIDTH_2 = 200;
    /**
     * The link connected to the port
     */
    private RemoteObject connectedLink;
    /**
     * Label that displays the port name to the left side of the widget
     */
    private Widget linkColorWidget;
    /**
     * Label that displays the port name to the left side of the widget
     */
    private LabelWidget portNameWidget;
    
    /**
     * Default constructor
     * @param scene Scene the widget belongs to
     * @param port The business object representing the port
     * @param connectedLink
     * @param alignment
     */
    public PortWidget(Scene scene, RemoteObjectLight port, RemoteObject connectedLink, ALIGNMENT alignment) {
        super(scene);
        this.connectedLink = connectedLink;
        this.linkColorWidget = new Widget(scene);
        int i=0;
            if(connectedLink != null){
            String[] attributes = connectedLink.getAttributes();
            for(String attr : attributes){
                if(attr.equals("name"))
                    break;
                i++;
            }
        }
        
        this.portNameWidget = new LabelWidget(scene, String.format("%s [%s]", 
                port.getName(), connectedLink == null ? "Not Connected" :  //NOI18N
                        (connectedLink.getValues()[i][0].length())));  //NOI18N
        
        
        //(connectedLink.getValues()[i][0].length() > 15 ? connectedLink.getValues()[i][0].substring(0, 15) + "...": connectedLink.getValues()[i][0])));  //NOI18N
        
        this.portNameWidget.setOpaque(true);
        this.linkColorWidget.setOpaque(true);
        
        this.portNameWidget.setPreferredSize(new Dimension(DEFAULT_WIDGET_WIDTH_2, DEFAULT_WIDGET_HEIGHT));
        this.linkColorWidget.setPreferredSize(new Dimension(DEFAULT_WIDGET_WIDTH_1, DEFAULT_WIDGET_HEIGHT));
        
        if (alignment.equals(ALIGNMENT.RIGHT)) {
            this.addChild(this.linkColorWidget);
            this.addChild(this.portNameWidget);
        } else {
            this.addChild(this.portNameWidget);
            this.addChild(this.linkColorWidget);
        }
        
        setLayout(LayoutFactory.createHorizontalFlowLayout());
        
    }

    public RemoteObject getConnectedLink() {
        return connectedLink;
    }

    public void setConnectedLink(RemoteObject connectedLink) {
        this.connectedLink = connectedLink;
        if (connectedLink == null) //Nothing connected
            linkColorWidget.setBackground(Color.DARK_GRAY);
        else {
            String[] attributes = connectedLink.getAttributes();
            int i=0;
            for(String attr : attributes){
                if(attr.equals("color"))
                    break;
                i++;
            }
            
            Integer rawColor = Integer.valueOf(connectedLink.getValues()[i][0]);
            if (rawColor != null)
                linkColorWidget.setBackground(new Color(rawColor));
            else
                linkColorWidget.setBackground(Color.BLACK);
        }
    }

    public Widget getPortNameWidget() {
        return linkColorWidget;
    }

    public void setPortNameWidget(Widget linkColorWidget) {
        this.linkColorWidget = linkColorWidget;
    }

    public LabelWidget getLinkNameWidget() {
        return portNameWidget;
    }

    public void setLinkNameWidget(LabelWidget portNameWidget) {
        this.portNameWidget = portNameWidget;
    }
    
    /**
     * The port and link name should be at the right side or the left side of the widget
     */
    public enum ALIGNMENT {
        RIGHT,
        LEFT
    }
}
