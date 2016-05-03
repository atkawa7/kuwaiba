/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.inventory.views.topology.scene.ObjectNodeWidget;
import org.inventory.views.topology.scene.TopologyViewScene;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author alex
 */
public class NodeMenu implements PopupMenuProvider, ActionListener {

    private static final String DELETE_NODE_ACTION = "deleteNodeAction"; // NOI18N

    private JPopupMenu menu;
    private ObjectNodeWidget node;

    private TopologyViewScene scene;

    public NodeMenu(TopologyViewScene scene) {
        this.scene=scene;
        menu = new JPopupMenu("Node Menu");
        JMenuItem item;

        item = new JMenuItem("Delete Node");
        item.setActionCommand(DELETE_NODE_ACTION);
        item.addActionListener(this);
        menu.add(item);
    }

    @Override
    public JPopupMenu getPopupMenu(Widget widget,Point point){
        node = (ObjectNodeWidget) widget;
        
        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(DELETE_NODE_ACTION)){
            scene.removeNodeWithEdges(scene.findObject(node));
            scene.validate();
        }
    }
}
