/*
 * Copyright (c) 2018 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
 */
package com.neotropic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.BorderFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 *
 * @author johnyortega
 */
public class ResizableLabelWidget extends Widget {
    
    private String label;
    private double theta;
    
    public ResizableLabelWidget (Scene scene) {
        this (scene, null);
        setCheckClipping (true);
        setBorder(BorderFactory.createEmptyBorder());
    }
    
    public ResizableLabelWidget (Scene scene, String label) {
        super (scene);
        this.label = label;
    }
    
    public ResizableLabelWidget (Scene scene, String label, double theta) {
        super (scene);
        this.label = label;
        this.theta = theta;
    }
    
    public String getLabel () {
        return label;
    }
    
    public void setLabel (String label) {
        this.label = label;
    }
    
//    @Override
//    protected Rectangle calculateClientArea() {
//        if (getPreferredSize() == null || getBorder() == null)
//            return new Rectangle();
//                
//        Dimension size = getPreferredSize();
//        Insets insets = getBorder().getInsets();
//        
//        Rectangle rect = new Rectangle (0, 0, 
//            size.width/* - insets.left - insets.right*/, 
//            size.height/* - insets.top - insets.bottom*/);
//        return rect;
//    }
    @Override
    protected Rectangle calculateClientArea() {
//        setPreferredLocation(new Point(getPreferredLocation().x - 100, getPreferredLocation().y)); 
        return new Rectangle(-100, 0, 100, 100);
//        return super.calculateClientArea();
    }
    
    public void rotate() {
//        setPreferredLocation(new Point(getPreferredLocation().x - 100, getPreferredLocation().y));        
        
        // Thanks to https://coderanch.com/t/344978/java/scaling-font-resizing-dragging
        //           http://javaingrab.blogspot.com.co/2012/08/rotate-text-in-java-using-graphics2d.html
        // for provide the sample
//        super.paintWidget();
        Graphics2D g2d = getGraphics();
        //
        AffineTransform previousTransform = g2d.getTransform ();
        AffineTransform newTransform = new AffineTransform();
        newTransform.setToTranslation(g2d.getTransform().getTranslateX(), g2d.getTransform().getTranslateY());
        newTransform.rotate(Math.toRadians(90), 0, 0);
        g2d.setTransform(newTransform);
        g2d.setColor(Color.BLACK/*getForeground()*/);
        
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Font font = g2d.getFont(); 
        g2d.setFont(font);
        FontRenderContext frc = g2d.getFontRenderContext();
        LineMetrics metrics = font.getLineMetrics(getLabel(), frc);

        Insets insets = getBorder().getInsets();

        float height = metrics.getAscent() + metrics.getDescent();
        double width = font.getStringBounds(getLabel(), frc).getWidth();
        int w = getPreferredSize().width - insets.left - insets.right;
        int h = getPreferredSize().height - insets.top - insets.bottom;
        
        g2d.scale(w/width, h/height);
        g2d.drawString(getLabel(), 0, metrics.getAscent());
        
        g2d.setTransform(previousTransform);
    }
    
