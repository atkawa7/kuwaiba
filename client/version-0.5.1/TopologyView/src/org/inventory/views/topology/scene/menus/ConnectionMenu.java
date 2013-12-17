/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.inventory.views.topology.scene.menus;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.views.topology.scene.ObjectConnectionWidget;
import org.inventory.views.topology.scene.TopologyViewScene;


/**
 * A edge menu
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */

public class ConnectionMenu implements PopupMenuProvider, ActionListener {
    
    private static final String REMOVE_CONTROL_POINT = "removeControPoint"; // NOI18N
    private static final String DELETE_CONNECTION = "deleteConnection"; // NOI18N

    private TopologyViewScene scene;

    private JPopupMenu menu;
    private ObjectConnectionWidget edge;
    private Point point;
    

    public ConnectionMenu(TopologyViewScene scene) {
        this.scene = scene;
        menu = new JPopupMenu("Connecttion Menu"); //NOI18N
        JMenuItem item;

        item = new JMenuItem("Delete Control Point");
        item.setActionCommand(REMOVE_CONTROL_POINT);
        item.addActionListener(this);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Delete connection");
        item.setActionCommand(DELETE_CONNECTION);
        item.addActionListener(this);
        menu.add(item);

    }
    
    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point point){
        if (widget instanceof ObjectConnectionWidget) {
            this.edge = (ObjectConnectionWidget) widget;
            this.point=point;
            return menu;
        }
        return null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(REMOVE_CONTROL_POINT)) {
            addRemoveControlPoint(point);
        } else if(e.getActionCommand().equals(DELETE_CONNECTION)) {
            Object findObject = scene.findObject(edge);
            scene.setFocusedObject(null);
            scene.removeEdge((String)findObject);
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
