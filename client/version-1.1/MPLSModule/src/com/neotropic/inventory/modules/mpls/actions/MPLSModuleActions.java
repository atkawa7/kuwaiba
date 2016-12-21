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
package com.neotropic.inventory.modules.mpls.actions;

import com.neotropic.inventory.modules.mpls.MPLSModuleService;
import com.neotropic.inventory.modules.mpls.scene.MPLSModuleScene;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractConnectionWidget;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.DeleteBusinessObjectAction;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;


/**
 * All the actions used by the nodes of an MPLSModuleScene
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MPLSModuleActions {
    private PopupMenuProvider nodeMenu;
    private PopupMenuProvider connectionMenu;
    private RemoveMPLSBusinessObjectFromView removeMPLSBusinessObjectFromViewAction;
    private DeleteMPLSConnection deleteMPLSConnectionAction;
    private MPLSModuleScene scene;

    public MPLSModuleActions(MPLSModuleScene scene) {
        this.scene = scene;
        
        removeMPLSBusinessObjectFromViewAction = new RemoveMPLSBusinessObjectFromView();
        deleteMPLSConnectionAction = new DeleteMPLSConnection();
    }
    
    public PopupMenuProvider createMenuForConnection() {
        if (connectionMenu == null) 
            connectionMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {                   
                    List<Action> actions = new ArrayList<>();
                    actions.add(removeMPLSBusinessObjectFromViewAction);
                    actions.add(deleteMPLSConnectionAction);
                    actions.add(null);
                    
                    AbstractConnectionWidget nodeWidget = (AbstractConnectionWidget)widget;
                    actions.addAll(Arrays.asList(nodeWidget.getNode().getActions(true)));
                    
                    return Utilities.actionsToPopup(actions.toArray(new Action[0]), scene.getView());                    
                }
            };
        return connectionMenu;
    }
    
    public PopupMenuProvider createMenuForNode() {
        if (nodeMenu == null) 
            nodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    List<Action> actions = new ArrayList<>();
                    actions.add(removeMPLSBusinessObjectFromViewAction);
                    actions.add(SystemAction.get(DeleteBusinessObjectAction.class));
                    actions.add(null);

                    AbstractNodeWidget nodeWidget = (AbstractNodeWidget)widget;
                    actions.addAll(Arrays.asList(nodeWidget.getLookup().lookup(ObjectNode.class).getActions(true)));

                    return Utilities.actionsToPopup(actions.toArray(new Action[0]), scene.getView()); 
                }
            };
        return nodeMenu;
    }
    
    public class RemoveMPLSBusinessObjectFromView extends AbstractAction {

        public RemoveMPLSBusinessObjectFromView() {
            this.putValue(NAME, "Remove From View"); 
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            for (Object selectedObject : selectedObjects) {
                LocalObjectLight castedObject = (LocalObjectLight)selectedObject;
                if (CommunicationsStub.getInstance().isSubclassOf(castedObject.getClassName(), MPLSModuleService.CLASS_GENERICEQUIPMENT))
                    scene.removeNodeWithEdges(castedObject);
                    
                else
                    scene.removeEdge(castedObject);
                
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
            }
        }
    }
    
    public class DeleteMPLSConnection extends AbstractAction {
      public DeleteMPLSConnection() {
            this.putValue(NAME, "Delete MPLS Link"); 
        }  

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to do this?", 
                    "Delete MPLS Link", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                Set<?> selectedObjects = scene.getSelectedObjects();
                for (Object selectedObject : selectedObjects) {
                    LocalObjectLight castedObject = (LocalObjectLight)selectedObject;
                    //TODO aqui!
                    if (CommunicationsStub.getInstance().deleteMPLSLink(castedObject.getClassName(), castedObject.getOid())) {
                        NotificationUtil.getInstance().showSimplePopup("Delete Operation", NotificationUtil.INFO_MESSAGE, "MPLS link deleted successfully");
                        scene.removeEdge(castedObject);
                        scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
                    } else 
                        NotificationUtil.getInstance().showSimplePopup("Delete Operation", NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            }
        }
    }
}
