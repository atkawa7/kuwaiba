/**
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
package org.inventory.design.topology.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.design.topology.scene.TopologyViewScene;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 *  Action manager for the topology designer module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class TopologyViewActions {
    private PopupMenuProvider nodeMenu;
    private PopupMenuProvider connectionMenu;
    private PopupMenuProvider frameMenu;
    
    private DeleteNodeAction deleteNode;
    private DeleteConnectionAction deleteConnection;
    private DeleteFrameAction deleteFrame;
    private TopologyViewScene scene;
    
    public TopologyViewActions(TopologyViewScene scene) {
        this.scene = scene;
        
        deleteNode = new DeleteNodeAction();
        deleteConnection = new DeleteConnectionAction();
        deleteFrame = new DeleteFrameAction();
    }
    
    public PopupMenuProvider createMenuForNode() {
        if (nodeMenu == null) {
            nodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point point) {
                    JPopupMenu theMenu = new JPopupMenu("Options");
                    theMenu.add(deleteNode);
                    return theMenu;
                }
            };
        }
        return nodeMenu;
    }
    
    public PopupMenuProvider createMenuForConnection() {
        if (connectionMenu == null) {
            connectionMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point point) {
                    JPopupMenu theMenu = new JPopupMenu("Connection Menu");
                    theMenu.add(deleteConnection);
                    return theMenu;
                }
            };
        }
        return connectionMenu;
    }
    
    public PopupMenuProvider createMenuForFrame() {
        if (frameMenu == null) {
            frameMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point point) {
                    JPopupMenu theMenu = new JPopupMenu("Frame Menu");
                    theMenu.add(deleteFrame);
                    return theMenu;
                }
            };
        }
        return frameMenu;
    }
    
    public class DeleteNodeAction extends AbstractAction {
        public DeleteNodeAction() {
            this.putValue(NAME, "Remove Node from View");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            for (Object selectedObject : selectedObjects) {
                LocalObjectLight lol = (LocalObjectLight)selectedObject;
                if(lol.getName().contains(TopologyViewScene.CLOUD_ICON) || CommunicationsStub.getInstance().isSubclassOf(lol.getClassName(), Constants.CLASS_VIEWABLEOBJECT))
                    scene.removeNodeWithEdges(lol);
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
            }
        }
    }
    
    public class DeleteConnectionAction extends AbstractAction {
        
        DeleteConnectionAction() {
            this.putValue(NAME, "Remove Connection from View");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            for (Object selectedObject : selectedObjects) {
                String edge = (String)selectedObject;
                scene.removeEdge(edge);
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
            }
        }
    }
    
    public class DeleteFrameAction extends AbstractAction {
        
        DeleteFrameAction() {
            this.putValue(NAME, "Delete Frame");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            for (Object selectedObject : selectedObjects) {
                LocalObjectLight lol = (LocalObjectLight)selectedObject;
                if(lol.getName().contains(TopologyViewScene.FREE_FRAME))
                    scene.removeNodeWithEdges(lol);
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
            }
        }
        
    }
}
