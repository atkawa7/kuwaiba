/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.inventory.views.objectview.scene.menus;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class was originally developed by the Sun Visual Library team and comes bundled
 * into the examples provided in the Netbeans website and has been modified to fit into the Kuwaiba's context.
 * It keeps the original CDDL license. The original author was "Alex".
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class EdgeMenu implements PopupMenuProvider, ActionListener {
    
    private static final String ADD_REMOVE_CP_ACTION = "addRemoveCPAction"; // NOI18N

    private JPopupMenu menu;
    private ConnectionWidget edge;
    private Point point;

    public EdgeMenu() {
        menu = new JPopupMenu("Actions for this connection");
        JMenuItem item;

        item = new JMenuItem("Add/Delete Control Point");
        item.setActionCommand(ADD_REMOVE_CP_ACTION);
        item.addActionListener(this);
        menu.add(item);     
    }
    
    public JPopupMenu getPopupMenu(Widget widget, Point point){
        if (widget instanceof ConnectionWidget) {
            this.edge = (ConnectionWidget) widget;
            this.point=point;
            return menu;
        }
        return null;
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(ADD_REMOVE_CP_ACTION)) {
            addRemoveControlPoint(point);
        }
    }
    
    private void addRemoveControlPoint (Point localLocation) {
        ArrayList<Point> list = new ArrayList<Point> (edge.getControlPoints());
        double createSensitivity=1.00, deleteSensitivity=5.00;
            if(!removeControlPoint(localLocation,list,deleteSensitivity)){
                Point exPoint=null;int index=0;
                for (Point elem : list) {
                    if(exPoint!=null){
                        Line2D l2d=new Line2D.Double(exPoint,elem);
                        if(l2d.ptLineDist(localLocation)<createSensitivity){
                            list.add(index,localLocation);
                            break;
                        }
                    }
                    exPoint=elem;index++;
                }
            }
            edge.setControlPoints(list,false);
    }
    
    private boolean removeControlPoint(Point point, ArrayList<Point> list, double deleteSensitivity){
        for (Point elem : list) {
            if(elem.distance(point)<deleteSensitivity){
                list.remove(elem);
                return true;
            }
        }
        return false;
    }
    
}