    @Override
    protected void paintWidget() {
        // Thanks to https://coderanch.com/t/344978/java/scaling-font-resizing-dragging
        //           http://javaingrab.blogspot.com.co/2012/08/rotate-text-in-java-using-graphics2d.html
        // for provide the sample
        Graphics2D g2d = getGraphics();
        //
        AffineTransform previousTransform = g2d.getTransform ();
        AffineTransform newTransform = new AffineTransform();
        newTransform.setToTranslation(g2d.getTransform().getTranslateX(), g2d.getTransform().getTranslateY());
        newTransform.rotate(Math.toRadians(90), 0, 0);
        g2d.setTransform(newTransform);
        g2d.setColor(Color.BLACK/*getForeground()*/);
        
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Font font = g2d.getFont(); 
        g2d.setFont(font);
        FontRenderContext frc = g2d.getFontRenderContext();
        LineMetrics metrics = font.getLineMetrics(getLabel(), frc);

        Insets insets = getBorder().getInsets();

        float height = metrics.getAscent() + metrics.getDescent();
        double width = font.getStringBounds(getLabel(), frc).getWidth();
        int w = getPreferredSize().width - insets.left - insets.right;
        int h = getPreferredSize().height - insets.top - insets.bottom;
        
        g2d.scale(w/width, h/height);
        g2d.drawString(getLabel(), 0, metrics.getAscent());
        
        g2d.setTransform(previousTransform);
////        super.paintWidget();
////        Graphics2D g2d = getGraphics();
////        //
////        AffineTransform previousTransform = g2d.getTransform ();
////        AffineTransform newTransform = new AffineTransform();
////////        AffineTransform oldTransform = g2d.getTransform();
////////        
////////        Rectangle preferredBounds = getPreferredBounds();
////////        
////////        AffineTransform newTransform = new AffineTransform();
////////        newTransform.setToTranslation(preferredBounds.width / 2, preferredBounds.height / 2);
////////        newTransform.rotate(Math.toRadians(45), 0, 0);
////        g2d.setTransform(newTransform);
////////        g2d.drawString(getLabel(), 0, 0);
////////        g2d.setTransform(oldTransform);
////        g2d.rotate(Math.toRadians(45), 0, 0);
////        
//////        .getPreferredBounds()
////        
//////        newTransform.setToTranslation(g2d.getTransform().getTranslateX(), g2d.getTransform().getTranslateY());
//////        newTransform.setToTranslation(0, 0);
//////        newTransform.rotate(Math.toRadians(0), 0, 0);
//////        g2d.setTransform(newTransform);
////        //
//////        g2d.drawRect(0, 0, 100, 100);
//////        g2d.setColor(Color.GREEN/*getForeground()*/);
//////        g2d.fillRect(0, 0, 100, 100);
////        g2d.setColor(Color.BLACK/*getForeground()*/);
////        
////        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
////                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
////        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
////                            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
////        Font font = g2d.getFont(); 
////        g2d.setFont(font);
////        FontRenderContext frc = g2d.getFontRenderContext();
////        LineMetrics metrics = font.getLineMetrics(getLabel(), frc);
////
////        Insets insets = getBorder().getInsets();
////
////        float height = metrics.getAscent() + metrics.getDescent();
////        double width = font.getStringBounds(getLabel(), frc).getWidth();
////        int w = getPreferredSize().width - insets.left - insets.right;
////        int h = getPreferredSize().height - insets.top - insets.bottom;
////        
////        
//////        g2d.translate (0, 0);
//////        previousTransform.setToRotation(Math.PI / 6);
////        
////        
////        g2d.scale(w/width, h/height);
////        g2d.drawString(getLabel(), 0, metrics.getAscent());
//////        g2d.drawString(getLabel(), 10, 10);
////                        
////        g2d.setTransform(previousTransform);
////        Graphics2D g2d = (Graphics2D) g;
////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,                         RenderingHints.VALUE_ANTIALIAS_ON);
////         g2d.setColor(Color.white); //to remove trail of painting
////         g2d.fillRect(0,0,getWidth(),getHeight());
////         Font font =  new Font("serif",Font.BOLD,50);
////         g2d.setFont(font);  //setting font of surface
////         FontRenderContext frc = g2d.getFontRenderContext();
////         TextLayout layout = new TextLayout("JAVA", font, frc);
////         //getting width & height of the text
////         double sw = layout.getBounds().getWidth();
////         double sh = layout.getBounds().getHeight();
////         //getting original transform instance 
////        AffineTransform saveTransform=g2d.getTransform();
////        g2d.setColor(Color.black);
////        Rectangle rect = this.getBounds();
////        //drawing the axis
////        g2d.drawLine((int)(rect.width)/2,0,(int)(rect.width)/2,rect.height);
////        g2d.drawLine(0,(int)(rect.height)/2,rect.width,(int)(rect.height)/2);
////        AffineTransform affineTransform = new AffineTransform();    /*creating instance set the translation to the mid of the component*/
////       affineTransform.setToTranslation((rect.width)/2,(rect.height)/2);
////       //rotate with the anchor point as the mid of the text
////       affineTransform.rotate(Math.toRadians(angdeg), 0, 0);
////       g2d.setTransform(affineTransform);
////       g2d.drawString("JAVA",(int)-sw/2,(int)sh/2);
////       g2d.setTransform(saveTransform); //restoring original transform
        
////        Graphics2D g2d = getGraphics();
////        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
////                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
////        g2d.setColor(Color.RED);
////        TextLayout textLayout = new TextLayout(getLabel(), g2d.getFont(), g2d.getFontRenderContext());        
////        double width = textLayout.getBounds().getWidth();
////        double height = textLayout.getBounds().getHeight();
////        
////        AffineTransform oldTransform = g2d.getTransform();
////        
////        Rectangle preferredBounds = getPreferredBounds();
////        
////        AffineTransform newTransform = new AffineTransform();
////        newTransform.setToTranslation(preferredBounds.width / 2, preferredBounds.height / 2);
////        newTransform.rotate(Math.toRadians(45), 0, 0);
////        g2d.setTransform(newTransform);
////        g2d.drawString(getLabel(), 0, 0);
////        g2d.setTransform(oldTransform);
    }
    
    public void fontResize() {
        Font font = getFont();
        int size = font.getSize();
                
        font.deriveFont(size);
    }
}
